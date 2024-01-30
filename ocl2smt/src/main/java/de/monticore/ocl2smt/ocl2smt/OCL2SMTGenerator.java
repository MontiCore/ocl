/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.setexpressions._ast.ASTGeneratorDeclaration;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.OCLExprConverter;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.SourcePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class OCL2SMTGenerator<T extends ExprAdapter<?>> {
  protected OCLExprConverter<T> exprConv;
  protected final Context ctx;

  public OCL2SMTGenerator(ASTCDCompilationUnit ast, Context ctx) {
    exprConv = new OCLExprConverter<>(ast, ctx);
    this.ctx = ctx;
  }

  public OCL2SMTGenerator(ASTCDCompilationUnit ast, OCL2SMTGenerator<T> ocl2SMTGenerator) {
    exprConv = new OCLExprConverter<>(ast, ocl2SMTGenerator);
    this.ctx = ocl2SMTGenerator.ctx;
  }

  public Context getCtx() {
    return ctx;
  }

  public CD2SMTGenerator getCD2SMTGenerator() {
    return exprConv.getCd2smtGenerator();
  }
  /**
   * Convert an ASTOCLArtifact in a Set of SMT BoolExpr
   *
   * @param astOclArtifact ocl Artifact to transform
   * @return the list of SMT BoolExpr
   */
  public List<IdentifiableBoolExpr> inv2smt(ASTOCLArtifact astOclArtifact) {
    List<IdentifiableBoolExpr> constraints = new ArrayList<>();
    for (ASTOCLConstraint constraint : astOclArtifact.getOCLConstraintList()) {
      if (constraint instanceof ASTOCLInvariant)
        constraints.add(convertInv((ASTOCLInvariant) constraint));
    }
    return constraints;
  }

  protected T convertCtxParDec(ASTOCLParamDeclaration node) {
    OCLType oclType = exprConv.typeConverter.buildOCLType(node.getMCType());
    return exprConv.mkConst(node.getName(), oclType);
  }

  protected Pair<T, T> convertGenDec(ASTGeneratorDeclaration node) {
    T expr =
        exprConv.mkConst(node.getName(), exprConv.typeConverter.buildOCLType(node.getSymbol()));
    T set = exprConv.convertExpr(node.getExpression());

    return new ImmutablePair<>(expr, exprConv.factory.mkContains(set, expr));
  }

  public IdentifiableBoolExpr convertInv(ASTOCLInvariant invariant) {

    SourcePosition srcPos = invariant.get_SourcePositionStart();

    // convert parameter declaration  in context
    Function<T, T> invCtx = openInvScope(invariant);

    // convert the inv body
    T inv = invCtx.apply(exprConv.convertExpr(invariant.getExpression()));

    // add general invConstraints
    for (T constr : exprConv.getGenConstraints()) {
      inv = exprConv.factory.mkAnd(inv, constr);
    }

    Optional<String> name =
        invariant.isPresentName() ? Optional.ofNullable(invariant.getName()) : Optional.empty();
    exprConv.reset();
    return IdentifiableBoolExpr.buildIdentifiable(
        (BoolExpr) inv.getExpr(), srcPos, name); // todo fix (BoolExpr)
  }

  protected Function<T, T> openInvScope(ASTOCLInvariant invariant) {
    List<T> vars = new ArrayList<>();
    T varConstraint = exprConv.factory.mkBool(true);
    for (ASTOCLContextDefinition invCtx : invariant.getOCLContextDefinitionList()) {
      if (invCtx.isPresentOCLParamDeclaration()) {
        vars.add(convertCtxParDec(invCtx.getOCLParamDeclaration()));
      }
      if (invCtx.isPresentGeneratorDeclaration()) {
        Pair<T, T> res = convertGenDec(invCtx.getGeneratorDeclaration());
        varConstraint = exprConv.factory.mkAnd(varConstraint, res.getRight());
        vars.add(res.getLeft());
      }
    }
    T varConstraint2 = varConstraint;
    if (!vars.isEmpty()) {
      return bool -> exprConv.mkForall(vars, exprConv.factory.mkImplies(varConstraint2, bool));
    }
    return bool -> bool;
  }

  public Optional<ASTODArtifact> buildOd(Model model, String ODName, boolean partial) {
    return exprConv.getCd2smtGenerator().smt2od(model, partial, ODName);
  }

  public Solver makeSolver(List<IdentifiableBoolExpr> constraints) {
    return exprConv.getCd2smtGenerator().makeSolver(constraints);
  }
}
