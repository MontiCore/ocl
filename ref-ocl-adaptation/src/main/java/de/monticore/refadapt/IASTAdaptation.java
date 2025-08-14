package de.monticore.refadapt;

import de.monticore.ast.ASTNode;

// TODO Naming? Maybe "AdaptationTransformation" ?
/**
 * An AST adaptation is a function that adapts a given AST node of type T to apply some variant
 * specific changes to it.<br>
 * This is used to adapt the AST nodes after the variants have been created, e.g. to rename a
 * variable in an expression.<br>
 * Example:
 * <pre>
 *   newVariant.addASTAdaptation(refExpr, adaptedNode -> {
 *     adaptedNode.setName(variableIncarnation.getName());
 *     return adaptedNode;
 *   });
 * </pre>
 * AST adaptations can be attached to a variant, and are applied in the second phase of the
 * adaptation process, after all valid variants have been discovered.<br>
 * This has the advantage that the adaptation can be <i>defined</i> in the variant visitor,
 * where the context is known, and, for example, the typecheck of an expression as already
 * performed. Later, in the "ASTAdaptationVisitor", an implementation would have to determine
 * the type of the expression once again, and then check for bindings in the variant which
 * duplicates logic. Moreover, if there are multiple kinds of adaptations for a single AST node,
 * the "ASTAdaptationVisitor" would still not know which one to apply without additional information
 * attached to the variant.<br>
 * Instead of attaching this additional information to the variant (per reference AST node), we
 * enable developers to define a simple AST adaptation function that is applied to the AST node in
 * case the variant is not filtered out.
 *
 * @param <T> the type of the AST node to be adapted
 */
@FunctionalInterface
public interface IASTAdaptation<T extends ASTNode> {

  /**
   * Adapts the given AST node to apply some variant-specific changes.<br>
   * This method is called in the second phase of the adaptation process, i.e., after all valid
   * variants have been discovered and the AST nodes have been created.
   *
   *
   * @param adaptedNode the AST node to be adapted. <b>NOTE:</b> This is already a copy of the
   *                    original node with possible links to adapted child nodes of the variant.
   *
   * @return the adapted AST node, which may be the same as the input node or a new instance.<br>
   * <b>NOTE:</b> if you return a new instance, make sure to properly link to the child nodes
   * of <code>adaptedNode</code>. Otherwise, the downstream adaptations will be lost.<br>
   */
  T adapt(T adaptedNode);

  /**
   * Adapts the given AST node to apply some variant-specific changes.<br>
   * <br>
   *
   * @param adaptedNode the AST node to be adapted. This is already a copy of the original node
   * @param original the original AST node from the reference model.
   * @param variant the variant for which the node should be adapted.
   * @return the adapted AST node
   * 
   * @see #adapt(ASTNode)
   */
  default T adapt(T adaptedNode, T original, IAdaptationVariant variant) {
    return adapt(adaptedNode);
  }
}
