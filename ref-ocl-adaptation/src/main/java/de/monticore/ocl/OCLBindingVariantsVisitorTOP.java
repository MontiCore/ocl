package de.monticore.ocl;

import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.ocl._visitor.OCLHandler;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl._visitor.OCLVisitor2;
import de.monticore.refadaptation.AbstractAdaptationHandler;

// NOTE: Can be generated.
public class OCLBindingVariantsVisitorTOP
        extends AbstractAdaptationHandler<OCLAdaptationContext, OCLAdaptationVariant>
        implements OCLVisitor2, OCLHandler {

  private OCLTraverser traverser;

  @Override
  public OCLTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(OCLTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void handle(ASTOCLCompilationUnit node) {
    getAdaptations4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLArtifact node) {
    getAdaptations4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLInvariant node) {
    getAdaptations4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLOperationConstraint node) {
    getAdaptations4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLMethodSignature node) {
    getAdaptations4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLContextDefinition node) {
    getAdaptations4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLParamDeclaration node) {
    getAdaptations4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }
}
