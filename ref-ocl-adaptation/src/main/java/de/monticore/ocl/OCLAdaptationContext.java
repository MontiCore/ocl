package de.monticore.ocl;

import de.monticore.refadaptation.BindingConflictException;
import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.symbols.*;
import de.monticore.symbols.basicsymbols.IBasicSymbolsBindings;
import de.monticore.symbols.basicsymbols.IBasicSymbolsIncMapping;
import de.monticore.symbols.basicsymbols.IBasicSymbolsLocalIncMapping;

/*
 * NOTE: Could be generated if we would declare the dependencies between the language and
 * the languages from which we want to use symbols/incarnation mappings!
 */
public class OCLAdaptationContext implements IOCLAdaptationContext {

  private final IOOSymbolsIncMapping ooSymbolsIncMapping;
  private final IOOSymbolsBindings ooSymbolsBindings;
  private final IOOSymbolsLocalIncMapping ooSymbolsLocalIncMapping;

  public OCLAdaptationContext(IOOSymbolsIncMapping ooSymbolsIncMapping) {
    this(ooSymbolsIncMapping, new OOSymbolsBindings());
  }

  protected OCLAdaptationContext(
          IOOSymbolsIncMapping ooSymbolsIncMapping,
          IOOSymbolsBindings ooSymbolsBindings) {
    /*
     * We initialize the local incarnation mapping once to avoid repeated creation of new instances
     * when getOOSymbolsIncMapping() is called.
     */
    this(ooSymbolsIncMapping,
        ooSymbolsBindings,
        new OOSymbolsRestrictedIncMapping(ooSymbolsIncMapping, ooSymbolsBindings));
  }

  protected OCLAdaptationContext(
          IOOSymbolsIncMapping ooSymbolsIncMapping,
          IOOSymbolsBindings ooSymbolsBindings,
          IOOSymbolsLocalIncMapping ooSymbolsLocalIncMapping) {
    this.ooSymbolsIncMapping = ooSymbolsIncMapping;
    this.ooSymbolsBindings = ooSymbolsBindings;
    this.ooSymbolsLocalIncMapping = ooSymbolsLocalIncMapping;
  }

  @Override
  public IOCLAdaptationVariant createVariant() {
    // We add all bindings currently holding in this context to the new variant.
    // There is at least one use case: traverse of OCLMethodSignature defines binding for
    // method parameters, but lower level variants need to be aware of the binding during AST
    // adaptation visitor run.
    // Also, as mentioned in IAdaptationContext.createVariant, adding the bindings enables early
    // pruning of invalid variant before they are passed upwards and cause a blowup.
    return new OCLAdaptationVariant(ooSymbolsBindings.copy());
  }

  @Override
  public IOCLAdaptationContext fork() {
    return new OCLAdaptationContext(
        ooSymbolsIncMapping,
        ooSymbolsBindings.copy());
  }

  @Override
  public IOOSymbolsBindings getOOSymbolsBindings() {
    return ooSymbolsBindings;
  }

  @Override
  public IBasicSymbolsBindings getBasicSymbolsBindings() {
    return ooSymbolsBindings;
  }

  @Override
  public IOOSymbolsLocalIncMapping getOOSymbolsIncMapping() {
    return ooSymbolsLocalIncMapping;
  }

  @Override
  public IBasicSymbolsLocalIncMapping getBasicSymbolsIncMapping() {
    // The OOSymbolsLocalIncMapping is a specific implementation of BasicSymbolsLocalIncMapping
    return ooSymbolsLocalIncMapping;
  }

  @Override
  public IBasicSymbolsIncMapping getOriginalBasicSymbolsIncMapping() {
    return ooSymbolsIncMapping;
  }

  @Override
  public IOOSymbolsIncMapping getOriginalOOSymbolsIncMapping() {
    return ooSymbolsIncMapping;
  }

  @Override
  public void addBindings(IAdaptationVariant variant) throws BindingConflictException {
    // no bindings to add
    // TODO does this method then even make sense in the interface?
    // variant used with OCLAdaptationContext should be of type OCLAdaptationVariant
    if (!(variant instanceof IOCLAdaptationVariant)) {
      throw new IllegalArgumentException("Expected an OCLAdaptationVariant, but got: " + variant.getClass().getName());
    }
    IOCLAdaptationVariant oclVariant = (IOCLAdaptationVariant) variant;
    // IMPL NOT: If the context would rely on more bindings from other languages we MUST check
    // ALL for conflicts before 'executing' any change
    if (ooSymbolsBindings.isConflicting(oclVariant.getBasicSymbolsBindings())) {
      throw new BindingConflictException();
    }
    // TODO rely on ooSymbolsBindings including all basic symbol bindings or add them separately?
    //   decide and adapt addAll implementation accordingly
    ooSymbolsBindings.addAll(oclVariant.getBasicSymbolsBindings());
    ooSymbolsBindings.addAll(oclVariant.getOOSymbolsBindings());
  }
}
