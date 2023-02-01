package de.monticore.ocl2smt.ocldiff.operationDiff;

import static de.monticore.ocl2smt.ocldiff.OCLDiffHelper.buildContext;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTStrategy;
import de.monticore.ocl2smt.ocldiff.TraceUnSatCore;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odlink._ast.ASTODLink;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.stream.Collectors;

public class OCLOperationDiff {
/*
  protected Context ctx;

  public Set<OCLOPWitness> oclWitness(
      ASTCDCompilationUnit ast,
      Set<ASTOCLCompilationUnit> ocl,
      ASTOCLMethodSignature method,
      boolean partial) {

    ctx = buildContext();
    OCL2SMTStrategy.buildPreCD(ast);
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(ast, ctx);

    return oclWitnessHelper(ocl, ocl2SMTGenerator, method, partial);
  }

  // TODO: take care of invariant
  public Set<OCLOPWitness> oclWitness(
      ASTCDCompilationUnit ast, Set<ASTOCLCompilationUnit> ocl, boolean partial) {
    ctx = buildContext();
    OCL2SMTStrategy.buildPreCD(ast);
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(ast, ctx);
    Set<OCLOPWitness> res = new HashSet<>();

    for (ASTOCLMethodSignature method : getMethodList(ocl)) {
      res.addAll(oclWitnessHelper(ocl, ocl2SMTGenerator, method, partial));
    }
    return res;
  }

  private Set<OCLOPWitness> oclWitnessHelper(
      Set<ASTOCLCompilationUnit> ocl,
      OCL2SMTGenerator ocl2SMTGenerator,
      ASTOCLMethodSignature method,
      boolean partial) {

    List<OPConstraint> constraints =
        opConst2smt(ocl2SMTGenerator, getOperationsConstraints(method, ocl));

    // add the pre-condition
    List<IdentifiableBoolExpr> preConditionsList =
        constraints.stream().map(OPConstraint::getPreCond).collect(Collectors.toList());

    // add the opConstraints
    List<IdentifiableBoolExpr> opConstraints =
        constraints.stream().map(op -> mkOperationConstraint(op, ctx)).collect(Collectors.toList());

    Set<OCLOPWitness> res = new HashSet<>();
    Solver solver;

    for (IdentifiableBoolExpr pre : preConditionsList) {
      opConstraints.add(pre);
      solver = ocl2SMTGenerator.makeSolver(opConstraints);
      if (solver.check() == Status.SATISFIABLE) {
        res.add(ocl2SMTGenerator.buildOPOd(solver.getModel(), "Name", method, partial));
      } else {
        Log.info("the Preconditions XXXXXX is not Satisfiable", "");
      }
      opConstraints.remove(pre);
    }
    return res;
  }

  public IdentifiableBoolExpr mkOperationConstraint(OPConstraint OPConstraint, Context ctx) {
    if (OPConstraint.isInvariant()) {
      Log.info("", "Cannot make Operation Constraint with OCL Invariant Invariant");
    }
    BoolExpr opConstraint =
        ctx.mkImplies(
            OPConstraint.getPreCond().getValue(), OPConstraint.getPostCond().getValue());
    return IdentifiableBoolExpr.buildIdentifiable(
        opConstraint, OPConstraint.getPreCond().getSourcePosition(), Optional.of("pre ==> Post"));
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
    OCL2SMTStrategy.buildPreCD(ast);
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(ast, ctx);

    // get new OCL constraints
    List<OPConstraint> newConstraints =
        opConst2smt(ocl2SMTGenerator, getOperationsConstraints(method, newOcl));

    // get old ocl Constraints
    List<OPConstraint> oldConstraints =
        opConst2smt(ocl2SMTGenerator, getOperationsConstraints(method, oldOcl));

    // transform  pre , post =====> pre implies post
    List<IdentifiableBoolExpr> posConstraints =
        newConstraints.stream()
            .map(x -> mkOperationConstraint(x, ctx))
            .collect(Collectors.toList());

    List<IdentifiableBoolExpr> negativeConstraints = new ArrayList<>();
    Solver solver;
    List<IdentifiableBoolExpr> solverConstraints = new ArrayList<>(posConstraints);

    for (OPConstraint oldConstraint : oldConstraints) {
      solverConstraints.add(oldConstraint.getPreCond());
      posConstraints.add(oldConstraint.getPreCond());

      IdentifiableBoolExpr oldOpConstraint = mkOperationConstraint(oldConstraint, ctx).negate(ctx);
      solverConstraints.add(oldOpConstraint);
      negativeConstraints.add(oldOpConstraint);

      solver = ocl2SMTGenerator.makeSolver(solverConstraints);

      if (solver.check() == Status.SATISFIABLE) {
        diffWitness.add(ocl2SMTGenerator.buildOPOd(solver.getModel(), "Witness", method, partial));
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
      OCL2SMTGenerator ocl2SMTGenerator, List<ASTOCLOperationConstraint> operationConstraints) {
    return operationConstraints.stream()
        .map(ocl2SMTGenerator::convertOpConst)
        .collect(Collectors.toList());
  }*/
}
