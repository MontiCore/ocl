package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsHandler;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsTraverser;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsVisitor2;
import de.monticore.refadapt.AbstractAdaptationHandler;

public class CommonExpressionsAdaptationVariantsVisitorTOP
        extends AbstractAdaptationHandler<ICommonExpressionsAdaptationContext, ICommonExpressionsAdaptationVariant>
        implements CommonExpressionsVisitor2, CommonExpressionsHandler {
  private CommonExpressionsTraverser traverser;

  @Override
  public CommonExpressionsTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(CommonExpressionsTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void handle(ASTEqualsExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTNotEqualsExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTLessThanExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTLessEqualExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTGreaterThanExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTGreaterEqualExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTBooleanAndOpExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTBooleanOrOpExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTBooleanNotExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTLogicalNotExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTPlusExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMinusExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMultExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTDivideExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTModuloExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTFieldAccessExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTCallExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTBracketExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTPlusPrefixExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTArrayAccessExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTConditionalExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMinusPrefixExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }
}
