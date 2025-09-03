package de.monticore.refadapt;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import de.monticore.ast.ASTNode;
import de.monticore.refmodel.BindingConflictException;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Stores a map of adapted AST nodes that are all consistent regarding the incarnations which
 * were used to adapt the nodes.
 */
public abstract class AbstractAdaptationVariant implements IAdaptationVariant {

  protected final Set<ASTNode> coveredRefNodes;

  /**
   * The map of adapted AST nodes.<br>
   * The keys are the reference AST nodes, the values are the adapted AST nodes for this variant.
   */
  protected final Map<ASTNode, ASTNode> adaptedNodes;

  /** The adaptations / transformations to be executed for each reference AST node. */
  protected final ListMultimap<ASTNode, IASTAdaptation<? extends ASTNode>> astAdaptations;

  /** The child variants for each reference AST node. */
  protected final ListMultimap<ASTNode, IAdaptationVariant> childVariants;


  protected AbstractAdaptationVariant() {
    this.coveredRefNodes = new HashSet<>();
    this.adaptedNodes = new HashMap<>();
    this.astAdaptations = ArrayListMultimap.create();
    this.childVariants = ArrayListMultimap.create();
  }

  /**
   * Constructor for creating an adaptation variant with given adapted nodes, AST adaptations,
   * and child variants (all of which are copied to ensure immutability).
   *
   * @param adaptedNodes
   * @param astAdaptations
   * @param childVariants
   */
  protected AbstractAdaptationVariant(
          Set<ASTNode> coveredRefNodes,
          Map<ASTNode, ASTNode> adaptedNodes,
          ListMultimap<ASTNode, IASTAdaptation<? extends ASTNode>> astAdaptations,
          ListMultimap<ASTNode, IAdaptationVariant> childVariants) {
    this.coveredRefNodes = new HashSet<>(coveredRefNodes);
    this.adaptedNodes = new HashMap<>(adaptedNodes);
    this.astAdaptations = ArrayListMultimap.create(astAdaptations);
    this.childVariants = ArrayListMultimap.create(childVariants);
  }

  @Override
  public Set<ASTNode> getCoveredRefNodes() {
    return Collections.unmodifiableSet(coveredRefNodes);
  }

  @Override
  public void addCoveredRefNode(ASTNode refNode) {
    Validate.notNull(refNode);
    this.coveredRefNodes.add(refNode);
  }

  @Override
  public void addAllCoveredRefNodes(Collection<? extends ASTNode> refNodes) {
    Validate.notNull(refNodes);
    this.coveredRefNodes.addAll(refNodes);
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

  @Override
  public void removeChildVariant(IAdaptationVariant variant) {
    for (ASTNode key : childVariants.keySet()) {
      if (childVariants.get(key).contains(variant)) {
        // copy list because Multimap returns view-only list when calling 'get'
        List<IAdaptationVariant> variantList = new ArrayList<>(childVariants.get(key));
        variantList.remove(variant);
        childVariants.replaceValues(key, variantList);
      }
    }
  }

  @Override
  public <T extends ASTNode> void addASTAdaptation(T refNode, IASTAdaptation<T> adaptation) {
    Validate.notNull(refNode);
    Validate.notNull(adaptation);
    astAdaptations.put(refNode, adaptation);
  }

  @Override
  public <T extends ASTNode> List<IASTAdaptation<T>> getASTAdaptations(T refNode) {
    Validate.notNull(refNode);
    return astAdaptations.get(refNode).stream()
            .map(adaptation -> (IASTAdaptation<T>) adaptation)
            .collect(Collectors.toList());
  }

  @Override
  public ListMultimap<ASTNode, IASTAdaptation<? extends ASTNode>> getAllASTAdaptations() {
    return LinkedListMultimap.create(astAdaptations);
  }

  @Override
  public void addAllASTAdaptations(IAdaptationVariant otherVariant) {
    for (Map.Entry<ASTNode, Collection<IASTAdaptation<? extends ASTNode>>> entry : otherVariant.getAllASTAdaptations().asMap().entrySet()) {
      for (IASTAdaptation<? extends ASTNode> adaptation : entry.getValue()) {
        addASTAdaptation(entry.getKey(), (IASTAdaptation<? super ASTNode>) adaptation);
      }
    }
  }

  /**
   * Checks if this variant can be merged with another variant.<br>
   * It cannot be merged if any of the following conditions apply:
   * <ul>
   *   <li>It has conflicting bindings with the other variant.</li>
   *   <li>It (partially) covers the same reference nodes. Only variants covering different parts
   *       of the AST can be merged!</li>
   *   <li>It has child variants that conflict with the other variant's child variants.</li>
   *   <li>It has AST adaptations for an AST node for which the other variant defined an AST
   *        adaptation as well. This is not a required condition, but we check it because it is an
   *        indication that something is wrong in the adaptation code. Two variants that are being
   *        merged are usually results of different subtrees in the AST.
   *   </li>
   * </ul>
   * @param otherVariant the other variant to check for conflicts with this one
   *
   * @see IAdaptationVariant#merge(IAdaptationVariant)
   *
   * @throws BindingConflictException if the other variant has conflicting bindings with this variant.
   * @throws IllegalArgumentException if the other variant cannot be merged with this one because
   *   some rules were violated (see above). This is likely the reason if an developer fault
   *   in the adaptation process.
   */
  protected void checkMergeConflicts(IAdaptationVariant otherVariant) throws  BindingConflictException, IllegalArgumentException {
    if (!Sets.intersection(coveredRefNodes, otherVariant.getCoveredRefNodes()).isEmpty()) {
      throw new IllegalArgumentException("Cannot merge variants that cover the same reference AST nodes.");
    }
    if (!Sets.intersection(childVariants.keySet(), otherVariant.getAllChildVariants().keySet()).isEmpty()) {
      throw new IllegalArgumentException("Cannot merge variants with child variants covering the same AST nodes.");
    }
    if (!Sets.intersection(astAdaptations.keySet(), otherVariant.getAllASTAdaptations().keySet()).isEmpty()) {
      throw new IllegalArgumentException("Cannot merge variants with AST adaptations for the same AST nodes.");
    }
  }
}
