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
        constraints.stream().map(OCLConstraint::getPostCond).collect(Collectors.toList()));

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
      Set<ASTOCLCompilationUnit> posOcl,
      Set<ASTOCLCompilationUnit> negOcl,
      boolean partial) {
    resetContext();
    OCL2SMTStrategy.buildPreCD(ast);

    //  oclWitness(ast,posOcl,false) ;
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(ast, ctx);
    Set<OCLConstraint> constraint1 = opConst2smt(ocl2SMTGenerator, posOcl);
    Set<OCLConstraint> constraint2 = opConst2smt(ocl2SMTGenerator, negOcl);

    Set<IdentifiableBoolExpr> posConstraint = new HashSet<>();
    posConstraint.add(constraint1.iterator().next().getPreCond());
    posConstraint.add(constraint1.iterator().next().getPostCond());
    posConstraint.add(constraint2.iterator().next().getPreCond());

    Set<IdentifiableBoolExpr> negConstraints = new HashSet<>();
    negConstraints.add(constraint2.iterator().next().getPostCond().negate(ctx));

    return diff2OPDiff(
        OCLDiffGenerator.oclDiffHelper(ocl2SMTGenerator, posConstraint, negConstraints, partial),
        ocl2SMTGenerator);
  }

  public static Pair<ASTODArtifact, Set<OPDiffResult>> diff2OPDiff(
      Pair<ASTODArtifact, Set<ASTODArtifact>> diff, OCL2SMTGenerator ocl2SMTGenerator) {
    Set<OPDiffResult> diffWitness = new HashSet<>();
    for (ASTODArtifact element : diff.getRight()) {
      // diffWitness.add(ocl2SMTGenerator.splitPreOD(element));
    }

    return new ImmutablePair<>(diff.getLeft(), diffWitness);
  }

  protected static Set<OCLConstraint> opConst2smt(
      OCL2SMTGenerator ocl2SMTGenerator, Set<ASTOCLCompilationUnit> in) {
    return in.stream()
        .flatMap(
            p ->
                ocl2SMTGenerator.ocl2smt(p.getOCLArtifact()).stream()
                    .filter(OCLConstraint::isOpConstraint))
        .collect(Collectors.toSet());
  }

  public static void resetContext() {
    Map<String, String> cfg = new HashMap<>();
    cfg.put("model", "true");
    ctx = new Context(cfg);
  }
}
