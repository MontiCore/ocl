package de.monticore.expressions.expressionsbasis;

import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;
import de.monticore.symbols.basicsymbols.BasicSymbolsIncMapping;

public interface ExpressionsBasisAdaptationVariant extends IAdaptationVariant {

  // ==========================================================
  // Methods from IAdaptationVariant redefined for type safety
  // ===========================================================

  ExpressionsBasisAdaptationVariant copy();

  ExpressionsBasisAdaptationVariant merge(ExpressionsBasisAdaptationVariant otherVariant);

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  BasicSymbolsBindings getBasicSymbolsBindings();
}
