package de.monticore.ocl.setexpressions;

import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationVariant;
import de.monticore.refadaptation.IAdaptationVariant;

public interface SetExpressionsAdaptationVariant extends IAdaptationVariant,
        ExpressionsBasisAdaptationVariant {

  // ==========================================================
  // Methods from IAdaptationVariant redefined for type safety
  // ===========================================================

  SetExpressionsAdaptationVariant copy();
}
