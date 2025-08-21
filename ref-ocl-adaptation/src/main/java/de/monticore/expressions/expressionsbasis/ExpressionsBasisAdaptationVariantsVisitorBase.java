package de.monticore.expressions.expressionsbasis;

import de.monticore.ast.ASTNode;
import de.monticore.expressions.expressionsbasis._ast.*;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisHandler;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisTraverser;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisVisitor2;
import de.monticore.refadapt.AbstractAdaptationHandler;

/**
 * Basic implementation of a variants visitor for the <i>ExpressionsBasis</i> language<br>
 * <br>
 * <b>NOTE:</b> This class is intended to be GENERATED in the future! Therefore, only apply changes
 * which are systematic and can be automatically derived from the language grammar/AST.<br>
 */
public class ExpressionsBasisAdaptationVariantsVisitorBase
        extends AbstractAdaptationHandler<IExpressionsBasisAdaptationContext, IExpressionsBasisAdaptationVariant>
        implements ExpressionsBasisVisitor2, ExpressionsBasisHandler {

  private ExpressionsBasisTraverser traverser;

  @Override
  public ExpressionsBasisTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(ExpressionsBasisTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void handle(ASTNameExpression node) {
    getVariants4Ast().clearVariants(node);
    ExpressionsBasisHandler.super.handle(node);
  }

  @Override
  public void handle(ASTArguments node) {
    getVariants4Ast().clearVariants(node);
    ExpressionsBasisHandler.super.handle(node);
  }

  @Override
  public void handle(ASTLiteralExpression node) {
    getVariants4Ast().clearVariants(node);
    ExpressionsBasisHandler.super.handle(node);
  }

  @Override
  public void handle(ASTExpression node) {
    getVariants4Ast().clearVariants(node);
    ExpressionsBasisHandler.super.handle(node);
  }

  @Override
  public void handle(ASTExpressionsBasisNode node) {
    getVariants4Ast().clearVariants(node);
    ExpressionsBasisHandler.super.handle(node);
  }

  @Override
  public void handle(ASTNode node) {
    getVariants4Ast().clearVariants(node);
    ExpressionsBasisHandler.super.handle(node);
  }
}
