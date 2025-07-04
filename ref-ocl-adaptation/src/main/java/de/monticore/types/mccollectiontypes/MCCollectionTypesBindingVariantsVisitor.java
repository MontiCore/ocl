package de.monticore.types.mccollectiontypes;

import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.types.mccollectiontypes._ast.ASTMCListType;
import de.monticore.types.mccollectiontypes._ast.ASTMCMapType;
import de.monticore.types.mccollectiontypes._ast.ASTMCSetType;
import de.monticore.types.mccollectiontypes._visitor.MCCollectionTypesHandler;
import de.monticore.types.mccollectiontypes._visitor.MCCollectionTypesTraverser;
import de.monticore.types.mccollectiontypes._visitor.MCCollectionTypesVisitor2;

public class MCCollectionTypesBindingVariantsVisitor extends AbstractAdaptationVisitor implements MCCollectionTypesVisitor2, MCCollectionTypesHandler {

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
    passChildConstraintsUpwards(node, node.getMCTypeArgument());
  }

  @Override
  public void endVisit(ASTMCSetType node) {
    passChildConstraintsUpwards(node, node.getMCTypeArgument());
  }

  @Override
  public void traverse(ASTMCMapType node) {
    MCCollectionTypesHandler.super.traverse(node);
  }

  @Override
  public void endVisit(ASTMCMapType node) {
    // TODO traverse key first and then value
  }
}
