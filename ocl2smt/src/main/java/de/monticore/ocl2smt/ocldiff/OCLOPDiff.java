package de.monticore.ocl2smt.ocldiff;

import com.microsoft.z3.Context;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.ocl2smt.helpers.Helper;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.odbasis._ast.ASTODArtifact;
import java.util.*;
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
    OCL2SMTGenerator ocl2smt = new OCL2SMTGenerator(ast, ctx);
    Pair<IdentifiableBoolExpr, IdentifiableBoolExpr> constraint1 =
        ocl2smt.convertOpConst(
            (ASTOCLOperationConstraint)
                posOcl.iterator().next().getOCLArtifact().getOCLConstraintList().get(0));

    Pair<IdentifiableBoolExpr, IdentifiableBoolExpr> constraint2 =
        ocl2smt.convertOpConst(
            (ASTOCLOperationConstraint)
                negOcl.iterator().next().getOCLArtifact().getOCLConstraintList().get(0));

    Set<IdentifiableBoolExpr> posConstraint = new HashSet<>();
    posConstraint.add(constraint1.getLeft());
    posConstraint.add(constraint2.getLeft());
    posConstraint.add(constraint1.getRight());

    Set<IdentifiableBoolExpr> negConstraints = new HashSet<>();
    negConstraints.add(constraint2.getRight().negate(ctx));

    return OCLDiffGenerator.oclDiffHelper(ocl2smt, posConstraint, negConstraints, false);
  }

  public static void resetContext() {
    Map<String, String> cfg = new HashMap<>();
    cfg.put("model", "true");
    de.monticore.ocl2smt.ocldiff.OCLDiffGenerator.ctx = new Context(cfg);
  }
}
