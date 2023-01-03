package de.monticore.ocl2smt.ocldiff;

import static de.monticore.ocl2smt.ocldiff.OCLDiffGenerator.buildOd;

import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.helpers.Helper;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.ocl2smt.util.OCLConstraint;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class OCLOPDiff {

  protected static Context ctx;

  public static Pair<ASTODArtifact, Set<ASTODArtifact>> oclDiffOp(
      ASTCDCompilationUnit ast,
      Set<ASTOCLCompilationUnit> posOcl,
      Set<ASTOCLCompilationUnit> negOcl,
      boolean partial) {
    resetContext();
    Helper.buildPreCD(ast);

    //  oclWitness(ast,posOcl,false) ;
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(ast, ctx);
    Set<OCLConstraint> constraint1 = opConst2smt(ocl2SMTGenerator, posOcl);
    Set<OCLConstraint> constraint2 = opConst2smt(ocl2SMTGenerator, negOcl);

    Set<IdentifiableBoolExpr> posConstraint = new HashSet<>();
    posConstraint.add(constraint1.iterator().next().getPreCond());
    posConstraint.add(constraint1.iterator().next().getPreCond());
    posConstraint.add(constraint2.iterator().next().getPreCond());

    Set<IdentifiableBoolExpr> negConstraints = new HashSet<>();
    negConstraints.add(constraint2.iterator().next().getPostCond().negate(ctx));

    return OCLDiffGenerator.oclDiffHelper(ocl2SMTGenerator, posConstraint, negConstraints, partial);
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

  protected static ASTODArtifact oclWitness(
      ASTCDCompilationUnit ast, Set<ASTOCLCompilationUnit> in, boolean partial) {
    resetContext();
    Helper.buildPreCD(ast);
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(ast, ctx);

    Set<OCLConstraint> constraints = opConst2smt(ocl2SMTGenerator, in);

    // check if they exist a model for the list of positive Constraint
    List<IdentifiableBoolExpr> solversConstraints =
        constraints.stream().map(OCLConstraint::getPreCond).collect(Collectors.toList());
    solversConstraints.addAll(
        constraints.stream().map(OCLConstraint::getPostCond).collect(Collectors.toList()));
    Solver solver = ocl2SMTGenerator.cd2smtGenerator.makeSolver(solversConstraints);
    System.out.println(solver); // TODO:: remove
    if (solver.check() != Status.SATISFIABLE) {
      System.out.println(Arrays.toString(solver.getUnsatCore()));
      Log.error("there are no Model for the List Of Positive Constraints");
    }

    return buildOd(ocl2SMTGenerator.cd2smtGenerator, solver.getModel(), "Witness", partial)
        .orElse(null);
  }

  public static void resetContext() {
    Map<String, String> cfg = new HashMap<>();
    cfg.put("model", "true");
    ctx = new Context(cfg);
  }
}
