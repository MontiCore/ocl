package de.monticore.refadaptation;

import de.monticore.ast.ASTNode;
import de.monticore.visitor.ITraverser;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ReferenceArtifactAdapter {

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

  /**
   * Adapt the given AST node to all its variants.
   * @param refNode
   * @return
   * @param <T>
   */
  public <T extends ASTNode> List<T> adapt(T refNode) {
    // reset AdaptationVariants4Ast & AdaptationResults4Ast...
    refNode.accept(getBindingVariantsTraverser());
    // now we know all the variants in BindingVariants4Ast

    // adaptation visitor gets variants via BindingVariants4Ast
    refNode.accept(getAdaptationTraverser());
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
