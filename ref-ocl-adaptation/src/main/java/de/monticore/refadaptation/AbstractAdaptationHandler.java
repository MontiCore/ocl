package de.monticore.refadaptation;

import de.monticore.ast.ASTNode;
import de.monticore.visitor.ITraverser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractAdaptationHandler<C extends IAdaptationContext, V extends IAdaptationVariant>
        extends AbstractAdaptationVisitor<C> {

  protected abstract ITraverser getTraverser();

  protected List<V> traverseAndPropagateConstraints(ASTNode... nodes) {
    return traverseAndPropagateConstraints(Arrays.asList(nodes));
  }

  /**
   * Traverses the given list of ASTNodes and propagates the constraints from the first child
   * to the last child, returning a list of variants where in ach variant all children are
   * adapted under the same constraints.
   *
   * @param children
   * @return
   */
  protected List<V> traverseAndPropagateConstraints(List<ASTNode> children) {
    if (children.isEmpty()) {
      return List.of((V) getAdaptationContext().createVariant()); // empty variant
    }
    // we have at least one child node
    // 1. get variants for first child
    Iterator<ASTNode> iterator = children.iterator();
    ASTNode firstChild = iterator.next();
    firstChild.accept(getTraverser());
    List<V> variants = getAdaptations4Ast().getVariants(firstChild);
    // 2. get variants for each subsequent child under constraints from all previous children
    while (iterator.hasNext()) {
      ASTNode nextChild = iterator.next();
      variants = traverseForEachVariant(variants, nextChild);
    }
    // now, variants contains all variants where each child is adapted under the same constraints
    // 3. set these variants for all children
    for (ASTNode child : children) {
      getAdaptations4Ast().setVariants(child, variants);
    }
    return variants;
  }

  /**
   * Traverses the given ASTNode for each variant in the sourceVariants list and returns a set
   * of variants where each is a combination of the constraints of a source variant and all
   * possible adaptations of the given ASTNode under these constraints.
   *
   * @param sourceVariants the list of source variants to use as constraints
   * @param node the ASTNode to traverse and adapt
   * @return
   */
  // TODO formalize this more precise / mathematically (look at constraint propagation again)
  // TODO decide if this is meant as helper API for suers or only as internal support method for traverseAndPropagateConstraints
  protected List<V> traverseForEachVariant(
          List<V> sourceVariants, ASTNode node) {
    C previousCtx = getAdaptationContext();
    List<V> resultVariants = new ArrayList<>();
    for (V sourceVariant : sourceVariants) {
      C localCtx = (C) previousCtx.fork(); // TODO avoid unchecked casts by better generics
      localCtx.addBindings(sourceVariant);

      setAdaptationContext(localCtx);
      // clear previous variant results for node
      getAdaptations4Ast().clearVariants(node);

      node.accept(getTraverser());

      List<V> rightAdapted = getAdaptations4Ast().getVariants(node);
      if (rightAdapted.isEmpty()) {
        // conflict with existing bindings -> drop current leftResult
        getAdaptations4Ast().removeVariant(sourceVariant);
      } else {
        for (V rightResult : rightAdapted) {
          V mergedVariant = (V) sourceVariant.merge(rightResult);
          resultVariants.add(mergedVariant);
        }
      }
    }
    // IMPORTANT: reset the adaptation context to the previous one
    setAdaptationContext(previousCtx);
    return resultVariants;
  }
}
