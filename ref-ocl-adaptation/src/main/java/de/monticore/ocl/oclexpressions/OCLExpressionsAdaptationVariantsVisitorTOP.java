package de.monticore.ocl.oclexpressions;

import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.refadapt.AbstractAdaptationHandler;

// NOTE: Could be generated
public class OCLExpressionsAdaptationVariantsVisitorTOP
        extends AbstractAdaptationHandler<IOCLExpressionsAdaptationContext, IOCLExpressionsAdaptationVariant>
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
    getVariants4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTInDeclaration node) {
    getVariants4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTLetinExpression node) {
    getVariants4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTExistsExpression node) {
    getVariants4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTForallExpression node) {
    getVariants4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTTypeIfExpression node) {
    getVariants4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTImpliesExpression node) {
    getVariants4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTIterateExpression node) {
    getVariants4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTEquivalentExpression node) {
    getVariants4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTIfThenElseExpression node) {
    getVariants4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTTypeIfThenExpression node) {
    getVariants4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTInDeclarationVariable node) {
    getVariants4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLAtPreQualification node) {
    getVariants4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLVariableDeclaration node) {
    getVariants4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLTransitiveQualification node) {
    getVariants4Ast().clearVariants(node);
    OCLExpressionsHandler.super.handle(node);
  }
}
