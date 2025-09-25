package de.monticore.ocl.setexpressions;

import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsHandler;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsTraverser;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsVisitor2;
import de.monticore.refadapt.AbstractAdaptationHandler;

/**
 * Basic implementation of a variants visitor for the <i>SetExpressions</i> language<br>
 * <br>
 * <b>NOTE:</b> This class is intended to be GENERATED in the future! Therefore, only apply changes
 * which are systematic and can be automatically derived from the language grammar/AST.<br>
 */
public class SetExpressionsAdaptationVariantsVisitorBase
        extends AbstractAdaptationHandler<ISetExpressionsAdaptationContext, ISetExpressionsAdaptationVariant>
        implements SetExpressionsVisitor2, SetExpressionsHandler {
  private SetExpressionsTraverser traverser;

  @Override
  public SetExpressionsTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(SetExpressionsTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void handle(ASTSetInExpression node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetNotInExpression node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTUnionExpression node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTIntersectionExpression node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetMinusExpression node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetUnionExpression node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetIntersectionExpression node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetAndExpression node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetOrExpression node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetVariableDeclaration node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetComprehension node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetComprehensionItem node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTGeneratorDeclaration node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetEnumeration node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetValueItem node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetValueRange node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetCollectionItem node) {
    getVariants4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }
}
