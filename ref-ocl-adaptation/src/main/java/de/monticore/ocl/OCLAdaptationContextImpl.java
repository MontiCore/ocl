package de.monticore.ocl;

import de.monticore.expressions.commonexpressions.CommonExpressionsAdaptationVariant;
import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationVariant;
import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.symbols.OOSymbolsIncMapping;
import de.monticore.symbols.basicsymbols.BasicSymbolsIncMapping;
import de.monticore.types.mcbasictypes.MCBasicTypesAdaptationVariant;
import de.monticore.types.mccollectiontypes.MCCollectionTypesAdaptationVariant;

public class OCLAdaptationContextImpl implements OCLAdaptationContext {

  private OOSymbolsIncMapping ooSymbolsIncMapping;

  public OCLAdaptationContextImpl(OOSymbolsIncMapping ooSymbolsIncMapping) {
    this.ooSymbolsIncMapping = ooSymbolsIncMapping;
  }

  @Override
  public OCLAdaptationVariant createVariant() {
    return new OCLAdaptationVariantImpl();
  }

  @Override
  public OCLAdaptationContext fork() {
    return new OCLAdaptationContextImpl(ooSymbolsIncMapping);
  }

  @Override
  public OOSymbolsIncMapping getOOSymbolsIncMapping() {
    return null;
  }

  @Override
  public BasicSymbolsIncMapping getBasicSymbolsIncMapping() {
    return null;
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
