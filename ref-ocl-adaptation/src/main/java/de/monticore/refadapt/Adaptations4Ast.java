package de.monticore.refadapt;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import de.monticore.ast.ASTNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class manages adaptation variants for AST nodes form a reference artifact.
 * It allows  associated with specific nodes from a reference artifact.<br>
 * <br>
 * An instance of this class is always accessible during the adaptation process to add, replace,
 * retrieve, and remove variants.
 * It is usually accessed via {@link AbstractAdaptationVisitor#getAdaptations4Ast()}.
 */
public class Adaptations4Ast {

  /**
   * Stores all variants for each reference AST node.
   * The keys are the reference AST nodes, the values are the variants for this node.
   */
  private final ListMultimap<ASTNode, IAdaptationVariant> variants = LinkedListMultimap.create();

  /**
   * Adds a new variant for the given reference AST node.
   *
   * @param refNode the reference AST node to which the variant is associated
   * @param variant the variant to be added
   */
  public void addVariant(ASTNode refNode, IAdaptationVariant variant) {
    variants.put(refNode, variant);
  }

  /**
   * Adds a collection of new variants for the given reference AST node.
   *
   * @param refNode the reference AST node to which the variants are associated
   * @param newVariants the collection of variants to be added
   */
  public void addVariants(ASTNode refNode, Collection<? extends IAdaptationVariant> newVariants) {
    variants.putAll(refNode, newVariants);
  }

  /**
   * Replaces the given old variant with the new variant for all AST nodes that contain the old
   * variant.
   *
   * @param oldVariant the variant to be replaced
   * @param newVariant the new variant to replace the old one
   * @param <T> the language-specific variant type
   */
  public <T extends IAdaptationVariant> void replaceVariant(IAdaptationVariant oldVariant, T newVariant) {
    replaceVariant(oldVariant, List.of(newVariant));
  }

  /**
   * Replaces the given old variant with the new variants for all AST nodes that contain the old
   * variant.<br>
   * <br>
   * This method is used to replace a variant with a new one, e.g., when a variant is split into
   * multiple variants during adaptation.
   *
   * @param oldVariant the variant to be replaced
   * @param newVariants the new variants to replace the old one
   */
  public void replaceVariant(IAdaptationVariant oldVariant, List<? extends IAdaptationVariant> newVariants) {
    for (ASTNode key : variants.keySet()) {
      if (variants.get(key).contains(oldVariant)) {
        // copy list because Multimap returns view-only list when calling 'get'
        List<IAdaptationVariant> variantList = new ArrayList<>(variants.get(key));
        int index = variantList.indexOf(oldVariant);
        variantList.remove(index);
        variantList.addAll(index, newVariants);
        variants.replaceValues(key, variantList);
      }
    }
    // TODO replace all occurrences of oldVariant in childVariants as well!
  }

  /**
   * Returns the list of variants associated with the given reference AST node.<br>
   * <br>
   * This method returns a read-only copy of the variants list!
   *
   * @param refNode the reference AST node for which to retrieve the variants
   * @return a list of variants associated with the given reference AST node
   * @param <T> the language-specific variant type
   */
  public <T extends IAdaptationVariant> List<T> getVariants(ASTNode refNode) {
    // return read-only / copy here so that the caller cannot modify the internal state
    return new ArrayList<T>((Collection<T>) variants.get(refNode));
  }

  /**
   * Removes the given variant from all ASTNodes it is attached to. Also, if the variant has any
   * child variants, they will be removed as well.<br>
   * <br>
   * Call this during variant identification to remove variants that turn out to be incompatible
   * with the constraints of other reference elements.
   *
   * @param variant the variant to remove
   */
  public void removeVariant(IAdaptationVariant variant) {
    // 1. remove the variant itself
    variants.entries().removeIf(entry -> entry.getValue().equals(variant));
    // 2. remove the variant from all parent variants that reference it as a child variant
    // (this is necessary to avoid dangling references)
    for (IAdaptationVariant v : variants.values()) {
      if (v.getAllChildVariants().containsValue(variant)) {
        v.removeChildVariant(variant);
      }
    }
    // 3. remove all children of the variant
    // (child variants are not referenced from multiple parent variants)
    for (IAdaptationVariant childVariant : variant.getAllChildVariants().values()) {
      removeVariant(childVariant);
    }
  }

  /**
   * Clears all variants associated with the given reference AST node.
   *
   * @param refNode the AST node for which to clear the variants
   */
  public void clearVariants(ASTNode refNode) {
    variants.removeAll(refNode);
  }

  /**
   * Resets the internal state of this class, clearing all stored variants.
   */
  public void reset() {
    variants.clear();
  }
}
