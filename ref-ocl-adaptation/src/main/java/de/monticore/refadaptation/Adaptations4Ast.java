package de.monticore.refadaptation;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import de.monticore.ast.ASTNode;

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

  public void setVariants(ASTNode refNode, Collection<? extends IAdaptationVariant> newVariants) {
    variants.removeAll(refNode);
    variants.putAll(refNode, newVariants);
  }

  public <T extends IAdaptationVariant> List<T> getVariants(ASTNode refNode) {
    return (List<T>) variants.get(refNode);
  }

  // TODO remove variants from all keys?
  public void removeVariant(ASTNode refNode, IAdaptationVariant variant) {
    variants.remove(refNode, variant);
  }

  public void removeVariant(IAdaptationVariant variant) {
    variants.entries().removeIf(entry -> entry.getValue().equals(variant));
  }

  public void clearVariants(ASTNode refNode) {
    variants.removeAll(refNode);
  }

  public void reset() {
    variants.clear();
  }
}
