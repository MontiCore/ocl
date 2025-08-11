package de.monticore.ocl.setexpressions;

import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsHandler;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsTraverser;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsVisitor2;
import de.monticore.refadaptation.AbstractAdaptationHandler;

// NOTE: Could be generated
public class SetExpressionsAdaptationVariantsVisitorTOP
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
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetNotInExpression node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTUnionExpression node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTIntersectionExpression node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetMinusExpression node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetUnionExpression node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetIntersectionExpression node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetAndExpression node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetOrExpression node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetVariableDeclaration node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetComprehension node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetComprehensionItem node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTGeneratorDeclaration node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetEnumeration node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetValueItem node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetValueRange node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTSetCollectionItem node) {
    getAdaptations4Ast().clearVariants(node);
    SetExpressionsHandler.super.handle(node);
  }
}
