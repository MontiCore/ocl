package de.monticore.ocl;

import de.monticore.expressions.commonexpressions.CommonExpressionsAdaptationVariant;
import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationVariant;
import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.symbols.*;
import de.monticore.symbols.basicsymbols.BasicSymbolsIncMapping;
import de.monticore.symbols.basicsymbols.BasicSymbolsLocalIncMapping;
import de.monticore.types.mcbasictypes.MCBasicTypesAdaptationVariant;
import de.monticore.types.mccollectiontypes.MCCollectionTypesAdaptationVariant;

public class OCLAdaptationContextImpl implements OCLAdaptationContext {

  private final OOSymbolsIncMapping ooSymbolsIncMapping;
  private final OOSymbolsBindings ooSymbolsBindings;
  private final OOSymbolsLocalIncMapping ooSymbolsLocalIncMapping;

  public OCLAdaptationContextImpl(OOSymbolsIncMapping ooSymbolsIncMapping) {
    this(ooSymbolsIncMapping, new OOSymbolsBindingsImpl());
  }

  protected OCLAdaptationContextImpl(
          OOSymbolsIncMapping ooSymbolsIncMapping,
          OOSymbolsBindings ooSymbolsBindings) {
    /*
     * We initialize the local incarnation mapping once to avoid repeated creation of new instances
     * when getOOSymbolsIncMapping() is called.
     */
    this(ooSymbolsIncMapping,
        ooSymbolsBindings,
        new OOSymbolsRestrictedIncMapping(ooSymbolsIncMapping, ooSymbolsBindings));
  }

  protected OCLAdaptationContextImpl(
          OOSymbolsIncMapping ooSymbolsIncMapping,
          OOSymbolsBindings ooSymbolsBindings,
          OOSymbolsLocalIncMapping ooSymbolsLocalIncMapping) {
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
        ooSymbolsIncMapping,
        ooSymbolsBindings.copy());
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
  public void addBindings(CommonExpressionsAdaptationVariant variant) {

  }

  @Override
  public void addBindings(ExpressionsBasisAdaptationVariant variant) {

  }

  @Override
  public void addBindings(MCCollectionTypesAdaptationVariant variant) {

  }

  @Override
  public void addBindings(MCBasicTypesAdaptationVariant variant) {

  }

  @Override
  public void addBindings(IAdaptationVariant variant) {

  }

  @Override
  public void addBindings(OCLAdaptationVariant variant) {

  }
}
