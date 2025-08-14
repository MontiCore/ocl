package de.monticore.refadapt;

import de.monticore.ast.ASTNode;
import de.monticore.refmodel.BindingConflictException;
import de.monticore.visitor.ITraverser;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.lang3.function.FailableFunction;

import java.util.*;
import java.util.function.Function;

public abstract class AbstractAdaptationHandler<C extends IAdaptationContext, V extends IAdaptationVariant>
        extends AbstractAdaptationVisitor<C> {

  private static final String LOG_NAME = AbstractAdaptationHandler.class.getName();

  protected abstract ITraverser getTraverser();

  protected void traverseForConsistentVariants(ASTNode parentNode, ASTNode... children) {
    List<V> variants = traverseAndPropagateConstraints(children);
    getAdaptations4Ast().addVariants(parentNode, variants);
  }

  protected void traverseForConsistentVariants(ASTNode parentNode, List<? extends ASTNode> children) {
    List<V> variants = traverseAndPropagateConstraints(children);
    getAdaptations4Ast().addVariants(parentNode, variants);
  }

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
  protected List<V> traverseAndPropagateConstraints(List<? extends ASTNode> children) {
    if (children.isEmpty()) {
      return List.of((V) getAdaptationContext().createVariant()); // empty variant
    }
    // we have at least one child node
    // 1. get variants for first child
    Iterator<? extends ASTNode> iterator = children.iterator();
    ASTNode firstChild = iterator.next();
    firstChild.accept(getTraverser());
    List<V> variants = getAdaptations4Ast().getVariants(firstChild);
    // TODO report error if we get zero variants ? -> or default to empty variant?
    // 2. get variants for each subsequent child under constraints from all previous children
    while (iterator.hasNext()) {
      ASTNode nextChild = iterator.next();
      variants = traverseForEachVariant(variants, nextChild);
    }
    // now, variants contains all variants where each child  is adapted under the same constraints
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
  // TODO decide if this is meant as helper API for users or only as internal support method for traverseAndPropagateConstraints
  protected List<V> traverseForEachVariant(
          List<V> sourceVariants, ASTNode node) {
    return expandAndMergeVariants(sourceVariants, v -> {
      node.accept(getTraverser());
      // no need to copy the list here. getVariants creates a new list internally
      return getAdaptations4Ast().getVariants(node);
    });
  }

  /**
   * Expands the child variants of the given child node and adds them to the parent node.<br>
   * This method is used to propagate the constraints from the child node to the parent node,
   * allowing the parent node to adapt its variants based on the constraints of the child node.
   *
   * @param parentNode the parent node to which the variants will be added
   * @param childNode the child node whose variants will be expanded
   * @param expandVariant a function that retrieves all variants of the parent node for a given
   *                      child variant,
   */
  protected void expandChildVariants(ASTNode parentNode, ASTNode childNode, Function<V, List<V>> expandVariant) {
    List<V> sourceVariants = getAdaptations4Ast().getVariants(childNode);
    if (sourceVariants.isEmpty()) {
      Log.info("No variants found for child node " + childNode, LOG_NAME);
      // TODO Set error in Adaptations4Ast so no default variant is created?
    } else {
      List<V> expandedVariants = expandAndMergeVariants(sourceVariants, expandVariant);
      if (expandedVariants.isEmpty()) {
        Log.info("No expanded variants found for child node " + childNode, LOG_NAME);
        // TODO Set error in Adaptations4Ast so no default variant is created?
      } else {
        getAdaptations4Ast().addVariants(parentNode, expandedVariants);
      }
    }
  }

  /**
   * Expands the given source variants and merges the results into a single list of variants.<br>
   * More precisely, for each source variant:
   * <ol>
   *   <li>Forks the current adaptation context and adds the bindings of the variant.</li>
   *   <li>Switches the current adaptation context to the new one</li>
   *   <li>Retrieves all variants for the given source variant using the provided function.</li>
   *   <li>Merges each retrieved variant with the source variant and replaces the source variant
   *       with the list of merged variants in {@link Adaptations4Ast}.
   *   </li>
   * </ol>>
   *
   * @param sourceVariants the list of source variants to expand and merge
   * @param getVariants a function that retrieves all variants for a given source variant, e.g.
   *                    by traversing an AST node or applying
   * @return
   */
  protected List<V> expandAndMergeVariants(
          List<V> sourceVariants, Function<V, List<V>> getVariants) {
    C previousCtx = getAdaptationContext();
    List<V> resultVariants = new ArrayList<>();
    for (V sourceVariant : sourceVariants) {
      C localCtx = (C) previousCtx.fork(); // TODO avoid unchecked casts by better generics

      try {
        localCtx.addBindings(sourceVariant);
      } catch (BindingConflictException e) {
        // unexpected. sourceVariants should be compatible with the current context
        Log.warn("Unexpected binding conflict. sourceVariants are expected to be " +
                "compatible with the current context when calling 'traverseForEachVariant'");
      }

      setAdaptationContext(localCtx);

      List<V> nodeVariants = getVariants.apply(sourceVariant);

      if (nodeVariants.isEmpty()) {
        // conflict with existing bindings -> drop current leftResult
        getAdaptations4Ast().removeVariant(sourceVariant);
      } else {
        List<V> mergedVariants = new ArrayList<>();
        for (V nodeVariant : nodeVariants) {
          V mergedVariant;
          try {
            mergedVariant = (V) sourceVariant.merge(nodeVariant);
          } catch (BindingConflictException e) {
            // This can happen if some visitors return variants not compatible with the current context
            // However, it is better for performance to prune these variants EARLY. Otherwise, they are
            // propagated up in the tree and cause variant explosion and are then dropped anyway
            continue;
          }
          mergedVariants.add(mergedVariant);
          resultVariants.add(mergedVariant);
          getAdaptations4Ast().replaceVariant(nodeVariant, resultVariants); // can we improve here?
        }
        // TODO What if all variants had merge conflicts? -> mergedVariants is empty
        //   -> replaceVariant actually causes removal of the sourceVariant
        getAdaptations4Ast().replaceVariant(sourceVariant, mergedVariants);
      }
    }
    // IMPORTANT: reset the adaptation context to the previous one
    setAdaptationContext(previousCtx);
    return resultVariants;
  }

  protected <T> List<V> tryCreateVariantsForIncarnations(
          Set<T> incarnations,
          FailableFunction<T, V, BindingConflictException> createVariant) {
    List<V> variants = new ArrayList<>();
    for (T incarnation : incarnations) {
      try {
        variants.add(createVariant.apply(incarnation));
      } catch (BindingConflictException e) {
        // This is expected as some bindings implied by the incarnation may not be compatible
        // with the existing bindings in the adaptation context.
      }
    }
    return variants;
  }
}
