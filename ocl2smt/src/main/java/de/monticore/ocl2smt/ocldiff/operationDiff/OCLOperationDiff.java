package de.monticore.ocl2smt.ocldiff.operationDiff;

import static de.monticore.ocl2smt.ocldiff.OCLDiffHelper.buildContext;
import static de.monticore.ocl2smt.ocldiff.OCLDiffHelper.buildInvName;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTStrategy;
import de.monticore.ocl2smt.ocldiff.TraceUnsatCore;
import de.monticore.ocl2smt.util.OCLConstraint;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odlink._ast.ASTODLink;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.stream.Collectors;

public class OCLOperationDiff {

  protected Context ctx;

  public Set<OCLOPWitness> oclWitness(
      ASTCDCompilationUnit ast, Set<ASTOCLCompilationUnit> ocl, boolean partial) {

    ctx = buildContext();
    OCL2SMTStrategy.buildPreCD(ast);
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(ast, ctx);

    Set<OCLOPWitness> res = new HashSet<>();
    Map<ASTOCLMethodSignature, List<ASTOCLOperationConstraint>> opConstraintMap =
        sortOPConstraint(ocl);

    for (Map.Entry<ASTOCLMethodSignature, List<ASTOCLOperationConstraint>> entry :
        opConstraintMap.entrySet()) {
      List<OCLConstraint> constraints = opConst2smt(ocl2SMTGenerator, entry.getValue());

      // add the pre-condition
      List<IdentifiableBoolExpr> preConditionsList =
          constraints.stream().map(OCLConstraint::getPreCond).collect(Collectors.toList());

      // add the post condition
      List<IdentifiableBoolExpr> opConstraintsList =
          constraints.stream().map(op -> mkOperationConstraint(op, ctx)).collect(Collectors.toList());

      /*   // add the invariants    TODO:Operation constraints with Invariant with Invariants
      solversConstraints.addAll(
          constraints.stream()
              .filter(OCLConstraint::isInvariant)
              .map(OCLConstraint::getInvariant)
              .collect(Collectors.toList()));*/

      res.addAll(
          oclWitnessHelper(
              preConditionsList, opConstraintsList, ocl2SMTGenerator, entry.getKey(), partial));
    }
    return res;
  }

  private Set<OCLOPWitness> oclWitnessHelper(
      List<IdentifiableBoolExpr> preConditionList,
      List<IdentifiableBoolExpr> opConstraints,
      OCL2SMTGenerator ocl2SMTGenerator,
      ASTOCLMethodSignature method,
      boolean partial) {
    Set<OCLOPWitness> res = new HashSet<>();
    Solver solver;
    for (IdentifiableBoolExpr pre : preConditionList) {
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

  public IdentifiableBoolExpr mkOperationConstraint(OCLConstraint oclConstraint, Context ctx) {
    if (oclConstraint.isInvariant()) {
      Log.info("", "Cannot make Operation Constraint with OCL Invariant Invariant");
    }
    BoolExpr opConstraint =
        ctx.mkImplies(
            oclConstraint.getPostCond().getValue(), oclConstraint.getPostCond().getValue());
    return IdentifiableBoolExpr.buildIdentifiable(
        opConstraint, oclConstraint.getPreCond().getSourcePosition(), Optional.of("pre ==> Post"));
  }

  public OCLOPDiffResult oclDiff(
      ASTCDCompilationUnit ast,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOcl,
      ASTOCLMethodSignature method ,
      boolean partial) {

    // setup
    ctx = buildContext();
    Set<OCLOPWitness> diffWitness  = new HashSet<>();
    List<ASTODLink> trace  = new ArrayList<>();
    OCL2SMTStrategy.buildPreCD(ast);
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(ast, ctx);

    List<OCLConstraint> newConstraints =  opConst2smt(ocl2SMTGenerator, sortOPConstraint(newOcl).get(method));
    List<OCLConstraint> oldConstraints =opConst2smt(ocl2SMTGenerator,sortOPConstraint(oldOcl).get(method));
    List<IdentifiableBoolExpr> posConstraints = newConstraints.stream().map(x->mkOperationConstraint(x,ctx))
            .collect(Collectors.toList());
    List<IdentifiableBoolExpr> negativeConstraints = new ArrayList<>() ;
    Solver solver ;
    List<IdentifiableBoolExpr> solverConstraints = new ArrayList<>(posConstraints);

    for (OCLConstraint oldConstraint : oldConstraints){
      solverConstraints.add(oldConstraint.getPreCond());
      posConstraints.add(oldConstraint.getPreCond());

      IdentifiableBoolExpr oldOpConstraint = mkOperationConstraint(oldConstraint,ctx).negate(ctx);
      solverConstraints.add(oldOpConstraint);
      negativeConstraints.add(oldOpConstraint);

      solver = ocl2SMTGenerator.makeSolver(posConstraints);

      if (solver.check() == Status.SATISFIABLE){
        diffWitness.add(ocl2SMTGenerator.buildOPOd(solver.getModel(),"Witness",method,partial));
      }else {
        trace.addAll(TraceUnsatCore.traceUnsatCore(solver));
      }
      posConstraints.remove(oldConstraint.getPreCond());
      posConstraints.remove(oldOpConstraint);
    }
    ASTODArtifact unSatCore = TraceUnsatCore.buildUnsatOD(posConstraints,negativeConstraints,trace);
   return  new OCLOPDiffResult(unSatCore,diffWitness);
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

  public boolean containsKey(
      Map<ASTOCLMethodSignature, List<ASTOCLOperationConstraint>> map,
      ASTOCLMethodSignature method) {
    return map.keySet().stream().anyMatch(key -> key.deepEquals(method));
  }

 /* protected OCLOPDiffResult olcDiffHelper(
      OCL2SMTGenerator ocl2SMTGenerator,
      List<IdentifiableBoolExpr> constraintList,
      ASTOCLMethodSignature method,
      boolean partial) {

    Set<OCLOPWitness> diffWitness = new HashSet<>();
    List<ASTODLink> traceUnSat = new ArrayList<>();

    Solver solver = ocl2SMTGenerator.makeSolver(constraintList);
    if (solver.check() == Status.SATISFIABLE) {
      Model model = solver.getModel();
      String invName = buildInvName(negConstraint);
      diffWitness.add(ocl2SMTGenerator.buildOPOd(model, invName, partial, method));
    } else {
      traceUnSat.addAll(TraceUnsatCore.traceUnsatCore(solver));
    }
    posConstraintList.remove(negConstraint);

    return new OCLOPDiffResult(
        TraceUnsatCore.buildUnsatOD(posConstraintList, negConstraintList, traceUnSat), diffWitness);
  }*/

  protected List<OCLConstraint> opConst2smt(
      OCL2SMTGenerator ocl2SMTGenerator, List<ASTOCLOperationConstraint> operationConstraints) {
    return operationConstraints.stream()
        .map(ocl2SMTGenerator::convertOpConst)
        .collect(Collectors.toList());
  }
}
