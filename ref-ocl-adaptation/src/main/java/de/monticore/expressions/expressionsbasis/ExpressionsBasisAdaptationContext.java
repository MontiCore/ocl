package de.monticore.expressions.expressionsbasis;

import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.symbols.basicsymbols.BasicSymbolsLocalIncMapping;

public interface ExpressionsBasisAdaptationContext extends IAdaptationContext {

  // ==========================================================
  // Methods from IAdaptationContext redefined for type safety
  // ===========================================================

  ExpressionsBasisAdaptationVariant createVariant();

  ExpressionsBasisAdaptationContext fork();

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  BasicSymbolsLocalIncMapping getBasicSymbolsIncMapping();

  BasicSymbolsLocalIncMapping getOriginalBasicSymbolsIncMapping();
}
