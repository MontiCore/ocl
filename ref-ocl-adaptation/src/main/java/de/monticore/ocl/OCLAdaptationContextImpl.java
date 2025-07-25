package de.monticore.ocl;

import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.symbols.*;
import de.monticore.symbols.basicsymbols.BasicSymbolsIncMapping;
import de.monticore.symbols.basicsymbols.BasicSymbolsLocalIncMapping;
import de.monticore.symbols.oosymbols._symboltable.IOOSymbolsGlobalScope;

public class OCLAdaptationContextImpl implements OCLAdaptationContext {

  private final IOOSymbolsGlobalScope ooSymbolsGlobalScope;
  private final OOSymbolsIncMapping ooSymbolsIncMapping;
  private final OOSymbolsBindings ooSymbolsBindings;
  private final OOSymbolsLocalIncMapping ooSymbolsLocalIncMapping;

  public OCLAdaptationContextImpl(
          IOOSymbolsGlobalScope ooSymbolsGlobalScope,
          OOSymbolsIncMapping ooSymbolsIncMapping) {
    this(ooSymbolsGlobalScope, ooSymbolsIncMapping, new OOSymbolsBindingsImpl());
  }

  protected OCLAdaptationContextImpl(
          IOOSymbolsGlobalScope ooSymbolsGlobalScope,
          OOSymbolsIncMapping ooSymbolsIncMapping,
          OOSymbolsBindings ooSymbolsBindings) {
    /*
     * We initialize the local incarnation mapping once to avoid repeated creation of new instances
     * when getOOSymbolsIncMapping() is called.
     */
    this(ooSymbolsGlobalScope,
        ooSymbolsIncMapping,
        ooSymbolsBindings,
        new OOSymbolsRestrictedIncMapping(ooSymbolsIncMapping, ooSymbolsBindings));
  }

  protected OCLAdaptationContextImpl(
          IOOSymbolsGlobalScope ooSymbolsGlobalScope,
          OOSymbolsIncMapping ooSymbolsIncMapping,
          OOSymbolsBindings ooSymbolsBindings,
          OOSymbolsLocalIncMapping ooSymbolsLocalIncMapping) {
    this.ooSymbolsGlobalScope = ooSymbolsGlobalScope;
    this.ooSymbolsIncMapping = ooSymbolsIncMapping;
    this.ooSymbolsBindings = ooSymbolsBindings;
    this.ooSymbolsLocalIncMapping = ooSymbolsLocalIncMapping;
  }

  @Override
  public OCLAdaptationVariant createVariant() {
    return new OCLAdaptationVariantImpl();
  }

  @Override
  public OCLAdaptationContext fork() {
    return new OCLAdaptationContextImpl(
        ooSymbolsGlobalScope,
        ooSymbolsIncMapping,
        ooSymbolsBindings.copy());
  }

  @Override
  public OOSymbolsBindings getOOSymbolsBindings() {
    return ooSymbolsBindings;
  }

  @Override
  public OOSymbolsLocalIncMapping getOOSymbolsIncMapping() {
    return ooSymbolsLocalIncMapping;
  }

  @Override
  public BasicSymbolsLocalIncMapping getBasicSymbolsIncMapping() {
    // The OOSymbolsLocalIncMapping is a specific implementation of BasicSymbolsLocalIncMapping
    return ooSymbolsLocalIncMapping;
  }

  @Override
  public BasicSymbolsIncMapping getOriginalBasicSymbolsIncMapping() {
    return ooSymbolsIncMapping;
  }

  @Override
  public OOSymbolsIncMapping getOriginalOOSymbolsIncMapping() {
    return ooSymbolsIncMapping;
  }

  @Override
  public IOOSymbolsGlobalScope getOOSymbolsGlobalScope() {
    return ooSymbolsGlobalScope;
  }

  @Override
  public void addBindings(IAdaptationVariant variant) {
    // no bindings to add
    // TODO does this method then even make sense in the interface?
    // variant used with OCLAdaptationContext should be of type OCLAdaptationVariant
    if (!(variant instanceof OCLAdaptationVariant)) {
      throw new IllegalArgumentException("Expected an OCLAdaptationVariant, but got: " + variant.getClass().getName());
    }
    OCLAdaptationVariant oclVariant = (OCLAdaptationVariant) variant;
    // TODO rely on ooSymbolsBindings including all basic symbol bindings or add them separately?
    //   decide and adapt addAll implementation accordingly
    ooSymbolsBindings.addAll(oclVariant.getBasicSymbolsBindings());
    ooSymbolsBindings.addAll(oclVariant.getOOSymbolsBindings());
  }
}
