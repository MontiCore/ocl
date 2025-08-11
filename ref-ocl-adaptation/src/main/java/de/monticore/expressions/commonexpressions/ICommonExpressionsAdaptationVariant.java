package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.expressionsbasis.IExpressionsBasisAdaptationVariant;
import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.symbols.IOOSymbolsBindings;

public interface ICommonExpressionsAdaptationVariant extends IAdaptationVariant, IExpressionsBasisAdaptationVariant {

  // ==========================================================
  // Methods from IAdaptationVariant redefined for type safety
  // ===========================================================

  ICommonExpressionsAdaptationVariant copy();

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  IOOSymbolsBindings getOOSymbolsBindings();
}
