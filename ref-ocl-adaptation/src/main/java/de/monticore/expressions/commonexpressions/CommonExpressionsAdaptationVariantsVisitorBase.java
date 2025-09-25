package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsHandler;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsTraverser;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsVisitor2;
import de.monticore.refadapt.AbstractAdaptationHandler;

/**
 * Basic implementation of a variants visitor for the <i>CommonExpressions</i> language<br>
 * <br>
 * <b>NOTE:</b> This class is intended to be GENERATED in the future! Therefore, only apply changes
 * which are systematic and can be automatically derived from the language grammar/AST.<br>
 */
public class CommonExpressionsAdaptationVariantsVisitorBase
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
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTNotEqualsExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTLessThanExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTLessEqualExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTGreaterThanExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTGreaterEqualExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTBooleanAndOpExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTBooleanOrOpExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTBooleanNotExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTLogicalNotExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTPlusExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMinusExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMultExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTDivideExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTModuloExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTFieldAccessExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTCallExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTBracketExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTPlusPrefixExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTArrayAccessExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTConditionalExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMinusPrefixExpression node) {
    getVariants4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }
}
