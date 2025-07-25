package de.monticore.expressions.expressionsbasis;

import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;

public interface ExpressionsBasisAdaptationVariant extends IAdaptationVariant {

  // ==========================================================
  // Methods from IAdaptationVariant redefined for type safety
  // ===========================================================

  ExpressionsBasisAdaptationVariant copy();

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  BasicSymbolsBindings getBasicSymbolsBindings();
}
