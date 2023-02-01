/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocldiff.invariantDiff;

import static de.monticore.ocl2smt.ocldiff.OCLDiffHelper.*;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cddiff.CDDiff;
import de.monticore.cddiff.alloycddiff.CDSemantics;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.ocl2smt.ocldiff.OCLDiffHelper;
import de.monticore.ocl2smt.ocldiff.TraceUnSatCore;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odlink._ast.ASTODLink;
import de.se_rwth.commons.logging.Log;
import java.util.*;

public class OCLInvariantDiff {
  protected Context ctx;

  public ASTODArtifact oclWitness(
      ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> ocl, boolean partial) {
    ctx = buildContext();
    return oclWitnessInternal(cd, ocl, partial);
  }

  public OCLInvDiffResult oclDiff(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOcl,
      boolean partial) {
    ctx = buildContext();
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(cd, ctx);
    // check if the Model is consistent
    if (oclWitnessInternal(cd, newOcl, false) == null) {
      Log.info("The Model PosCD + PosOCL is not Consistent", "[MODEl-INCONSISTENT]");
    }

    // add new ocl Constraint
    List<IdentifiableBoolExpr> newConstraints = invariant2SMT(ocl2SMTGenerator, newOcl);

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
      boolean partial) {

    ctx = OCLDiffHelper.buildContext();
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(newCD, ctx);

    // list of new OCl Constraints
    List<IdentifiableBoolExpr> newOClConstraints = invariant2SMT(ocl2SMTGenerator, newOCL);

    // list of old OCL Constraints
    List<IdentifiableBoolExpr> oldOCLConstraints = invariant2SMT(ocl2SMTGenerator, oldOCl);

    CD2SMTGenerator cd2SMTGenerator = new CD2SMTGenerator();
    cd2SMTGenerator.cd2smt(oldCD, ctx);
    List<IdentifiableBoolExpr> oldAssocConstr =
        new ArrayList<>(cd2SMTGenerator.getAssociationsConstraints());

    // remove assoc cardinality and compute CDDiff
    CDHelper.removeAssocCard(oldCD);
    CDHelper.removeAssocCard(oldCD);
    List<ASTODArtifact> res =
        CDDiff.computeAlloySemDiff(
            oldCD,
            newCD,
            CDDiff.getDefaultDiffsize(oldCD, oldCD),
            5,
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

  private ASTODArtifact oclWitnessInternal(
      ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> in, boolean partial) {

    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(cd, ctx);

    List<IdentifiableBoolExpr> solverConstraints = invariant2SMT(ocl2SMTGenerator, in);

    // check if they exist a model for the list of positive Constraint
    Solver solver = ocl2SMTGenerator.makeSolver(solverConstraints);
    if (solver.check() != Status.SATISFIABLE) {
      Log.error("there are no Model for the List Of Positive Constraints");
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

    // add one by one all Constraints to the Solver and check if  it can always produce a Model
    for (IdentifiableBoolExpr negConstraint : negConstraintList) {
      posConstraintList.add(negConstraint);
      Solver solver = ocl2SMTGenerator.makeSolver(posConstraintList);

      if (solver.check() == Status.SATISFIABLE) {
        String name = OCLDiffHelper.buildInvName(negConstraint);
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
}
