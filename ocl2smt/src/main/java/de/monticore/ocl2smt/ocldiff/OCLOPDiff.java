package de.monticore.ocl2smt.ocldiff;

import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTStrategy;
import de.monticore.ocl2smt.util.OCLConstraint;
import de.monticore.ocl2smt.util.OPDiffResult;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odlink._ast.ASTODLink;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class OCLOPDiff {

  protected static Context ctx;

  protected static OPDiffResult oclWitness( // TODO: Unsatcore when not Consistent
      ASTCDCompilationUnit ast, Set<ASTOCLCompilationUnit> in, boolean partial) {

    resetContext();
    OCL2SMTStrategy.buildPreCD(ast);
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(ast, ctx);

    Set<OCLConstraint> constraints = opConst2smt(ocl2SMTGenerator, in);

    // add the pre-condition
    List<IdentifiableBoolExpr> solversConstraints =
        constraints.stream()
            .filter(OCLConstraint::isOpConstraint)
            .map(OCLConstraint::getPreCond)
            .collect(Collectors.toList());

    // add the post condition
    solversConstraints.addAll(
        constraints.stream()
            .filter(OCLConstraint::isOpConstraint)
            .map(OCLConstraint::getPostCond)
            .collect(Collectors.toList()));

    // add the invariants
    solversConstraints.addAll(
        constraints.stream()
            .filter(OCLConstraint::isInvariant)
            .map(OCLConstraint::getInvariant)
            .collect(Collectors.toList()));

    Solver solver = ocl2SMTGenerator.getCD2SMTGenerator().makeSolver(solversConstraints);

    if (solver.check() != Status.SATISFIABLE) {
      Log.info("there are no Model for the List Of Positive Constraints", "NOWitnessOD");
      return null;
    }

    Model model = solver.getModel();
    Optional<ASTODArtifact> od = ocl2SMTGenerator.buildOd(model, "Witness", partial);
    assert od.isPresent();

    return OCL2SMTStrategy.splitPreOD(
        od.get(), model, ocl2SMTGenerator.getExpression2SMT().getConstrData());
  }

  public static Pair<ASTODArtifact, Set<OPDiffResult>> oclDiffOp(
      ASTCDCompilationUnit ast,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOcl,
      boolean partial) {
    resetContext();
    OCL2SMTStrategy.buildPreCD(ast);

    //  oclWitness(ast,posOcl,false) ;
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(ast, ctx);
    Set<OCLConstraint> oldConstr = opConst2smt(ocl2SMTGenerator, oldOcl);
    Set<OCLConstraint> newConstr = opConst2smt(ocl2SMTGenerator, newOcl);

    Set<IdentifiableBoolExpr> posConstraint = new HashSet<>();
    posConstraint.add(newConstr.iterator().next().getPreCond());
    posConstraint.add(newConstr.iterator().next().getPostCond());
    posConstraint.add(oldConstr.iterator().next().getPreCond());

    Set<IdentifiableBoolExpr> negConstraints = new HashSet<>();
    negConstraints.add(oldConstr.iterator().next().getPostCond().negate(ctx));

    return oclDiffOpHelper(ocl2SMTGenerator, posConstraint, negConstraints, partial);
  }

  protected static Pair<ASTODArtifact, Set<OPDiffResult>> oclDiffOpHelper(
      OCL2SMTGenerator ocl2SMTGenerator,
      Set<IdentifiableBoolExpr> posConstraintList,
      Set<IdentifiableBoolExpr> negConstList,
      boolean partial) {

    Set<OPDiffResult> satOdList = new HashSet<>();
    List<ASTODLink> traceUnsat = new ArrayList<>();

    // add one by one all Constraints to the Solver and check if  it can always produce a Model
    for (IdentifiableBoolExpr negConstraint : negConstList) {
      posConstraintList.add(negConstraint);
      Solver solver =
          ocl2SMTGenerator.getCD2SMTGenerator().makeSolver(new ArrayList<>(posConstraintList));

      if (solver.check() == Status.SATISFIABLE) {
        Model model = solver.getModel();
        String invName =
            negConstraint.getInvariantName().orElse("NoInvName").split("_____NegInv")[0];
        Optional<ASTODArtifact> od = ocl2SMTGenerator.buildOd(model, invName, partial);
        assert od.isPresent();

        satOdList.add(
            OCL2SMTStrategy.splitPreOD(
                od.get(), model, ocl2SMTGenerator.getExpression2SMT().getConstrData()));

      } else {
        traceUnsat.addAll(TraceUnsatCore.traceUnsatCore(solver));
      }
      posConstraintList.remove(negConstraint);
    }
    return new ImmutablePair<>(
        TraceUnsatCore.buildUnsatOD(posConstraintList, negConstList, traceUnsat), satOdList);
  }

  protected static Set<OCLConstraint> opConst2smt(
      OCL2SMTGenerator ocl2SMTGenerator, Set<ASTOCLCompilationUnit> in) {
    return in.stream()
        .flatMap(p -> ocl2SMTGenerator.ocl2smt(p.getOCLArtifact()).stream())
        .collect(Collectors.toSet());
  }

  public static void resetContext() {
    Map<String, String> cfg = new HashMap<>();
    cfg.put("model", "true");
    ctx = new Context(cfg);
  }
}
