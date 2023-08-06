/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocldiff.invariantDiff;

import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cddiff.CDDiff;
import de.monticore.cddiff.alloycddiff.CDSemantics;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.ocl2smt.ocldiff.TraceUnSatCore;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odlink._ast.ASTODLink;
import de.se_rwth.commons.logging.Log;

import java.util.*;
import java.util.stream.Collectors;

public class OCLInvariantDiff {
  protected Context ctx;

  public ASTODArtifact oclWitness(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> ocl,
      Set<IdentifiableBoolExpr> additionalConstraints,
      Context ctx,
      boolean partial) {
    this.ctx = ctx;
    return oclWitnessInternal(cd, ocl, additionalConstraints, partial);
  }

  public OCLInvDiffResult oclDiff(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOcl,
      Set<IdentifiableBoolExpr> additionalConstraints,
      Context ctx,
      boolean partial) {
    this.ctx = ctx;
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(cd, ctx);
    // check if the Model is consistent
    if (oclWitnessInternal(cd, newOcl, new HashSet<>(additionalConstraints), false) == null) {
      Log.info("The Model PosCD + PosOCL is not Consistent", "[MODEl-INCONSISTENT]");
    } // FIXME: 05.06.2023 is always true

    // add new ocl Constraint
    List<IdentifiableBoolExpr> newConstraints = invariant2SMT(ocl2SMTGenerator, newOcl);
    newConstraints.addAll(additionalConstraints);
    // negate and add old ocl constraints
    List<IdentifiableBoolExpr> oldConstraints = invariant2SMT(ocl2SMTGenerator, oldOcl);

    List<IdentifiableBoolExpr> negConstraints = negateId(oldConstraints, ctx);
    return oclDiffHelper(ocl2SMTGenerator, newConstraints, negConstraints, partial);
  }

  public OCLInvDiffResult CDOCLDiff(
      ASTCDCompilationUnit oldCD,
      ASTCDCompilationUnit newCD,
      Set<ASTOCLCompilationUnit> oldOCl,
      Set<ASTOCLCompilationUnit> newOCL,
      Set<IdentifiableBoolExpr> additionalConstraints,
      Context ctx,
      boolean partial) {

    this.ctx = ctx;
    OCL2SMTGenerator ocl2SMTGenerator =
        new OCL2SMTGenerator(newCD, ctx); // Fixme: translate both classdiagram

    // list of new OCl Constraints
    List<IdentifiableBoolExpr> newOClConstraints = invariant2SMT(ocl2SMTGenerator, newOCL);
    newOClConstraints.addAll(additionalConstraints);
    // list of old OCL Constraints
    List<IdentifiableBoolExpr> oldOCLConstraints = invariant2SMT(ocl2SMTGenerator, oldOCl);

    CD2SMTGenerator cd2SMTGenerator = CD2SMTMill.cd2SMTGenerator();
    cd2SMTGenerator.cd2smt(oldCD, ctx);
    List<IdentifiableBoolExpr> oldAssocConstr =
        new ArrayList<>(cd2SMTGenerator.getAssociationsConstraints());

    // remove assoc cardinality and compute CDDiff
    ASTCDCompilationUnit oldCDClone = oldCD.deepClone();
    ASTCDCompilationUnit newCDClone = newCD.deepClone();
    CDHelper.removeAssocCard(oldCDClone);
    CDHelper.removeAssocCard(newCDClone);
    List<ASTODArtifact> res =
        CDDiff.computeAlloySemDiff(
            newCDClone,
            oldCDClone,
            CDDiff.getDefaultDiffsize(newCDClone, oldCDClone),
            1,
            CDSemantics.SIMPLE_CLOSED_WORLD);
    if (!res.isEmpty()) {
      return new OCLInvDiffResult(null, new HashSet<>(res));
    }

    // build positive constraint List
    List<IdentifiableBoolExpr> posConstraint = new ArrayList<>(newOClConstraints);

    // build negative constraintList
    List<IdentifiableBoolExpr> negConstraint = negateId(oldOCLConstraints, ctx);
    negConstraint.addAll(negateId(oldAssocConstr, ctx));

    return oclDiffHelper(ocl2SMTGenerator, posConstraint, negConstraint, partial);
  }

  protected ASTODArtifact oclWitnessInternal(
          ASTCDCompilationUnit cd,
          Set<ASTOCLCompilationUnit> in,
          Set<IdentifiableBoolExpr> additionalConstraints,
          boolean partial) {

    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(cd, ctx);

    List<IdentifiableBoolExpr> solverConstraints = invariant2SMT(ocl2SMTGenerator, in);
    solverConstraints.addAll(additionalConstraints);
    // check if they exist a model for the list of positive Constraint
    Solver solver = ocl2SMTGenerator.makeSolver(solverConstraints);
    if (solver.check() != Status.SATISFIABLE) {
      solverConstraints.addAll(ocl2SMTGenerator.getCD2SMTGenerator().getAssociationsConstraints());
      solverConstraints.addAll(ocl2SMTGenerator.getCD2SMTGenerator().getInheritanceConstraints());

      return TraceUnSatCore.buildUnSatOD(
              solverConstraints, new ArrayList<>(), TraceUnSatCore.traceUnSatCoreWitness(solver));
      // Log.error("there is no Model for the List Of Positive Constraints");
    }

    return ocl2SMTGenerator.buildOd(solver.getModel(), "Witness", partial).orElse(null);
  }

  protected OCLInvDiffResult oclDiffHelper(
      OCL2SMTGenerator ocl2SMTGenerator,
      List<IdentifiableBoolExpr> posConstraintList,
      List<IdentifiableBoolExpr> negConstraintList,
      boolean partial) {

    Set<ASTODArtifact> satOdList = new HashSet<>();
    List<ASTODLink> traceUnSat = new ArrayList<>();

    // add one by one all Constraints to the Solver and check if it can always produce a Model
    for (IdentifiableBoolExpr negConstraint : negConstraintList) {
      posConstraintList.add(negConstraint);
      Solver solver = ocl2SMTGenerator.makeSolver(posConstraintList);

      if (solver.check() == Status.SATISFIABLE) {
        String name = buildInvName(negConstraint);
        Optional<ASTODArtifact> witness =
            ocl2SMTGenerator.buildOd(solver.getModel(), name, partial);

        assert witness.isPresent();
        satOdList.add(witness.get());
      } else {
        traceUnSat.addAll(TraceUnSatCore.traceUnSatCore(solver));
      }
      posConstraintList.remove(negConstraint);
    }
    return new OCLInvDiffResult(
        TraceUnSatCore.buildUnSatOD(posConstraintList, negConstraintList, traceUnSat), satOdList);
  }

  public String buildInvName(IdentifiableBoolExpr constr) {
    return constr.getInvariantName().orElse("NoInvName").split("_____NegInv")[0];
  }

  public static List<IdentifiableBoolExpr> negateId(
      List<IdentifiableBoolExpr> constraints, Context ctx) {
    return constraints.stream().map(x -> x.negate(ctx)).collect(Collectors.toList());
  }

  public static List<IdentifiableBoolExpr> invariant2SMT(
      OCL2SMTGenerator ocl2SMTGenerator, Set<ASTOCLCompilationUnit> oclSet) {
    return oclSet.stream()
        .flatMap(x -> ocl2SMTGenerator.inv2smt(x.getOCLArtifact()).stream())
        .collect(Collectors.toList());
  }
}
