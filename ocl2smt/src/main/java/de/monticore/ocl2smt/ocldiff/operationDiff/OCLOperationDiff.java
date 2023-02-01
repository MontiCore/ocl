package de.monticore.ocl2smt.ocldiff.operationDiff;

import static de.monticore.ocl2smt.ocldiff.OCLDiffHelper.buildContext;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocl2smt.FullOCL2SMTGenerator;
import de.monticore.ocl2smt.ocldiff.TraceUnSatCore;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odlink._ast.ASTODLink;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.stream.Collectors;

public class OCLOperationDiff {

  protected Context ctx;

  public Set<OCLOPWitness> oclWitness(
      ASTCDCompilationUnit ast,
      Set<ASTOCLCompilationUnit> ocl,
      ASTOCLMethodSignature method,
      boolean partial) {

    ctx = buildContext();
    OCLHelper.buildPreCD(ast);
    FullOCL2SMTGenerator fullOCL2SMTGenerator = new FullOCL2SMTGenerator(ast, ctx);

    return oclWitnessHelper(ocl, fullOCL2SMTGenerator, method, partial);
  }

  // TODO: take care of invariant
  public Set<OCLOPWitness> oclWitness(
      ASTCDCompilationUnit ast, Set<ASTOCLCompilationUnit> ocl, boolean partial) {
    ctx = buildContext();
    OCLHelper.buildPreCD(ast);
    FullOCL2SMTGenerator fullOCL2SMTGenerator = new FullOCL2SMTGenerator(ast, ctx);
    Set<OCLOPWitness> res = new HashSet<>();

    for (ASTOCLMethodSignature method : getMethodList(ocl)) {
      res.addAll(oclWitnessHelper(ocl, fullOCL2SMTGenerator, method, partial));
    }
    return res;
  }

  private Set<OCLOPWitness> oclWitnessHelper(
      Set<ASTOCLCompilationUnit> ocl,
      FullOCL2SMTGenerator fullOCL2SMTGenerator,
      ASTOCLMethodSignature method,
      boolean partial) {

    List<OPConstraint> constraintList =
        opConst2smt(fullOCL2SMTGenerator, getOperationsConstraints(method, ocl));

    // add the opConstraints
    List<IdentifiableBoolExpr> opConstraints =
        constraintList.stream()
            .map(OPConstraint::getOperationConstraint)
            .collect(Collectors.toList());

    Set<OCLOPWitness> res = new HashSet<>();
    Solver solver;

    for (OPConstraint constraint : constraintList) {
      opConstraints.add(constraint.getPreCond());
      solver = fullOCL2SMTGenerator.makeSolver(opConstraints);
      if (solver.check() == Status.SATISFIABLE) {
        res.add(
            fullOCL2SMTGenerator.buildOPOd(solver.getModel(), "Name", method, constraint, partial));
      } else {
        Log.info("the Preconditions XXXXXX is not Satisfiable", "");
      }
      opConstraints.remove(constraint.getPreCond());
    }
    return res;
  }

  public OCLOPDiffResult oclDiff(
      ASTCDCompilationUnit ast,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOcl,
      ASTOCLMethodSignature method,
      boolean partial) {

    // setup
    ctx = buildContext();
    Set<OCLOPWitness> diffWitness = new HashSet<>();
    List<ASTODLink> trace = new ArrayList<>();
    OCLHelper.buildPreCD(ast);
    FullOCL2SMTGenerator fullOCL2SMTGenerator = new FullOCL2SMTGenerator(ast, ctx);

    // get new OCL constraints
    List<OPConstraint> newConstraints =
        opConst2smt(fullOCL2SMTGenerator, getOperationsConstraints(method, newOcl));

    // get old ocl Constraints
    List<OPConstraint> oldConstraints =
        opConst2smt(fullOCL2SMTGenerator, getOperationsConstraints(method, oldOcl));

    // transform  pre , post =====> pre implies post
    List<IdentifiableBoolExpr> posConstraints =
        newConstraints.stream()
            .map(OPConstraint::getOperationConstraint)
            .collect(Collectors.toList());

    List<IdentifiableBoolExpr> negativeConstraints = new ArrayList<>();
    Solver solver;
    List<IdentifiableBoolExpr> solverConstraints = new ArrayList<>(posConstraints);

    for (OPConstraint oldConstraint : oldConstraints) {
      solverConstraints.add(oldConstraint.getPreCond());
      posConstraints.add(oldConstraint.getPreCond());

      IdentifiableBoolExpr oldOpConstraint = oldConstraint.getOperationConstraint().negate(ctx);
      solverConstraints.add(oldOpConstraint);
      negativeConstraints.add(oldOpConstraint);

      solver = fullOCL2SMTGenerator.makeSolver(solverConstraints);

      if (solver.check() == Status.SATISFIABLE) {
        diffWitness.add(
            fullOCL2SMTGenerator.buildOPOd(
                solver.getModel(), "Witness", method, oldConstraint, partial));
      } else {
        trace.addAll(TraceUnSatCore.traceUnSatCore(solver));
      }
      posConstraints.remove(oldConstraint.getPreCond());
      posConstraints.remove(oldOpConstraint);
    }
    ASTODArtifact unSatCore =
        TraceUnSatCore.buildUnSatOD(posConstraints, negativeConstraints, trace);
    return new OCLOPDiffResult(unSatCore, diffWitness);
  }
  // TODO: fix this  in case of many  constraints per operation

  private List<ASTOCLOperationConstraint> getOperationsConstraints(
      Set<ASTOCLCompilationUnit> oclSet) {
    return oclSet.stream()
        .map(ocl -> ocl.getOCLArtifact().getOCLConstraintList())
        .collect(Collectors.toList())
        .stream()
        .flatMap(List::stream)
        .filter(c -> c instanceof ASTOCLOperationConstraint)
        .map(c -> (ASTOCLOperationConstraint) c)
        .collect(Collectors.toList());
  }

  private Map<ASTOCLMethodSignature, List<ASTOCLOperationConstraint>> sortOPConstraint(
      Set<ASTOCLCompilationUnit> oclSet) {

    Map<ASTOCLMethodSignature, List<ASTOCLOperationConstraint>> res = new HashMap<>();
    for (ASTOCLOperationConstraint opConstraint : getOperationsConstraints(oclSet)) {

      ASTOCLOperationSignature opSignature = opConstraint.getOCLOperationSignature();

      if (opSignature instanceof ASTOCLMethodSignature) {

        if (containsKey(res, (ASTOCLMethodSignature) opSignature)) {
          res.get((ASTOCLMethodSignature) opSignature).add(opConstraint);
        } else {
          List<ASTOCLOperationConstraint> opConstraintList = new ArrayList<>();
          opConstraintList.add(opConstraint);
          res.put((ASTOCLMethodSignature) opSignature, opConstraintList);
        }
      }
    }
    return res;
  }

  private List<ASTOCLOperationConstraint> getOperationsConstraints(
      ASTOCLOperationSignature method, Set<ASTOCLCompilationUnit> ocl) {
    return getOperationsConstraints(ocl).stream()
        .filter(c -> c.getOCLOperationSignature().deepEquals(method))
        .collect(Collectors.toList());
  }

  public List<ASTOCLMethodSignature> getMethodList(Set<ASTOCLCompilationUnit> ocl) {
    List<ASTOCLMethodSignature> res = new ArrayList<>();

    for (ASTOCLOperationConstraint constraint : getOperationsConstraints(ocl)) {
      ASTOCLOperationSignature method = constraint.getOCLOperationSignature();
      if (method instanceof ASTOCLMethodSignature && !res.contains(method)) {
        res.add((ASTOCLMethodSignature) method);
      }
    }
    return res;
  }

  public boolean containsKey(
      Map<ASTOCLMethodSignature, List<ASTOCLOperationConstraint>> map,
      ASTOCLMethodSignature method) {
    return map.keySet().stream().anyMatch(key -> key.deepEquals(method));
  }

  protected List<OPConstraint> opConst2smt(
      FullOCL2SMTGenerator fullOCL2SMTGenerator,
      List<ASTOCLOperationConstraint> operationConstraints) {
    return operationConstraints.stream()
        .map(fullOCL2SMTGenerator::convertOpConst)
        .collect(Collectors.toList());
  }
}
