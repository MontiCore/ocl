package de.monticore.refadapt;

import de.monticore.ast.ASTNode;
import de.monticore.visitor.ITraverser;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Abstract implementation defining the basic framework for reference artifact adaptation.
 * This class provides the necessary traverser instances and datastructures to perform the
 * adaptation of an AST node. The high level adaptation process is defined in
 * {@link #adapt(ASTNode, IAdaptationContext)}.<br>
 * <br>
 * Subclasses do not have to add additional functionality to this class, but can add
 * language-specific convenience methods which do not require users to create an adaptation
 * context themselves, but just pass the required incarnation mappings.
 * See {@link de.monticore.ocl.OCLReferenceArtifactAdapter}.
 *
 * @param <C> the language specific adaptation context type
 */
public abstract class ReferenceArtifactAdapter<C extends IAdaptationContext> {

  /**
   * The traverser for step 1: Used to identify all valid binding variants.
   * All handlers for constraint propagation and visitors that add variants must be added to
   * this traverser.
   */
  protected ITraverser bindingVariantsTraverser;

  /**
   * The traverser for step 2: Used to adapt the reference AST according to the bindings from
   * step 1.
   */
  protected ITraverser adaptationTraverser;

  /** Provides access to the current adaptation context. */
  protected AdaptationContextHolder contextHolder;

  /**
   * Maps reference AST nodes to all their adaptation variants.<br>
   * Step 1 adds the variants in the first place, while step 2 enriches each variant with the
   * adapted AST nodes.
   */
  protected Variants4Ast variants4Ast;

  protected ReferenceArtifactAdapter(
          ITraverser bindingVariantsTraverser,
          ITraverser adaptationTraverser,
          AdaptationContextHolder contextHolder,
          Variants4Ast variants4Ast
  ) {
    this.bindingVariantsTraverser = Log.errorIfNull(bindingVariantsTraverser);
    this.adaptationTraverser = Log.errorIfNull(adaptationTraverser);
    this.contextHolder = Log.errorIfNull(contextHolder);
    this.variants4Ast = Log.errorIfNull(variants4Ast);
  }

  public ITraverser getBindingVariantsTraverser() {
    return bindingVariantsTraverser;
  }

  public ITraverser getAdaptationTraverser() {
    return adaptationTraverser;
  }

  public AdaptationContextHolder getContextHolder() {
    return contextHolder;
  }

  public Variants4Ast getVariants4Ast() {
    return variants4Ast;
  }

  /**
   * Adapt the given AST node to all variants possible within the given context.<br>
   * <br>
   * On a high level, this method performs the following steps:
   * <ol>
   *   <li>
   *     Identify all possible variants of each AST node considering the given incarnation mapping.
   *   </li>
   *   <li>
   *     For each variant, adapt the reference AST node according to the bindings of the variant.
   *   </li>
   *   <li>
   *     Return all adapted variants of the root AST node that was passed to the method.
   *   </li>
   * </ol>
   * If you want detailed insights into the variants and the identified bindings, you can use
   * {@link #getVariants4Ast()} to access the variants for each reference AST node.
   *
   * @param refNode the reference AST node to adapt
   * @param context the adaptation context to use for the adaptation
   *
   * @return a list of all adaptations of the given reference AST node
   * @param <T> the type of the AST node to adapt
   */
  public <T extends ASTNode> List<T> adapt(T refNode, C context) {
    // 1. reset
    getVariants4Ast().reset();
    // 2. init context
    getContextHolder().setContext(context);
    // 3. find all valid binding variants
    refNode.accept(getBindingVariantsTraverser());
    // 4. adapt the reference node according to the binding variants
    refNode.accept(getAdaptationTraverser());
    // 5. collect all adaptations of the reference node from the variants
    return variants4Ast.getVariants(refNode).stream()
            .map(variant -> {
              Optional<T> adaptedNode = variant.getAdaptedNode(refNode);
              if (adaptedNode.isEmpty()) {
                Log.warn("no adapted node for original input node in variant: " + variant);
                return adaptedNode;
              }
              return adaptedNode;
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
  }
}
