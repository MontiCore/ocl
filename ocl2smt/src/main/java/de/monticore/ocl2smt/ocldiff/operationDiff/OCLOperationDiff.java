package de.monticore.ocl2smt.ocldiff.operationDiff;

import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
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
    List<IdentifiableBoolExpr> solverConstraints = new ArrayList<>();
    List<IdentifiableBoolExpr> invConstraints =
        inv2smt(fullOCL2SMTGenerator, getInvariantList(ocl));

    List<OPConstraint> opConstraints =
        opConst2smt(fullOCL2SMTGenerator, getOperationsConstraints(method, ocl));

    // add the opConstraints
    opConstraints.forEach(op -> solverConstraints.add(op.getOperationConstraint()));

    // add invariants
    solverConstraints.addAll(invConstraints);

    Set<OCLOPWitness> res = new HashSet<>();
    Solver solver;

    for (OPConstraint constraint : opConstraints) {
      solverConstraints.add(constraint.getPreCond());
      solver = fullOCL2SMTGenerator.makeSolver(solverConstraints);
      if (solver.check() == Status.SATISFIABLE) {
        res.add(
            fullOCL2SMTGenerator.buildOPOd(
                solver.getModel(), method.getMethodName().getQName(), method, constraint, partial));
      } else {
        Log.info("the Preconditions XXXXXX is not Satisfiable", "");
      }
      solverConstraints.remove(constraint.getPreCond());
    }
    return res;
  }
  public OCLOPDiffResult oclDiffV1(
          ASTCDCompilationUnit ast,
          Set<ASTOCLCompilationUnit> oldOcl,
          Set<ASTOCLCompilationUnit> newOcl,
          ASTOCLMethodSignature method,
          boolean partial) {
    return  oclDiffHelper(ast,oldOcl,newOcl,method,true,partial) ;
  }
  public OCLOPDiffResult oclDiffV2(
          ASTCDCompilationUnit ast,
          Set<ASTOCLCompilationUnit> oldOcl,
          Set<ASTOCLCompilationUnit> newOcl,
          ASTOCLMethodSignature method,
          boolean partial) {
    return  oclDiffHelper(ast,oldOcl,newOcl,method,false,partial) ;
  }
  public OCLOPDiffResult oclDiffHelper(
      ASTCDCompilationUnit ast,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOcl,
      ASTOCLMethodSignature method,
          boolean preCondHolds,
      boolean partial) {

    // setup
    ctx = buildContext();
    Set<OCLOPWitness> opDiffWitness = new HashSet<>();
    List<ASTODLink> trace = new ArrayList<>();
    OCLHelper.buildPreCD(ast);
    FullOCL2SMTGenerator fullOcl2smt = new FullOCL2SMTGenerator(ast, ctx);

    // get new invariants
    List<IdentifiableBoolExpr> newInvariants = inv2smt(fullOcl2smt, getInvariantList(newOcl));

    // get new OCL constraints
    List<OPConstraint> newConstraints =
            opConst2smt(fullOcl2smt, getOperationsConstraints(method, newOcl));

    // get old ocl Constraints
    List<OPConstraint> oldConstraints =
            opConst2smt(fullOcl2smt, getOperationsConstraints(method, oldOcl));


    // build the set of positive constraints from new opConstraints and new invariants;
    List<IdentifiableBoolExpr> posConstraints = new ArrayList<>();
    newConstraints.forEach(op -> {
      if (preCondHolds){
        posConstraints.add(op.getPreCond());
        posConstraints.add(op.getPostCond());
      }else {
        posConstraints.add(op.getPreCond().negate(ctx));
      }

    });
    posConstraints.addAll(newInvariants);

    List<IdentifiableBoolExpr> negativeConstraints = new ArrayList<>();

    Solver solver;
    List<IdentifiableBoolExpr> solverConstraints = new ArrayList<>(posConstraints);

    // diff between new on operation constraints
    for (OPConstraint oldConstraint : oldConstraints) {
      solverConstraints.add(oldConstraint.getPreCond());
      posConstraints.add(oldConstraint.getPreCond());

      IdentifiableBoolExpr oldOpConstraint = oldConstraint.getPostCond().negate(ctx);
      solverConstraints.add(oldOpConstraint);
      negativeConstraints.add(oldOpConstraint);

      solver = fullOcl2smt.makeSolver(solverConstraints);

      if (solver.check() == Status.SATISFIABLE) {
        opDiffWitness.add(
                fullOcl2smt.buildOPOd(solver.getModel(), "Witness", method, oldConstraint, partial));
      } else {
        trace.addAll(TraceUnSatCore.traceUnSatCore(solver));
      }
      posConstraints.remove(oldConstraint.getPreCond());
      solverConstraints.remove(oldOpConstraint);
    }

    ASTODArtifact unSatCore =
        TraceUnSatCore.buildUnSatOD(posConstraints, negativeConstraints, trace);
    return new OCLOPDiffResult(unSatCore, opDiffWitness);
  }


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

  public List<ASTOCLInvariant> getInvariantList(Set<ASTOCLCompilationUnit> oclSet) {
    return oclSet.stream()
        .map(ocl -> ocl.getOCLArtifact().getOCLConstraintList())
        .collect(Collectors.toList())
        .stream()
        .flatMap(List::stream)
        .filter(c -> c instanceof ASTOCLInvariant)
        .map(c -> (ASTOCLInvariant) c)
        .collect(Collectors.toList());
  }

  public boolean containsKey(
      Map<ASTOCLMethodSignature, List<ASTOCLOperationConstraint>> map,
      ASTOCLMethodSignature method) {
    return map.keySet().stream().anyMatch(key -> key.deepEquals(method));
  }

  protected List<OPConstraint> opConst2smt(
      FullOCL2SMTGenerator fullOCL2SMTGenerator, List<ASTOCLOperationConstraint> opConstraints) {
    return opConstraints.stream()
        .map(fullOCL2SMTGenerator::convertOpConst)
        .collect(Collectors.toList());
  }

  protected List<IdentifiableBoolExpr> inv2smt(
          FullOCL2SMTGenerator fullOcl2smt, List<ASTOCLInvariant> invList) {
    List<IdentifiableBoolExpr> res = new ArrayList<>();

    for (ASTOCLInvariant inv : invList) {
      IdentifiableBoolExpr postInv = fullOcl2smt.convertInv(inv);
      IdentifiableBoolExpr preInv = fullOcl2smt.convertPreInv(inv);

      res.add(
              IdentifiableBoolExpr.buildIdentifiable(
                      ctx.mkAnd(preInv.getValue(), postInv.getValue()),
                      preInv.getSourcePosition(),
                      preInv.getInvariantName()));
    }
    return res;
  }

  public String buildInvName(IdentifiableBoolExpr constr) {
    return constr.getInvariantName().orElse("NoInvName").split("_____NegInv")[0];
  }

  public Context buildContext() {
    Map<String, String> cfg = new HashMap<>();
    cfg.put("model", "true");
    return new Context(cfg);
  }
}
