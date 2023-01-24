/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocldiff.invariantDiff;

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
import de.monticore.ocl2smt.ocldiff.TraceUnsatCore;
import de.monticore.ocl2smt.util.OCLConstraint;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odlink._ast.ASTODLink;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.stream.Collectors;

public class OCLInvariantDiff {
  protected Context ctx;

  public ASTODArtifact oclWitness(
      ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> in, boolean partial) {
    resetContext();
    return oclWitnessInternal(cd, in, partial);
  }

  public OCLInvDiffResult oclDiff(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> in,
      Set<ASTOCLCompilationUnit> notIn,
      boolean partial) {
    resetContext();
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(cd, ctx);
    // check if the Model is consistent
    if (oclWitnessInternal(cd, in, false) == null) {
      Log.info("The Model PosCD + PosOCL is not Consistent", "[MODEl-INCONSISTENT]");
    }

    // positive ocl constraint
    Set<OCLConstraint> posConstList = OCLDiffHelper.invariant2SMT(ocl2SMTGenerator, in);

    // negative ocl constraints
    Set<OCLConstraint> negConstList = negate(OCLDiffHelper.invariant2SMT(ocl2SMTGenerator, notIn), ctx);

    return oclDiffHelper(ocl2SMTGenerator, posConstList, negConstList, partial);
  }

  public OCLInvDiffResult CDOCLDiff(
      ASTCDCompilationUnit posCd,
      ASTCDCompilationUnit negCd,
      Set<ASTOCLCompilationUnit> posOcl,
      Set<ASTOCLCompilationUnit> negOcl,
      boolean partial) {
    resetContext();
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(posCd, ctx);

    // list of positive OCl Constraints
    Set<OCLConstraint> posConstraints = OCLDiffHelper.invariant2SMT(ocl2SMTGenerator, posOcl);

    // list of negative OCL Constraints
    CD2SMTGenerator cd2SMTGenerator = new CD2SMTGenerator();
    Set<OCLConstraint> negConstraints =
        negate(OCLDiffHelper.invariant2SMT(ocl2SMTGenerator, negOcl), ctx);
    cd2SMTGenerator.cd2smt(negCd, ctx);
    negConstraints.addAll(negate(cd2SMTGenerator.getAssociationsConstraints(), ctx));

    CDHelper.removeAssocCard(posCd);
    CDHelper.removeAssocCard(negCd);

    List<ASTODArtifact> res =
        CDDiff.computeAlloySemDiff(
            posCd,
            negCd,
            CDDiff.getDefaultDiffsize(negCd, posCd),
            5,
            CDSemantics.SIMPLE_CLOSED_WORLD);
    if (!res.isEmpty()) {
      return new OCLInvDiffResult(null, new HashSet<>(res));
    }
    return oclDiffHelper(ocl2SMTGenerator, posConstraints, negConstraints, partial);
  }

  private ASTODArtifact oclWitnessInternal(
      ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> in, boolean partial) {

    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(cd, ctx);

    Set<OCLConstraint> solverConstraints = OCLDiffHelper.invariant2SMT(ocl2SMTGenerator, in);

    // check if they exist a model for the list of positive Constraint
    Solver solver =
        ocl2SMTGenerator.getCD2SMTGenerator().makeSolver(new ArrayList<>(solverConstraints));
    if (solver.check() != Status.SATISFIABLE) {
      Log.error("there are no Model for the List Of Positive Constraints");
    }

    return ocl2SMTGenerator.buildOd(solver.getModel(), "Witness", partial).orElse(null);
  }

  protected OCLInvDiffResult oclDiffHelper(
      OCL2SMTGenerator ocl2SMTGenerator,
      Set<OCLConstraint> newConstraintSet,
      Set<OCLConstraint> oldConstraintsSet,
      boolean partial) {

    Set<ASTODArtifact> satOdList = new HashSet<>();
    List<ASTODLink> traceUnSat = new ArrayList<>();

    // add one by one all Constraints to the Solver and check if  it can always produce a Model
    for (OCLConstraint negConstraint : oldConstraintsSet) {
      newConstraintSet.add(negConstraint);
      Solver solver =
          ocl2SMTGenerator.getCD2SMTGenerator().makeSolver(OCLDiffHelper.extractInv(newConstraintSet));

      if (solver.check() == Status.SATISFIABLE) {
        String invName =
            negConstraint.getInvariant().getInvariantName().orElse("NoInvName").split("_____NegInv")[0];
        satOdList.add(ocl2SMTGenerator.buildOd(solver.getModel(), invName, partial).get());
      } else {
        traceUnSat.addAll(TraceUnsatCore.traceUnsatCore(solver));
      }
      newConstraintSet.remove(negConstraint);
    }
    return new OCLInvDiffResult(
        TraceUnsatCore.buildUnsatOD(newConstraintSet, oldConstraintsSet, traceUnSat), satOdList);
  }





  public void resetContext() {
    Map<String, String> cfg = new HashMap<>();
    cfg.put("model", "true");
    ctx = new Context(cfg);
  }
}
