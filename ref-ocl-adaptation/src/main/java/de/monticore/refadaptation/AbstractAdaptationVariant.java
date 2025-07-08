package de.monticore.refadaptation;

import de.monticore.ast.ASTNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stores a map of adapted AST nodes that are all consistent regarding the incarnations which
 * were used to adapt the nodes.
 */
public abstract class AbstractAdaptationVariant implements IAdaptationVariant {

  protected final Map<ASTNode, ASTNode> adaptedNodes;


  public AbstractAdaptationVariant() {
    this.adaptedNodes = new HashMap<>();
  }

  protected AbstractAdaptationVariant(Map<ASTNode, ASTNode> adaptedNodes) {
    this.adaptedNodes = new HashMap<>(adaptedNodes);
  }

  @Override
  public <T extends ASTNode> Optional<T> getAdaptedNode(T refNode) {
    return Optional.ofNullable((T) adaptedNodes.get(refNode));
  }

  @Override
  public Map<ASTNode, ASTNode> getAdaptedNodes() {
    return Collections.unmodifiableMap(adaptedNodes);
  }

  @Override
  public void addAdaptedNodes(Map<ASTNode, ASTNode> adaptedNodes) {
    this.adaptedNodes.putAll(adaptedNodes);
  }

  // TODO better deepClone parent and only set child if it exists in adaptedNodes
  @Deprecated
  public <T extends ASTNode> T getAdaptedNodeOrClone(ASTNode refNode) {
    return (T) getAdaptedNode(refNode).orElseGet(() -> refNode.deepClone());
  }

  public void setAdaptedNode(ASTNode refNode, ASTNode adaptedNode) {
    adaptedNodes.put(refNode, adaptedNode);
  }
}
