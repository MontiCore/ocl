package de.monticore.types.mccollectiontypes;

import de.monticore.refadapt.AbstractAdaptationHandler;
import de.monticore.types.mccollectiontypes._ast.*;
import de.monticore.types.mccollectiontypes._visitor.MCCollectionTypesHandler;
import de.monticore.types.mccollectiontypes._visitor.MCCollectionTypesTraverser;
import de.monticore.types.mccollectiontypes._visitor.MCCollectionTypesVisitor2;

// NOTE: Could be generated
public class MCCollectionTypesAdaptationVariantsVisitorTOP
        extends AbstractAdaptationHandler<IMCCollectionTypesAdaptationContext, IMCCollectionTypesAdaptationVariant>
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
    getVariants4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCOptionalType node) {
    getVariants4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCMapType node) {
    getVariants4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCSetType node) {
    getVariants4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCBasicTypeArgument node) {
    getVariants4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCPrimitiveTypeArgument node) {
    getVariants4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCGenericType node) {
    getVariants4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCTypeArgument node) {
    getVariants4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCCollectionTypesNode node) {
    getVariants4Ast().clearVariants(node);
    MCCollectionTypesHandler.super.handle(node);
  }

}
