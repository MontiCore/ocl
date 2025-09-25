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
  protected Variants4Ast variants4Ast;

  protected AdaptationContextHolder contextHolder;

  public void setVariants4Ast(Variants4Ast variants4Ast) {
    Preconditions.checkNotNull(variants4Ast);
    this.variants4Ast = variants4Ast;
  }

  public void setContextHolder(AdaptationContextHolder contextHolder) {
    Preconditions.checkNotNull(contextHolder);
    this.contextHolder = contextHolder;
  }

  protected Variants4Ast getVariants4Ast() {
    if (variants4Ast == null) {
      Log.error("internal error: variants4Ast not set. Check the type traverser setup.");
    }
    return variants4Ast;
  }

  protected C getAdaptationContext() {
    if (contextHolder == null) {
      Log.error("internal error: contextHolder not set."
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
      Log.error("internal error: contextHolder not set."
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
    getVariants4Ast().addVariants(node, getVariants4Ast().getVariants(child));
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
      aggregateVariant.addChildVariants(child, getVariants4Ast().getVariants(child));
    }
    getVariants4Ast().addVariant(parent, aggregateVariant);
  }
}
