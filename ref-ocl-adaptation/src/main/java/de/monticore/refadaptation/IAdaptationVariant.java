package de.monticore.refadaptation;

import de.monticore.ast.ASTNode;

import java.util.Optional;

public interface IAdaptationVariant {

  <T extends ASTNode> Optional<T> getAdaptedNode(T refNode);

  void setAdaptedNode(ASTNode refNode, ASTNode adaptedNode);

  IAdaptationVariant copy();

  IAdaptationVariant merge(IAdaptationVariant otherVariant);
}
