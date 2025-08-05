package de.monticore.expressions.expressionsbasis;

import de.monticore.ast.ASTNode;
import de.monticore.expressions.expressionsbasis._ast.*;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisHandler;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisTraverser;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisVisitor2;
import de.monticore.refadaptation.AbstractAdaptationHandler;

// NOTE: Can be generated.
public class ExpressionsBasisAdaptationVariantsVisitorTOP
        extends AbstractAdaptationHandler<ExpressionsBasisAdaptationContext, ExpressionsBasisAdaptationVariant>
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
    getAdaptations4Ast().clearVariants(node);
    ExpressionsBasisHandler.super.handle(node);
  }

  @Override
  public void handle(ASTArguments node) {
    getAdaptations4Ast().clearVariants(node);
    ExpressionsBasisHandler.super.handle(node);
  }

  @Override
  public void handle(ASTLiteralExpression node) {
    getAdaptations4Ast().clearVariants(node);
    ExpressionsBasisHandler.super.handle(node);
  }

  @Override
  public void handle(ASTExpression node) {
    getAdaptations4Ast().clearVariants(node);
    ExpressionsBasisHandler.super.handle(node);
  }

  @Override
  public void handle(ASTExpressionsBasisNode node) {
    getAdaptations4Ast().clearVariants(node);
    ExpressionsBasisHandler.super.handle(node);
  }

  @Override
  public void handle(ASTNode node) {
    getAdaptations4Ast().clearVariants(node);
    ExpressionsBasisHandler.super.handle(node);
  }
}
