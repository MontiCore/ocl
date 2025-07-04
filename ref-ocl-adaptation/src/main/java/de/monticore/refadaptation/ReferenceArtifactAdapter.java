package de.monticore.refadaptation;

import de.monticore.ast.ASTNode;
import de.monticore.visitor.ITraverser;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ReferenceArtifactAdapter<C extends IAdaptationContext> {

  protected ITraverser bindingVariantsTraverser;

  protected ITraverser adaptationTraverser;

  protected AdaptationContextHolder contextHolder;

  /** Results of step 1/2 the adapted variants of an AST node */
  protected Adaptations4Ast adaptations4Ast;

  protected ReferenceArtifactAdapter(
          ITraverser bindingVariantsTraverser,
          ITraverser adaptationTraverser,
          AdaptationContextHolder contextHolder,
          Adaptations4Ast adaptations4Ast
  ) {
    this.bindingVariantsTraverser = Log.errorIfNull(bindingVariantsTraverser);
    this.adaptationTraverser = Log.errorIfNull(adaptationTraverser);
    this.contextHolder = Log.errorIfNull(contextHolder);
    this.adaptations4Ast = Log.errorIfNull(adaptations4Ast);
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

  public Adaptations4Ast getAdaptations4Ast() {
    return adaptations4Ast;
  }

  /**
   * Adapt the given AST node to all its variants.
   *
   * @param refNode the reference AST node to adapt
   * @param context the adaptation context to use for the adaptation
   *
   * @return a list of all adapted AST nodes that are variants of the given reference node
   * @param <T> the type of the AST node to adapt
   */
  public <T extends ASTNode> List<T> adapt(T refNode, C context) {
    // 1. reset
    getAdaptations4Ast().reset();
    // 2. init context
    getContextHolder().setContext(context);
    // 3. find all valid binding variants
    refNode.accept(getBindingVariantsTraverser());
    // 4. adapt the reference node according to the binding variants
    refNode.accept(getAdaptationTraverser());
    // 5. collect all adaptations of the reference node from the variants
    return adaptations4Ast.getVariants(refNode).stream()
            .map(variant -> {
              Optional<T> adaptedNode = variant.getAdaptedNode(refNode);
              if (adaptedNode.isEmpty()) {
                Log.warn("0xFD336 "
                        + "no adapted node for original input node in variant: " + variant);
                return adaptedNode;
              }
              return adaptedNode;
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
  }
}
