package de.monticore.expressions.expressionsbasis;

import de.monticore.refadapt.IAdaptationVariant;
import de.monticore.symbols.basicsymbols.refmodel.IBasicSymbolsBindings;

public interface IExpressionsBasisAdaptationVariant extends IAdaptationVariant {

  // ==========================================================
  // Methods from IAdaptationVariant redefined for type safety
  // ===========================================================

  IExpressionsBasisAdaptationVariant copy();

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  IBasicSymbolsBindings getBasicSymbolsBindings();
}
