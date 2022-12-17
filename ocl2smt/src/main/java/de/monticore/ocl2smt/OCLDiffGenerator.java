package de.monticore.ocl2smt;

import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cddiff.CDDiff;
import de.monticore.cddiff.alloycddiff.CDSemantics;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odlink._ast.ASTODLink;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class OCLDiffGenerator {
  protected static Context ctx;

  public static ASTODArtifact oclWitness(
      ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> in, boolean partial) {
    resetContext();
    return oclWitnessInternal(cd, in, partial);
  }

  private static ASTODArtifact oclWitnessInternal(
      ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> in, boolean partial) {

    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(cd, ctx);

    Set<IdentifiableBoolExpr> solverConstraints = buildSmtBoolExpr(ocl2SMTGenerator, in);

    // check if they exist a model for the list of positive Constraint
    Solver solver = ocl2SMTGenerator.cd2smtGenerator.makeSolver(new ArrayList<>(solverConstraints));
    if (solver.check() != Status.SATISFIABLE) {
      Log.error("there are no Model for the List Of Positive Constraints");
    }

    return buildOd(ocl2SMTGenerator.cd2smtGenerator, solver.getModel(), "Witness", partial)
        .orElse(null);
  }

  public static Pair<ASTODArtifact, Set<ASTODArtifact>> oclDiff(
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
    ;
    // positive ocl constraint
    Set<IdentifiableBoolExpr> posConstList = buildSmtBoolExpr(ocl2SMTGenerator, in);

    // negative ocl constraints
    Set<IdentifiableBoolExpr> negConstList = negate(buildSmtBoolExpr(ocl2SMTGenerator, notIn), ctx);

    return oclDiffHelper(ocl2SMTGenerator, posConstList, negConstList, partial);
  }

  public static Pair<ASTODArtifact, Set<ASTODArtifact>> CDOCLDiff(
      ASTCDCompilationUnit posCd,
      ASTCDCompilationUnit negCd,
      Set<ASTOCLCompilationUnit> posOcl,
      Set<ASTOCLCompilationUnit> negOcl,
      boolean partial) {
    resetContext();
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(posCd, ctx);

    // list of positive OCl Constraints
    Set<IdentifiableBoolExpr> posConstraints = buildSmtBoolExpr(ocl2SMTGenerator, posOcl);

    // list of negative OCL Constraints
    CD2SMTGenerator cd2SMTGenerator = new CD2SMTGenerator();
    Set<IdentifiableBoolExpr> negConstraints =
        negate(buildSmtBoolExpr(ocl2SMTGenerator, negOcl), ctx);
    cd2SMTGenerator.cd2smt(negCd, ctx);
    negConstraints.addAll(negate(cd2SMTGenerator.getAssociationsConstraints(), ctx));

    CDHelper.removeAssocCard(posCd);
    CDHelper.removeAssocCard(negCd);

    List<ASTODArtifact> res =
        CDDiff.computeAlloySemDiff(
            posCd,
            negCd,
            CDDiff.getDefaultDiffsize(negCd, posCd),
            1,
            CDSemantics.SIMPLE_CLOSED_WORLD);
    if (!res.isEmpty()) {
      return new ImmutablePair<>(null, new HashSet<>(res));
    }
    return oclDiffHelper(ocl2SMTGenerator, posConstraints, negConstraints, partial);
  }

  public static Pair<ASTODArtifact, Set<ASTODArtifact>> oclDiff(
      ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> in, Set<ASTOCLCompilationUnit> notIn) {
    return oclDiff(cd, in, notIn, false);
  }

  private static Pair<ASTODArtifact, Set<ASTODArtifact>> oclDiffHelper(
      OCL2SMTGenerator ocl2SMTGenerator,
      Set<IdentifiableBoolExpr> posConstraintList,
      Set<IdentifiableBoolExpr> negConstList,
      boolean partial) {

    Set<ASTODArtifact> satOdList = new HashSet<>();
    List<ASTODLink> traceUnsat = new ArrayList<>();

    // add one by one all Constraints to the Solver and check if  it can always produce a Model
    for (IdentifiableBoolExpr negConstraint : negConstList) {
      posConstraintList.add(negConstraint);
      Solver solver =
          ocl2SMTGenerator.cd2smtGenerator.makeSolver(new ArrayList<>(posConstraintList));

      if (solver.check() == Status.SATISFIABLE) {
        satOdList.add(
            buildOd(
                    ocl2SMTGenerator.cd2smtGenerator,
                    solver.getModel(),
                    negConstraint.getInvariantName().orElse("NoInvName").split("_____NegInv")[0],
                    partial)
                .get());
      } else {
        traceUnsat.addAll(TraceUnsatCore.traceUnsatCore(solver));
      }
      posConstraintList.remove(negConstraint);
    }
    return new ImmutablePair<>(
        TraceUnsatCore.buildUnsatOD(posConstraintList, negConstList, traceUnsat), satOdList);
  }

  protected static Optional<ASTODArtifact> buildOd(
      CD2SMTGenerator cd2SMTGenerator, Model model, String ODName, boolean partial) {
    return cd2SMTGenerator.smt2od(model, partial, ODName);
  }

  private static Set<IdentifiableBoolExpr> buildSmtBoolExpr(
      OCL2SMTGenerator ocl2SMTGenerator, Set<ASTOCLCompilationUnit> in) {
    return in.stream()
        .flatMap(p -> ocl2SMTGenerator.ocl2smt(p.getOCLArtifact()).stream())
        .collect(Collectors.toSet());
  }

  private static Set<IdentifiableBoolExpr> negate(
      Set<IdentifiableBoolExpr> constraints, Context ctx) {
    return constraints.stream().map(x -> x.negate(ctx)).collect(Collectors.toSet());
  }

  public static void resetContext() {
    Map<String, String> cfg = new HashMap<>();
    cfg.put("model", "true");
    OCLDiffGenerator.ctx = new Context(cfg);
  }
}
