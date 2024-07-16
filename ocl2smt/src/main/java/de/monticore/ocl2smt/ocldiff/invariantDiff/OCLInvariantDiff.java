/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocldiff.invariantDiff;

import static de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy.Strategy.*;

import com.microsoft.z3.Context;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cddiff.CDDiff;
import de.monticore.cddiff.alloycddiff.CDSemantics;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OCLInvariantDiff implements OCLInvDiffStrategy {

  @Override
  public ASTODArtifact oclWitness(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> ocl,
      Set<IdentifiableBoolExpr> additionalConstraints,
      Context ctx,
      int timeout,
      boolean partial) {

    ASTODArtifact od = oclWitnessInternal(cd, ocl, additionalConstraints, ctx, timeout, partial);

    if (od != null) {
      Log.info(": Witness found", this.getClass().getSimpleName());
    } else {
      Log.info(": Witness not found", this.getClass().getSimpleName());
    }

    return od;
  }

  @Override
  public OCLInvDiffResult oclInvDiff(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> newConstr,
      ASTOCLInvariant inv,
      Set<IdentifiableBoolExpr> addConstr,
      Context ctx,
      int timeout,
      boolean partial) {
    String invName = inv.isPresentName() ? inv.getName() : "";
    Log.println("\n");

    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(cd, ctx);
    if (checkConsistency(cd, newConstr, addConstr, ctx, timeout) != Status.UNSATISFIABLE) {
      List<IdentifiableBoolExpr> newConstraints = invariant2SMT(ocl2SMTGenerator, newConstr);
      newConstraints.addAll(addConstr);

      List<IdentifiableBoolExpr> oldConstr = List.of(ocl2SMTGenerator.convertInv(inv));
      List<IdentifiableBoolExpr> negConstraints = negateId(oldConstr, ctx);

      return computeDiff(ocl2SMTGenerator, newConstraints, negConstraints, timeout, partial);
    }

    return new OCLInvDiffResult(null, new HashSet<>());
  }

  public OCLInvDiffResult CDOCLDiff(
      ASTCDCompilationUnit oldCD,
      ASTCDCompilationUnit newCD,
      Set<ASTOCLCompilationUnit> oldOCl,
      Set<ASTOCLCompilationUnit> newOCL,
      Set<IdentifiableBoolExpr> additionalConstraints,
      Context ctx,
      boolean partial) {

    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(newCD, ctx);

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

    return computeDiff(ocl2SMTGenerator, posConstraint, negConstraint, Integer.MAX_VALUE, partial);
  }
}
