package de.monticore.ocl2smt.ocldiff.invariantDiff;

import com.microsoft.z3.Context;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.odbasis._ast.ASTODArtifact;
import java.util.Set;

public class CompInvariantDiff implements OCLInvDiffStrategy {
  protected FiniteOCLInvariantDiff finiteOperator;
  protected OCLInvariantDiff normalOperator;

  public CompInvariantDiff(long max) {
    finiteOperator = new FiniteOCLInvariantDiff(max);
    normalOperator = new OCLInvariantDiff();
  }

  @Override
  public ASTODArtifact oclWitness(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> ocl,
      Set<IdentifiableBoolExpr> addConstraints,
      Context ctx,
      int timeout,
      boolean partial) {

    // try computing the witness with the normal operator
    ASTODArtifact od = normalOperator.oclWitness(cd, ocl, addConstraints, ctx, 10000, partial);
    // TODO: 04.09.2023  flexible timeout
    if (od != null) {
      return od;
    }

    // compute the witness with the finite operator
    return finiteOperator.oclWitness(cd, ocl, addConstraints, ctx, partial);
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

    OCLInvDiffResult res =
        normalOperator.oclInvDiff(cd, newConstr, inv, addConstr, ctx, 10000, partial);
    // TODO: 04.09.2023  flexible time out
    if (res.isPresentTrace() || res.isPresentWitness()) {
      return res;
    }

    return finiteOperator.oclInvDiff(cd, newConstr, inv, addConstr, ctx, partial);
  }
}
