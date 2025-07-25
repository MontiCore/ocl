package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationVariant;
import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.symbols.OOSymbolsBindings;

public interface CommonExpressionsAdaptationVariant extends IAdaptationVariant, ExpressionsBasisAdaptationVariant {

  // ==========================================================
  // Methods from IAdaptationVariant redefined for type safety
  // ===========================================================

  CommonExpressionsAdaptationVariant copy();

  OOSymbolsBindings getOOSymbolsBindings();
}
