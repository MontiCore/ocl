package de.monticore.refadapt;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import de.monticore.ast.ASTNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// TODO keep 4 in name so similarity with TypeCheck3 is clear or rename to "AdaptationResults"?
public class Adaptations4Ast {

  private final ListMultimap<ASTNode, IAdaptationVariant> variants = LinkedListMultimap.create();

  public void addVariant(ASTNode refNode, IAdaptationVariant variant) {
    variants.put(refNode, variant);
  }

  public void addVariants(ASTNode refNode, Collection<? extends IAdaptationVariant> newVariants) {
    variants.putAll(refNode, newVariants);
  }

  public <T extends IAdaptationVariant> void replaceVariant(IAdaptationVariant oldVariant, T newVariant) {
    replaceVariant(oldVariant, List.of(newVariant));
  }

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
    // TODO Should we replace all occurrences of oldVariant in childVariants as well?
  }

  public <T extends IAdaptationVariant> List<T> getVariants(ASTNode refNode) {
    // TODO Do we need to return read-only / copy here?
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
    variants.entries().removeIf(entry -> entry.getValue().equals(variant));
    // TODO Should we also remove the variant from childVariants?
    // TODO We have a reference counting / garbage collection issue here. Should we remove all child
    //   variants from the variants map as well? -> Since variants are not referenced from multiple parent variants -> yes
    for (IAdaptationVariant childVariant : variant.getAllChildVariants().values()) {
      removeVariant(childVariant);
    }
  }

  public void clearVariants(ASTNode refNode) {
    variants.removeAll(refNode);
  }

  public void reset() {
    variants.clear();
  }
}
