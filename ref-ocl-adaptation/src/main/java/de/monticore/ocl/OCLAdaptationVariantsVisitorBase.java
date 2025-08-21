package de.monticore.ocl;

import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.ocl._visitor.OCLHandler;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl._visitor.OCLVisitor2;
import de.monticore.refadapt.AbstractAdaptationHandler;

/**
 * Basic implementation of a variants visitor for the <i>OCL</i> language<br>
 * <br>
 * <b>NOTE:</b> This class is intended to be GENERATED in the future! Therefore, only apply changes
 * which are systematic and can be automatically derived from the language grammar/AST.<br>
 */
public class OCLAdaptationVariantsVisitorBase
        extends AbstractAdaptationHandler<IOCLAdaptationContext, IOCLAdaptationVariant>
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
    getVariants4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLArtifact node) {
    getVariants4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLInvariant node) {
    getVariants4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLOperationConstraint node) {
    getVariants4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLMethodSignature node) {
    getVariants4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLContextDefinition node) {
    getVariants4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLParamDeclaration node) {
    getVariants4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }
}
