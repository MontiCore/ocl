package de.monticore.refadaptation;

import de.monticore.ast.ASTNode;

import java.util.Map;
import java.util.Optional;

public interface IAdaptationVariant {

  <T extends ASTNode> Optional<T> getAdaptedNode(T refNode);

  void setAdaptedNode(ASTNode refNode, ASTNode adaptedNode);

  Map<ASTNode, ASTNode> getAdaptedNodes();

  void addAdaptedNodes(Map<ASTNode, ASTNode> adaptedNodes);

  /**
   * Creates a copy of this adaptation variant.<br>
   * This is necessary to enable visitors of composed languages to create new variants without
   * knowing the actual implementation (which is defined by the top level language)
   *
   * @return a new instance of IAdaptationVariant that is a copy of this one.
   */
  IAdaptationVariant copy();

  IAdaptationVariant merge(IAdaptationVariant otherVariant);
}
