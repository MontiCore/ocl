package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationVariant;
import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.symbols.OOSymbolsBindings;

/*
 * TODO maybe rename CommonExpressionsBindingVariant and separate adapted AST nodes in other
 *  then we can store adapted AST nodes PER IBindingVariant
 */
public interface CommonExpressionsAdaptationVariant extends IAdaptationVariant, ExpressionsBasisAdaptationVariant {

  // ==========================================================
  // Methods from IAdaptationVariant redefined for type safety
  // ===========================================================

  CommonExpressionsAdaptationVariant copy();

  CommonExpressionsAdaptationVariant merge(CommonExpressionsAdaptationVariant otherVariant);

  OOSymbolsBindings getOOSymbolsBindings();
}
