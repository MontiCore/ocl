package de.monticore.expressions.expressionsbasis;

import de.monticore.expressions.expressionsbasis._ast.ASTArguments;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisVisitor2;
import de.monticore.literals.mcliteralsbasis._ast.ASTLiteral;
import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.refadaptation.IASTAdaptation;
import de.monticore.refadaptation.RefAdaptationUtils;

import java.util.List;
import java.util.Optional;

public class ExpressionsBasisASTAdaptationVisitor
        extends AbstractAdaptationVisitor<ExpressionsBasisAdaptationContext>
        implements ExpressionsBasisVisitor2 {

  @Override
  public void endVisit(ASTNameExpression expr) {
    List<ExpressionsBasisAdaptationVariant> variants = getAdaptations4Ast().getVariants(expr);
    for (ExpressionsBasisAdaptationVariant variant : variants) {
      // 1. Default adaptation (links to adapted child nodes)
      ASTNameExpression adaptedNode = adapt(expr, variant);
      // 2. Apply AST adaptations registered specifically for this variant
      for (IASTAdaptation<ASTNameExpression> adaptationFun : variant.getASTAdaptations(expr)) {
        adaptedNode = adaptationFun.adapt(adaptedNode);
      }
      variant.setAdaptedNode(expr, adaptedNode);
    }
  }

  protected ASTNameExpression adapt(ASTNameExpression original, ExpressionsBasisAdaptationVariant variant) {
    ASTNameExpression adapted = ExpressionsBasisMill.nameExpressionBuilder().uncheckedBuild();

    adapted.setName(original.getName());

    RefAdaptationUtils.deepCloneComments(original, adapted);
    return adapted;
  }

  @Override
  public void endVisit(ASTArguments node) {
    List<ExpressionsBasisAdaptationVariant> variants = getAdaptations4Ast().getVariants(node);
    for (ExpressionsBasisAdaptationVariant variant : variants) {
      // 1. Default adaptation (links to adapted child nodes)
      ASTArguments adaptedNode = adapt(node, variant);
      // 2. Apply AST adaptations registered specifically for this variant
      for (IASTAdaptation<ASTArguments> adaptationFun : variant.getASTAdaptations(node)) {
        adaptedNode = adaptationFun.adapt(adaptedNode);
      }
      variant.setAdaptedNode(node, adaptedNode);
    }
  }

  protected ASTArguments adapt(ASTArguments original, ExpressionsBasisAdaptationVariant variant) {
    ASTArguments adapted = ExpressionsBasisMill.argumentsBuilder().uncheckedBuild();
    for (ASTExpression expression : original.getExpressionList()) {
      Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(expression);
      adapted.getExpressionList().add(adaptedExpression.orElseGet(expression::deepClone));
    }

    RefAdaptationUtils.deepCloneComments(original, adapted);
    return adapted;
  }

  @Override
  public void endVisit(ASTLiteralExpression node) {
    List<ExpressionsBasisAdaptationVariant> variants = getAdaptations4Ast().getVariants(node);
    for (ExpressionsBasisAdaptationVariant variant : variants) {
      // 1. Default adaptation (links to adapted child nodes)
      ASTLiteralExpression adaptedNode = adapt(node, variant);
      // 2. Apply AST adaptations registered specifically for this variant
      for (IASTAdaptation<ASTLiteralExpression> adaptationFun : variant.getASTAdaptations(node)) {
        adaptedNode = adaptationFun.adapt(adaptedNode);
      }
      variant.setAdaptedNode(node, adaptedNode);
    }
  }

  protected ASTLiteralExpression adapt(ASTLiteralExpression original, ExpressionsBasisAdaptationVariant variant) {
    ASTLiteralExpression adapted = ExpressionsBasisMill.literalExpressionBuilder().uncheckedBuild();

    Optional<ASTLiteral> adaptedLiteral = variant.getAdaptedNode(original.getLiteral());
    adapted.setLiteral(adaptedLiteral.orElseGet(original.getLiteral()::deepClone));

    RefAdaptationUtils.deepCloneComments(original, adapted);
    return adapted;
  }
}
