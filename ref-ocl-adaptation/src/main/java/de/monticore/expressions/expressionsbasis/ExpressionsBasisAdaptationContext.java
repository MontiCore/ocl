package de.monticore.expressions.expressionsbasis;

import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.symbols.basicsymbols.BasicSymbolsIncMapping;

public interface ExpressionsBasisAdaptationContext extends IAdaptationContext {

  // ==========================================================
  // Methods from IAdaptationContext redefined for type safety
  // ===========================================================

  ExpressionsBasisAdaptationVariant createVariant();

  ExpressionsBasisAdaptationContext fork();

  void addBindings(ExpressionsBasisAdaptationVariant variant);

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  BasicSymbolsIncMapping getBasicSymbolsIncMapping();
}
