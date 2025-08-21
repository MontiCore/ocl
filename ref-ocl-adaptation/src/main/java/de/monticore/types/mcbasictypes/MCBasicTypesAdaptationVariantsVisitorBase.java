package de.monticore.types.mcbasictypes;

import de.monticore.refadapt.AbstractAdaptationHandler;
import de.monticore.types.mcbasictypes._ast.*;
import de.monticore.types.mcbasictypes._visitor.MCBasicTypesHandler;
import de.monticore.types.mcbasictypes._visitor.MCBasicTypesTraverser;
import de.monticore.types.mcbasictypes._visitor.MCBasicTypesVisitor2;

/**
 * Basic implementation of a variants visitor for the <i>MCBasicTypes</i> language<br>
 * <br>
 * <b>NOTE:</b> This class is intended to be GENERATED in the future! Therefore, only apply changes
 * which are systematic and can be automatically derived from the language grammar/AST.<br>
 */
public class MCBasicTypesAdaptationVariantsVisitorBase
        extends AbstractAdaptationHandler<IMCBasicTypesAdaptationContext, IMCBasicTypesAdaptationVariant>
        implements MCBasicTypesVisitor2, MCBasicTypesHandler {

  private MCBasicTypesTraverser traverser;

  @Override
  public void setTraverser(MCBasicTypesTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public MCBasicTypesTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void handle(ASTMCQualifiedType node) {
    getVariants4Ast().clearVariants(node);
    MCBasicTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCPrimitiveType node) {
    getVariants4Ast().clearVariants(node);
    MCBasicTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCImportStatement node) {
    getVariants4Ast().clearVariants(node);
    MCBasicTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCVoidType node) {
    getVariants4Ast().clearVariants(node);
    MCBasicTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCReturnType node) {
    getVariants4Ast().clearVariants(node);
    MCBasicTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCQualifiedName node) {
    getVariants4Ast().clearVariants(node);
    MCBasicTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCPackageDeclaration node) {
    getVariants4Ast().clearVariants(node);
    MCBasicTypesHandler.super.handle(node);
  }
}
