package de.monticore.refadaptation;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import de.monticore.ast.ASTNode;

import java.util.*;

/**
 * Stores a map of adapted AST nodes that are all consistent regarding the incarnations which
 * were used to adapt the nodes.
 */
public abstract class AbstractAdaptationVariant implements IAdaptationVariant {

  protected final Map<ASTNode, ASTNode> adaptedNodes;
  // Implementation note: LinkedList makes more sense as we usually only have one child?
  // TODO Does this impl note still makes sense if we keep merging variants and ony set children if
  //   we have aggregated variants.
  protected final ListMultimap<ASTNode, IAdaptationVariant> childVariants = LinkedListMultimap.create();


  protected AbstractAdaptationVariant() {
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

  public void setAdaptedNode(ASTNode refNode, ASTNode adaptedNode) {
    adaptedNodes.put(refNode, adaptedNode);
  }

  @Override
  public void addChildVariant(ASTNode refNode, IAdaptationVariant variant) {
    childVariants.put(refNode, variant);
    // TODO if we add a child variant we must also add the bindings to the bindings of the parent variant
  }

  @Override
  public void addChildVariants(ASTNode refNode, Collection<? extends IAdaptationVariant> variants) {
    for (IAdaptationVariant childVariant : variants) {
      addChildVariant(refNode, childVariant);
    }
  }

  @Override
  public void addAllChildVariants(IAdaptationVariant otherVariant) {
    for (Map.Entry<ASTNode, Collection<IAdaptationVariant>> entry : otherVariant.getAllChildVariants().asMap().entrySet()) {
      addChildVariants(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public <T extends IAdaptationVariant> List<T> getChildVariants(ASTNode refNode) {
    return (List<T>) childVariants.get(refNode);
  }

  @Override
  public ListMultimap<ASTNode, IAdaptationVariant> getAllChildVariants() {
    return LinkedListMultimap.create(childVariants);
  }
}
