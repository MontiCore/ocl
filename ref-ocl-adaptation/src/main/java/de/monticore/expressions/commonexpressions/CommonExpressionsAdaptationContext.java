package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationContext;
import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.symbols.OOSymbolsBindings;
import de.monticore.symbols.OOSymbolsIncMapping;
import de.monticore.symbols.OOSymbolsLocalIncMapping;

public interface CommonExpressionsAdaptationContext extends IAdaptationContext, ExpressionsBasisAdaptationContext {

  // ==========================================================
  // Methods from IAdaptationContext redefined for type safety
  // ===========================================================

  CommonExpressionsAdaptationVariant createVariant();

  CommonExpressionsAdaptationContext fork();

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  OOSymbolsBindings getOOSymbolsBindings();

  OOSymbolsLocalIncMapping getOOSymbolsIncMapping();

  OOSymbolsIncMapping getOriginalOOSymbolsIncMapping();
}
