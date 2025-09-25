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
    getVariants4Ast().addVariants(parentNode, variants);
  }

  protected void traverseForConsistentVariants(ASTNode parentNode, List<? extends ASTNode> children) {
    List<V> variants = traverseAndPropagateConstraints(children);
    getVariants4Ast().addVariants(parentNode, variants);
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
    List<V> variants = getVariants4Ast().getVariants(firstChild);
    // TODO report error if we get zero variants ? -> or default to empty variant?
    // 2. get variants for each subsequent child under constraints from all previous children
    while (iterator.hasNext()) {
      ASTNode nextChild = iterator.next();
      variants = traverseForEachVariant(variants, nextChild);
    }
    // now, variants contains all variants where each child  is adapted under the same constraints
    // TODO add test case for this instead of assertion
    for (ASTNode child : children) {
      assert(getVariants4Ast().getVariants(child).equals(variants));
    }
    return variants;
  }

  /**
   * Traverses the given ASTNode for each variant in the inputVariants list and returns a set
   * of variants where each is a combination of the constraints of an input variant and all
   * possible adaptations of the given ASTNode under these constraints.
   *
   * @param inputVariants the list of variants to use as constraints
   * @param node the ASTNode to traverse and adapt
   * @return
   */
  // TODO formalize this more precise / mathematically (look at constraint propagation again)
  // TODO decide if this is meant as helper API for users or only as internal support method for traverseAndPropagateConstraints
  protected List<V> traverseForEachVariant(
          List<V> inputVariants, ASTNode node) {
    return expandAndMergeVariants(inputVariants, v -> {
      node.accept(getTraverser());
      // no need to copy the list here. getVariants creates a new list internally
      return getVariants4Ast().getVariants(node);
    });
  }

  /**
   * Expands the child variants of the given child node and adds them to the parent node.<br>
   * This method is used to propagate the constraints from the child node to the parent node,
   * allowing the parent node to adapt its variants based on the constraints of the child node.
   *
   * @param parentNode the parent node to which the variants will be added
   * @param childNode the child node whose variants will be expanded
   * @param expandVariant a function that returns a list of variants to which the given variant
   *                      should be expanded
   */
  protected void expandChildVariants(ASTNode parentNode, ASTNode childNode, Function<V, List<V>> expandVariant) {
    List<V> inputVariants = getVariants4Ast().getVariants(childNode);
    if (inputVariants.isEmpty()) {
      Log.info("No variants found for child node " + childNode, LOG_NAME);
      // TODO Set error in Variants4Ast so no default variant is created?
    } else {
      List<V> expandedVariants = expandAndMergeVariants(inputVariants, expandVariant);
      if (expandedVariants.isEmpty()) {
        Log.info("No expanded variants found for child node " + childNode, LOG_NAME);
        // TODO Set error in Variants4Ast so no default variant is created?
      } else {
        getVariants4Ast().addVariants(parentNode, expandedVariants);
      }
    }
  }

  /**
   * Expands the given variants and merges the results into a single list of variants.<br>
   * More precisely, for each input variant:
   * <ol>
   *   <li>Forks the current adaptation context and adds the bindings of the variant.</li>
   *   <li>Switches the current adaptation context to the new one</li>
   *   <li>Retrieves all variants for the given input variant using the provided function.</li>
   *   <li>Merges each retrieved variant with the input variant and replaces the input variant
   *       with the list of merged variants in {@link Variants4Ast}.
   *   </li>
   * </ol>>
   *
   * @param inputVariants the list of input variants to expand and merge
   * @param expandVariant a function that retrieves all variants for a given input variant, e.g.
   *                    by traversing an AST node or applying
   * @return
   */
  protected List<V> expandAndMergeVariants(
          List<V> inputVariants, Function<V, List<V>> expandVariant) {
    C previousCtx = getAdaptationContext();
    List<V> resultVariants = new ArrayList<>();
    for (V inputVariant : inputVariants) {
      C localCtx = (C) previousCtx.fork(); // TODO avoid unchecked casts by better generics

      try {
        localCtx.addBindings(inputVariant);
      } catch (BindingConflictException e) {
        // unexpected. inputVariants should be compatible with the current context
        Log.warn("Unexpected binding conflict. inputVariants are expected to be " +
                "compatible with the current context when calling 'traverseForEachVariant'");
      }

      setAdaptationContext(localCtx);

      List<V> expandedVariants = expandVariant.apply(inputVariant);

      if (expandedVariants.isEmpty()) {
        // conflict with existing bindings -> drop current leftResult
        getVariants4Ast().removeVariant(inputVariant);
      } else {
        List<V> mergedVariants = new ArrayList<>();
        for (V nodeVariant : expandedVariants) {
          V mergedVariant;
          try {
            mergedVariant = (V) inputVariant.merge(nodeVariant);
          } catch (BindingConflictException e) {
            // This can happen if some visitors return variants not compatible with the current context
            // However, it is better for performance to prune these variants EARLY. Otherwise, they are
            // propagated up in the tree and cause variant explosion and are then dropped anyway
            Log.warn("Variant " + nodeVariant + " is not compatible with the current " +
                            "context! Avoid returning incompatible variants from visitors as " +
                            "they can cause variant blowup which drains performance! ", e);
            continue;
          }
          mergedVariants.add(mergedVariant);
        }
        resultVariants.addAll(mergedVariants);
        // If all variants had merge conflicts, mergedVariants is empty and replaceVariant causes
        // removal of the inputVariant.
        getVariants4Ast().replaceVariant(inputVariant, mergedVariants);
        // cleanup expanded variants from last iteration so they do not mix with the actual result
        // variants
        expandedVariants.forEach(getVariants4Ast()::removeVariant);
      }
    }
    /*
     * Finally, update the Variants4Ast data structure with all merged result variants.
     * After this whole method, the expected outcome is that all merged variants are properly
     * linked to their covered reference AST nodes so we have a "clean" Variants4Ast state again.
     */
    getVariants4Ast().insertVariants(resultVariants);
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
