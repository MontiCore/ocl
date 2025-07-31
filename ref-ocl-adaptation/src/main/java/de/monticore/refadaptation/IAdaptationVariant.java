package de.monticore.refadaptation;

import com.google.common.collect.ListMultimap;
import de.monticore.ast.ASTNode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TODO add extensive docs to this central interface
 * ...
 * <br>
 * <h5>Child variants</h5>
 * A variant can have zero or more child variants for per reference AST node. Each child variant
 * covers the subtree starting from the reference AST node it is attached to. Different child
 * variants for the same AST node can have a different (possibly conflicting) set of bindings. This
 * enables the aggregation of multiple variant. e.g., an OCL artifact may aggregate all variants of
 * all constraints to void outputting one artifact for each adapted constraint. Instead, the result
 * would be a single artifact that contains all adapted constraints.<br>
 * <br>
 * <h5>AST Adaptation</h5>
 * A variant can have zero or more AST adaptations for each reference AST node. An AST adaptation
 * is a function to modify the AST node specifically for this variant. See {@link IASTAdaptation}
 * for details.<br>
 *
 */
public interface IAdaptationVariant {

  /**
   * Returns the adapted AST node for the given reference node.<br>
   * If no adapted node is available, an empty Optional is returned.
   *
   * @param refNode the reference AST node for which to retrieve the adapted node
   * @param <T> the type of the AST node
   * @return an Optional containing the adapted AST node or empty if not available
   */
  <T extends ASTNode> Optional<T> getAdaptedNode(T refNode);

  /**
   * Sets the adapted AST node for the given reference node.<br>
   * <br>
   * <b>NOTE:</b> The adapted node instance must be <i>unique</i> for each variant, as this is
   * the final node which will end up in the output AST.
   *
   * @param refNode the reference AST node for which to set the adapted node
   * @param adaptedNode the adapted AST node to be set
   */
  void setAdaptedNode(ASTNode refNode, ASTNode adaptedNode);

  Map<ASTNode, ASTNode> getAdaptedNodes();

  void addAdaptedNodes(Map<ASTNode, ASTNode> adaptedNodes);

  /**
   * Creates a copy of this adaptation variant.<br>
   * This is necessary to enable visitors of composed languages to create new variants without
   * knowing the actual implementation (which is defined by the top level language).
   *
   * @return a new instance of IAdaptationVariant that is a copy of this one.
   */
  IAdaptationVariant copy();

  /**
   * Merges this adaptation variant with another one creating a new variant.<br>
   * <ul>
   *   <li>The other variant MUST NOT have any conflicting bindings with this variant.
   *   </li>
   *   <li>The other variant MUST NOT have child constraints conflicting with the child constraints
   *   of this variant. See {@link IAdaptationVariant#addChildVariant(ASTNode, IAdaptationVariant)}
   *   </li>
   * </ul>
   *
   * @param otherVariant the other variant to merge with this one.
   * @return
   * @throws BindingConflictException if the other variant has conflicting bindings with this variant.<br>
   */
  // TODO The conflict is not limited to bindings -> should we introduce another exception type?
  IAdaptationVariant merge(IAdaptationVariant otherVariant) throws BindingConflictException;

  /**
   * Checks if this variant is conflicting with another variant.<br>
   * Conflicting means:
   * <ul>
   *   <li>It has conflicting bindings with the other variant.</li>
   *   <li>It has child variants that conflict with the other variant's child variants.</li>
   *   <li>It has AST adaptations for an AST node for which the other variant defined an AST
   *        adaptation as well. This is not a required condition, but we check it because it is an
   *        indication that something is wrong in the adaptation code. Two variants that are being
   *        merged are usually results of different subtrees in the AST.
   *   </li>
   * </ul>
   * @param otherVariant the other variant to check for conflicts with this one
   * @return true if this variant is conflicting with the other variant, false otherwise
   * @see IAdaptationVariant#merge(IAdaptationVariant)
   */
  boolean isConflicting(IAdaptationVariant otherVariant);

  /*
   * TODO Make implementations for copy & merge more hierarchical, i.e. add
   *  variation copy(IAdaptationVariant copy) / similar to deepClone so subclasses can sue that
   *  maybe even name deepClone to make it more consistent with MontiCore namings.
   *    -> NO NOT call it deepClone() people could think we deepCLone even the AST nodes in the variant!
   *
   *  similar for merge
   */

  /**
   * Adds a child variant for the given reference node.<br>
   *
   * @param refNode
   * @param childVariant
   */
  void addChildVariant(ASTNode refNode, IAdaptationVariant childVariant);
  void addChildVariants(ASTNode refNode, Collection<? extends IAdaptationVariant> childVariants);

  void addAllChildVariants(IAdaptationVariant variant);

  <T extends IAdaptationVariant> List<T> getChildVariants(ASTNode refNode);

  ListMultimap<ASTNode, IAdaptationVariant> getAllChildVariants();


  /**
   * Adds an {@link IASTAdaptation} for the given reference node.
   *
   * @param refNode the AST node to which the adaptation applies
   * @param adaptation the AST adaptation to be added
   * @param <T> the AST node type that the adaptation applies to
   *
   * @see IASTAdaptation
   */
  <T extends ASTNode> void addASTAdaptation(T refNode, IASTAdaptation<T> adaptation);

  /**
   * returns all AST adaptations for the given reference node.
   *
   * @param refNode the reference AST node for which to retrieve adaptations
   * @return a list of AST adaptations applicable to the reference node
   * @param <T> the AST node type that the adaptations apply to
   */
  <T extends ASTNode> List<IASTAdaptation<T>> getASTAdaptations(T refNode);

  ListMultimap<ASTNode, IASTAdaptation<? extends ASTNode>> getAllASTAdaptations();

  void addAllASTAdaptations(IAdaptationVariant variant);
}
