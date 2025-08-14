package de.monticore.refadapt;

import com.google.common.base.Preconditions;
import de.monticore.ast.ASTNode;
import de.monticore.visitor.IVisitor;
import de.se_rwth.commons.logging.Log;

import java.util.List;

public abstract class AbstractAdaptationVisitor<C extends IAdaptationContext> implements IVisitor {

  /**
   * The map to be filled with adaptation variants.
   * Adapting classes should always use this method to get the map.
   */
  protected Adaptations4Ast adaptations4Ast;

  protected AdaptationContextHolder contextHolder;

  public void setAdaptations4Ast(Adaptations4Ast adaptations4Ast) {
    Preconditions.checkNotNull(adaptations4Ast);
    this.adaptations4Ast = adaptations4Ast;
  }

  public void setContextHolder(AdaptationContextHolder contextHolder) {
    Preconditions.checkNotNull(contextHolder);
    this.contextHolder = contextHolder;
  }

  protected Adaptations4Ast getAdaptations4Ast() {
    if (adaptations4Ast == null) {
      Log.error("0xFD335 internal error: adaptations4Ast not set."
              + " Check the type traverser setup."
      );
    }
    return adaptations4Ast;
  }

  protected C getAdaptationContext() {
    if (contextHolder == null) {
      Log.error("0xFD335 internal error: contextHolder not set."
              + " Make sure a context is set before using the traverser."
      );
      return null;
    } else {
      // TODO Can we work around this case by using fancy generic? Is it worth the complexity?
      return (C) contextHolder.getContext();
    }
  }

  protected void setAdaptationContext(C context) {
    if (contextHolder == null) {
      Log.error("0xFD335 internal error: contextHolder not set."
              + " Make sure a context is set before using the traverser."
      );
    } else {
      contextHolder.setContext(context);
    }
  }

  /**
   * Passes the child variants of the given child node upwards to the parent node.
   *
   * @param node the parent node to which the child constraints should be passed
   * @param child the child node whose constraints should be passed upwards
   */
  protected void passChildVariantsUpwards(ASTNode node, ASTNode child) {
    getAdaptations4Ast().addVariants(node, getAdaptations4Ast().getVariants(child));
  }

  /**
   * Aggregates the variants of the given children into a single variant of the parent node.
   *
   * @param parent the parent node to which the aggregated variant will be added
   * @param children the list of child nodes whose variants will be aggregated
   */
  protected void aggregateChildVariants(ASTNode parent, List<? extends ASTNode> children) {
    IAdaptationVariant aggregateVariant = getAdaptationContext().createVariant();
    for (ASTNode child : children) {
      aggregateVariant.addChildVariants(child, getAdaptations4Ast().getVariants(child));
    }
    getAdaptations4Ast().addVariant(parent, aggregateVariant);
  }
}
