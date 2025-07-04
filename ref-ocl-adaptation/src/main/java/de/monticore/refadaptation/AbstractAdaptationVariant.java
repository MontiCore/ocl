package de.monticore.refadaptation;

import de.monticore.ast.ASTNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stores a map of adapted AST nodes that are all consistent regarding the incarnations which
 * were used to adapt the nodes.
 */
// TODO better not implement all the bindings here -> maybe delegate?
public abstract class AbstractAdaptationVariant {

  private final Map<ASTNode, ASTNode> adaptedNodes;
  // TODO add bindings/context


  public AbstractAdaptationVariant() {
    this.adaptedNodes = new HashMap<>();
  }

  protected AbstractAdaptationVariant(Map<ASTNode, ASTNode> adaptedNodes) {
    this.adaptedNodes = new HashMap<>(adaptedNodes);
  }

  public <T extends ASTNode> Optional<T> getAdaptedNode(T refNode) {
    return Optional.ofNullable((T) adaptedNodes.get(refNode));
  }

  // TODO better deepClone parent and only set child if it exists in adaptedNodes
  @Deprecated
  public <T extends ASTNode> T getAdaptedNodeOrClone(ASTNode refNode) {
    return (T) getAdaptedNode(refNode).orElseGet(() -> refNode.deepClone());
  }

  public void setAdaptedNode(ASTNode refNode, ASTNode adaptedNode) {
    adaptedNodes.put(refNode, adaptedNode);
  }

  public abstract AbstractAdaptationVariant copy();
}
