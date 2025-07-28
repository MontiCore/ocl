package de.monticore.ocl.oclexpressions;

import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.refadaptation.AbstractAdaptationHandler;

// NOTE: Could be generated
public class OCLExpressionsAdaptationVariantsVisitorTOP
        extends AbstractAdaptationHandler<OCLExpressionsAdaptationContext, OCLExpressionsAdaptationVariant>
        implements OCLExpressionsVisitor2, OCLExpressionsHandler {
  private OCLExpressionsTraverser traverser;

  @Override
  public OCLExpressionsTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(OCLExpressionsTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void handle(ASTAnyExpression node) {
    getAdaptations4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTInDeclaration node) {
    getAdaptations4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTLetinExpression node) {
    getAdaptations4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTExistsExpression node) {
    getAdaptations4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTForallExpression node) {
    getAdaptations4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTTypeIfExpression node) {
    getAdaptations4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTImpliesExpression node) {
    getAdaptations4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTIterateExpression node) {
    getAdaptations4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTEquivalentExpression node) {
    getAdaptations4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTIfThenElseExpression node) {
    getAdaptations4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTTypeIfThenExpression node) {
    getAdaptations4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTInDeclarationVariable node) {
    getAdaptations4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLAtPreQualification node) {
    getAdaptations4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLVariableDeclaration node) {
    getAdaptations4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLTransitiveQualification node) {
    getAdaptations4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }
}
