package de.monticore.ocl2smt.ocldiff;

import static de.monticore.ocl2smt.ocldiff.OCLDiffGenerator.buildOd;

import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.helpers.OCLCDHelper;
import de.monticore.ocl2smt.helpers.OCLODHelper;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.ocl2smt.util.OCLConstraint;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class OCLOPDiff {

  protected static Context ctx;

  protected static Pair<ASTODArtifact, ASTODArtifact> oclWitness(
      ASTCDCompilationUnit ast, Set<ASTOCLCompilationUnit> in, boolean partial) {

    resetContext();
    OCLCDHelper.buildPreCD(ast);
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(ast, ctx);

    Set<OCLConstraint> constraints = opConst2smt(ocl2SMTGenerator, in);

    // check if they exist a model for the list of positive Constraint
    List<IdentifiableBoolExpr> solversConstraints =
        constraints.stream().map(OCLConstraint::getPreCond).collect(Collectors.toList());

    solversConstraints.addAll(
        constraints.stream().map(OCLConstraint::getPostCond).collect(Collectors.toList()));

    Solver solver = ocl2SMTGenerator.cd2smtGenerator.makeSolver(solversConstraints);
    System.out.println(solver);
    if (solver.check() != Status.SATISFIABLE) {
      Log.info("there are no Model for the List Of Positive Constraints", "NOWitnessOD");
      return null;
    }

    ASTODArtifact od =
        buildOd(ocl2SMTGenerator.cd2smtGenerator, solver.getModel(), "Witness", partial)
            .orElse(null);

    assert od != null;
    return OCLODHelper.splitPreOD(od);
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
