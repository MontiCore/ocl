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

  protected static OCL2SMTGenerator ocl2SMTGenerator;

  protected static List<IdentifiableBoolExpr> buildSmtBoolExpr(Set<ASTOCLCompilationUnit> in) {
    return in.stream()
        .flatMap(p -> ocl2SMTGenerator.ocl2smt(p.getOCLArtifact()).stream())
        .collect(Collectors.toList());
  }

  public static ASTODArtifact oclWitness(
      ASTCDCompilationUnit astcd, Set<ASTOCLCompilationUnit> in, boolean partial) {
    setOCL2SMTGenerator(astcd);
    List<IdentifiableBoolExpr> solverConstraints = buildSmtBoolExpr(in);

    // check if they exist a model for the list of positive Constraint
    Solver solver = ocl2SMTGenerator.cd2smtGenerator.makeSolver(solverConstraints);
    if (solver.check() != Status.SATISFIABLE) {
      Log.error("there are no Model for the List Of Positive Constraints");
    }

    return buildOd(solver.getModel(), "Witness", partial).get();
  }

  public static ASTODArtifact oclWitness(ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> in) {
    return oclWitness(cd, in, false);
  }

  private static Pair<ASTODArtifact, Set<ASTODArtifact>> oclDiffHelper(
      List<IdentifiableBoolExpr> posConstraintList,
      List<IdentifiableBoolExpr> negConstList,
      boolean partial) {

    Set<ASTODArtifact> satOdList = new HashSet<>();
    List<ASTODLink> traceUnsat = new ArrayList<>();

    // add one by one all Constraints to the Solver and check if  it can always produce a Model
    for (IdentifiableBoolExpr negConstraint : negConstList) {
      posConstraintList.add(negConstraint);
      Solver solver = ocl2SMTGenerator.cd2smtGenerator.makeSolver(posConstraintList);

      if (solver.check() == Status.SATISFIABLE) {
        satOdList.add(
            buildOd(
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

  public static Pair<ASTODArtifact, Set<ASTODArtifact>> oclDiff(
      ASTCDCompilationUnit astcd,
      Set<ASTOCLCompilationUnit> in,
      Set<ASTOCLCompilationUnit> notIn,
      boolean partial) {
    // check if the Model is Satisfiable
    oclWitness(astcd, in, false);
    // positive ocl constraint
    List<IdentifiableBoolExpr> solverConstraints = buildSmtBoolExpr(in);

    // negative ocl constraints
    List<IdentifiableBoolExpr> negConstList = new ArrayList<>();
    buildSmtBoolExpr(notIn)
        .forEach(
            idf -> negConstList.add(idf.negate(ocl2SMTGenerator.cd2smtGenerator.getContext())));

    return oclDiffHelper(solverConstraints, negConstList, partial);
  }

  public static Pair<ASTODArtifact, Set<ASTODArtifact>> CDOCLDiff(
      ASTCDCompilationUnit ast1,
      ASTCDCompilationUnit ast2,
      Set<ASTOCLCompilationUnit> in,
      Set<ASTOCLCompilationUnit> notIn,
      boolean partial) {

    setOCL2SMTGenerator(ast1);

    CD2SMTGenerator cd2SMTGenerator = new CD2SMTGenerator();
    final Context ctx = ocl2SMTGenerator.cd2smtGenerator.getContext();

    // lit of positive OCl Constraints
    List<IdentifiableBoolExpr> posConstraints = buildSmtBoolExpr(in);
    cd2SMTGenerator.cd2smt(ast1, ctx);
    posConstraints.addAll(cd2SMTGenerator.getAssociationsConstraints());

    // lists of negative ICL Constraints
    List<IdentifiableBoolExpr> negConstraints = buildSmtBoolExpr(notIn);
    cd2SMTGenerator.cd2smt(ast1, ctx);
    negConstraints.addAll(cd2SMTGenerator.getAssociationsConstraints());

    //CDHelper.removeAssocCard(ast1);
   // CDHelper.removeAssocCard(ast2);
    List<ASTODArtifact> res =
        CDDiff.computeAlloySemDiff(ast1, ast2, 20, 1, CDSemantics.SIMPLE_CLOSED_WORLD);
    if (!res.isEmpty()) {
      Log.info("the Both Class Diagram have A semantic Difference", "CDDiff");
      return new ImmutablePair<>(null, new HashSet<>(res));
    }

    return oclDiffHelper(posConstraints, negConstraints, partial);
  }

  public static Pair<ASTODArtifact, Set<ASTODArtifact>> oclDiff(
      ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> in, Set<ASTOCLCompilationUnit> notIn) {
    return oclDiff(cd, in, notIn, false);
  }

  protected static Optional<ASTODArtifact> buildOd(Model model, String ODName, boolean partial) {
    return ocl2SMTGenerator.cd2smtGenerator.smt2od(model, partial, ODName);
  }

  private static void setOCL2SMTGenerator(ASTCDCompilationUnit ast) {
    ocl2SMTGenerator = new OCL2SMTGenerator(ast);
  }
}
