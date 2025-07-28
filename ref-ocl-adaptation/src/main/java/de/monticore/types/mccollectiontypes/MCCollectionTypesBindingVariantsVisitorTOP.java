package de.monticore.types.mccollectiontypes;

import de.monticore.refadaptation.AbstractAdaptationHandler;
import de.monticore.types.mccollectiontypes._ast.*;
import de.monticore.types.mccollectiontypes._visitor.MCCollectionTypesHandler;
import de.monticore.types.mccollectiontypes._visitor.MCCollectionTypesTraverser;
import de.monticore.types.mccollectiontypes._visitor.MCCollectionTypesVisitor2;

// NOTE: Could be generated
public class MCCollectionTypesBindingVariantsVisitorTOP
        extends AbstractAdaptationHandler<MCCollectionTypesAdaptationContext, MCCollectionTypesAdaptationVariant>
        implements MCCollectionTypesVisitor2, MCCollectionTypesHandler {

  protected MCCollectionTypesTraverser traverser;

  @Override
  public MCCollectionTypesTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(MCCollectionTypesTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void handle(ASTMCListType node) {
    getAdaptations4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCOptionalType node) {
    getAdaptations4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCMapType node) {
    getAdaptations4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCSetType node) {
    getAdaptations4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCBasicTypeArgument node) {
    getAdaptations4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCPrimitiveTypeArgument node) {
    getAdaptations4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCGenericType node) {
    getAdaptations4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCTypeArgument node) {
    getAdaptations4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCCollectionTypesNode node) {
    getAdaptations4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

}
