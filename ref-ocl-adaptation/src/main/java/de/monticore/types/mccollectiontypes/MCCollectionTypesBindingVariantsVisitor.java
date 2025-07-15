package de.monticore.types.mccollectiontypes;

import de.monticore.refadaptation.AbstractAdaptationHandler;
import de.monticore.types.mccollectiontypes._ast.ASTMCListType;
import de.monticore.types.mccollectiontypes._ast.ASTMCMapType;
import de.monticore.types.mccollectiontypes._ast.ASTMCOptionalType;
import de.monticore.types.mccollectiontypes._ast.ASTMCSetType;
import de.monticore.types.mccollectiontypes._visitor.MCCollectionTypesHandler;
import de.monticore.types.mccollectiontypes._visitor.MCCollectionTypesTraverser;
import de.monticore.types.mccollectiontypes._visitor.MCCollectionTypesVisitor2;

public class MCCollectionTypesBindingVariantsVisitor
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
  public void traverse(ASTMCMapType node) {
    getAdaptations4Ast().addVariants(node, traverseAndPropagateConstraints(node.getKey(), node.getValue()));
  }
}
