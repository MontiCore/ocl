package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationContext;
import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.symbols.OOSymbolsIncMapping;

public interface CommonExpressionsAdaptationContext extends IAdaptationContext, ExpressionsBasisAdaptationContext {

  // ==========================================================
  // Methods from IAdaptationContext redefined for type safety
  // ===========================================================

  CommonExpressionsAdaptationVariant createVariant();

  CommonExpressionsAdaptationContext fork();

  void addBindings(CommonExpressionsAdaptationVariant variant);

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  OOSymbolsIncMapping getOOSymbolsIncMapping();
}
