package de.monticore.ocl.oclexpressions;

import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationVariant;
import de.monticore.refadaptation.IAdaptationVariant;

public interface OCLExpressionsAdaptationVariant extends IAdaptationVariant,
        ExpressionsBasisAdaptationVariant {

  // ==========================================================
  // Methods from IAdaptationVariant redefined for type safety
  // ===========================================================

  OCLExpressionsAdaptationVariant copy();
}
