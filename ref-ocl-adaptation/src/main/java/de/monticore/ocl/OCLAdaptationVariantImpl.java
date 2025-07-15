package de.monticore.ocl;

import de.monticore.ast.ASTNode;
import de.monticore.refadaptation.AbstractAdaptationVariant;
import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.symbols.OOSymbolsBindings;
import de.monticore.symbols.OOSymbolsBindingsImpl;
import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;

import java.util.HashMap;
import java.util.Map;

public class OCLAdaptationVariantImpl extends AbstractAdaptationVariant implements OCLAdaptationVariant {

  protected final OOSymbolsBindings ooSymbolsBindings;

  public OCLAdaptationVariantImpl() {
    this(new OOSymbolsBindingsImpl(), new HashMap<>());
  }

  protected OCLAdaptationVariantImpl(OOSymbolsBindings ooSymbolsBindings, Map<ASTNode, ASTNode> adaptedNodes) {
    super(adaptedNodes);
    this.ooSymbolsBindings = ooSymbolsBindings;
  }

  @Override
  public OCLAdaptationVariant copy() {
    return new OCLAdaptationVariantImpl(
            ooSymbolsBindings.copy(),
            new HashMap<>(adaptedNodes));
  }

  @Override
  public IAdaptationVariant merge(IAdaptationVariant otherVariant) {
    if (!(otherVariant instanceof OCLAdaptationVariant)) {
      throw new IllegalArgumentException("Cannot merge with " + otherVariant.getClass().getSimpleName() +
              ". Expected an instance of OCLAdaptationContext.");
    }
    OCLAdaptationVariant otherOCLVariant = (OCLAdaptationVariant) otherVariant;
    OCLAdaptationVariant merged = copy();
    merged.addAllChildVariants(otherVariant); // TODO check for conflicts!
    merged.addAdaptedNodes(otherVariant.getAdaptedNodes());
    merged.getBasicSymbolsBindings().addAll(otherOCLVariant.getBasicSymbolsBindings());
    merged.getOOSymbolsBindings().addAll(otherOCLVariant.getOOSymbolsBindings());
    return merged;
  }

  @Override
  public BasicSymbolsBindings getBasicSymbolsBindings() {
    return ooSymbolsBindings;
  }

  @Override
  public OOSymbolsBindings getOOSymbolsBindings() {
    return ooSymbolsBindings;
  }
}
