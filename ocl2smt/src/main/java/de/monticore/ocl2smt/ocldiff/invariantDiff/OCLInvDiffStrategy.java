package de.monticore.ocl2smt.ocldiff.invariantDiff;

import com.microsoft.z3.Context;
import com.microsoft.z3.Params;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.ocl2smt.ocldiff.TraceUnSatCore;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odlink._ast.ASTODLink;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.stream.Collectors;

public interface OCLInvDiffStrategy {
  /***
   * compute a witness object diagram that is a legal instance of a CD and satisfies OCL
   * and additional constraints.
   * @param timeout the timeout for the computation.
   */
  ASTODArtifact oclWitness(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> ocl,
      Set<IdentifiableBoolExpr> additionalConstraints,
      Context ctx,
      int timeout,
      boolean partial);
  /***
   * compute a witness object diagram that is a legal instance of a cd and satisfies the ocl
   * and the additional constraints.
   */
  default ASTODArtifact oclWitness(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> ocl,
      Set<IdentifiableBoolExpr> additionalConstraints,
      Context ctx,
      boolean partial) {
    return oclWitness(cd, ocl, additionalConstraints, ctx, Integer.MAX_VALUE, partial);
  }

  /***
   * compute the diff between the new ocl constraints and the old ones
   */
  default OCLInvDiffResult oclDiff(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOcl,
      Set<IdentifiableBoolExpr> addConstr,
      Context ctx,
      boolean partial) {

    return oclDiff(cd, oldOcl, newOcl, addConstr, ctx, Integer.MAX_VALUE, partial);
  }

  /***
   * compute the diff between the new ocl constraints and the old ones
   * @param timeout the time-out for the computation.
   */
  default OCLInvDiffResult oclDiff(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOcl,
      Set<IdentifiableBoolExpr> addConstr,
      Context ctx,
      int timeout,
      boolean partial) {
    // collect invariants
    List<ASTOCLInvariant> invariantList = OCLHelper.collectInv(oldOcl);

    // compute diff witness for each invariant
    Set<OCLInvDiffResult> diffs = new HashSet<>();
    invariantList.forEach(
        inv -> diffs.add(oclInvDiff(cd, newOcl, inv, addConstr, ctx, timeout, partial)));

    // merge different diff
    List<ASTODLink> traces = new ArrayList<>();
    Set<ASTODArtifact> witnesses = new HashSet<>();
    ASTODArtifact unSatCore = null;

    for (OCLInvDiffResult diff : diffs) {
      if (diff.getUnSatCore() != null) {
        traces.addAll(OCLHelper.getLinkList(diff.getUnSatCore()));
        unSatCore = diff.getUnSatCore();
      }
      witnesses.addAll(diff.getDiffWitness());
    }

    if (unSatCore != null) {
      unSatCore.getObjectDiagram().addAllODElements(traces);
    }
    return new OCLInvDiffResult(unSatCore, witnesses);
  }

  /***
   * compute a diff between a single new ocl invariant and the old ones.
   * @param  timeout for the diff
   */
  OCLInvDiffResult oclInvDiff(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> newConstr,
      ASTOCLInvariant inv,
      Set<IdentifiableBoolExpr> addConstr,
      Context ctx,
      int timeout,
      boolean partial);

  /***
   * compute a diff between a single new ocl invariant and the old ones
   */
  default OCLInvDiffResult oclInvDiff(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> newConstr,
      ASTOCLInvariant inv,
      Set<IdentifiableBoolExpr> addConstr,
      Context ctx,
      boolean partial) {
    return oclInvDiff(cd, newConstr, inv, addConstr, ctx, Integer.MAX_VALUE, partial);
  }

  default ASTODArtifact oclWitnessInternal(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> in,
      Set<IdentifiableBoolExpr> additionalConstraints,
      Context ctx,
      int timeout,
      boolean partial) {

    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(cd, ctx);

    List<IdentifiableBoolExpr> solverConstraints = invariant2SMT(ocl2SMTGenerator, in);
    solverConstraints.addAll(additionalConstraints);

    Solver solver = ocl2SMTGenerator.makeSolver(solverConstraints);
    setTimeout(solver, ctx, timeout);

    if (solver.check() == Status.SATISFIABLE) {
      return ocl2SMTGenerator.buildOd(solver.getModel(), "Witness", partial).orElse(null);
    }

    return null;
  }

  /*** compute the diff between two sets of ocl invariants.*/
  default OCLInvDiffResult computeDiff(
      OCL2SMTGenerator ocl2SMTGenerator,
      List<IdentifiableBoolExpr> posConstraintList,
      List<IdentifiableBoolExpr> negConstraintList,
      int timeout,
      boolean partial) {

    Set<ASTODArtifact> satOdList = new HashSet<>();
    List<ASTODLink> traceUnSat = new ArrayList<>();

    // add one by one all Constraints to the Solver and check if it can always produce a Model
    for (IdentifiableBoolExpr negConstraint : negConstraintList) {

      posConstraintList.add(negConstraint);
      Solver solver = ocl2SMTGenerator.makeSolver(posConstraintList);
      setTimeout(solver, ocl2SMTGenerator.getCtx(), timeout);
      Status status = solver.check();
      if (status == Status.SATISFIABLE) {
        Log.info("[SAT]The invariant is not refined by the new model", this.getClass().getName());
        String name = negConstraint.getInvariantName().orElse("NoInvName").split("_____NegInv")[0];

        Optional<ASTODArtifact> witness =
            ocl2SMTGenerator.buildOd(solver.getModel(), name, partial);

        assert witness.isPresent();
        satOdList.add(witness.get());
      } else if (status == Status.UNKNOWN) {
        return new OCLInvDiffResult(null, new HashSet<>());
      } else {
        Log.info("[UNSAT]The invariant is refined by the new model", this.getClass().getName());
        traceUnSat.addAll(TraceUnSatCore.traceUnSatCore(solver));
      }
      posConstraintList.remove(negConstraint);
    }
    return new OCLInvDiffResult(
        TraceUnSatCore.buildUnSatOD(posConstraintList, negConstraintList, traceUnSat), satOdList);
  }

  default List<IdentifiableBoolExpr> negateId(List<IdentifiableBoolExpr> constraints, Context ctx) {
    return constraints.stream().map(x -> x.negate(ctx)).collect(Collectors.toList());
  }

  default List<IdentifiableBoolExpr> invariant2SMT(
      OCL2SMTGenerator ocl2SMTGenerator, Set<ASTOCLCompilationUnit> oclSet) {
    return oclSet.stream()
        .flatMap(x -> ocl2SMTGenerator.inv2smt(x.getOCLArtifact()).stream())
        .collect(Collectors.toList());
  }

  default Status checkConsistency(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> newOCL,
      Set<IdentifiableBoolExpr> addConstr,
      Context ctx,
      int timeout) {

    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(cd, ctx);
    List<IdentifiableBoolExpr> solverConstraints = invariant2SMT(ocl2SMTGenerator, newOCL);
    solverConstraints.addAll(addConstr);
    Solver solver = ocl2SMTGenerator.makeSolver(solverConstraints);
    setTimeout(solver, ctx, timeout);

    return solver.check();
  }

  default void setTimeout(Solver solver, Context ctx, int timeout) {
    if (timeout <= 0) {
      Log.error("Time out must be greater than 0");
    }

    if (timeout < Integer.MAX_VALUE) {
      Params params = ctx.mkParams();
      params.add("timeout", timeout);
      solver.setParameters(params);
    }
  }
}
