package de.monticore.types.mccollectiontypes;

import de.monticore.types.mccollectiontypes._ast.*;

public class MCCollectionTypesBindingVariantsVisitor
        extends MCCollectionTypesBindingVariantsVisitorTOP {

  @Override
  public void endVisit(ASTMCListType node) {
    passChildVariantsUpwards(node, node.getMCTypeArgument());
  }

  @Override
  public void endVisit(ASTMCSetType node) {
    passChildVariantsUpwards(node, node.getMCTypeArgument());
  }

  @Override
  public void endVisit(ASTMCOptionalType node) {
    passChildVariantsUpwards(node, node.getMCTypeArgument());
  }

  @Override
  public void endVisit(ASTMCBasicTypeArgument node) {
    passChildVariantsUpwards(node, node.getMCQualifiedType());
  }

  @Override
  public void endVisit(ASTMCPrimitiveTypeArgument node) {
    passChildVariantsUpwards(node, node.getMCPrimitiveType());
  }

  @Override
  public void traverse(ASTMCMapType node) {
    getAdaptations4Ast().addVariants(node, traverseAndPropagateConstraints(node.getKey(), node.getValue()));
  }
}
