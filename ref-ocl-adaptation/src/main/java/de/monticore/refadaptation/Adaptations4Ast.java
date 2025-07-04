package de.monticore.refadaptation;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import de.monticore.ast.ASTNode;

import java.util.List;

// TODO keep 4 in name so similarity with TypeCheck3 is clear or rename to "AdaptationResults"?
public class Adaptations4Ast {

  private final ListMultimap<ASTNode, IAdaptationVariant> variants = LinkedListMultimap.create();

  public void addVariant(ASTNode refNode, IAdaptationVariant variant) {
    variants.put(refNode, variant);
  }

  public <T extends IAdaptationVariant> List<T> getVariants(ASTNode refNode) {
    return (List<T>) variants.get(refNode);
  }

  // TODO remove variants from all keys?
  public void removeVariant(ASTNode refNode, IAdaptationVariant variant) {
    variants.remove(refNode, variant);
  }

  public void reset() {
    variants.clear();
  }
}
