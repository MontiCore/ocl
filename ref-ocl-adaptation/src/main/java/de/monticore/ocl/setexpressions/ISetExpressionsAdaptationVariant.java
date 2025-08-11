package de.monticore.ocl.setexpressions;

import de.monticore.expressions.expressionsbasis.IExpressionsBasisAdaptationVariant;
import de.monticore.refadaptation.IAdaptationVariant;

public interface ISetExpressionsAdaptationVariant extends IAdaptationVariant,
        IExpressionsBasisAdaptationVariant {

  // ==========================================================
  // Methods from IAdaptationVariant redefined for type safety
  // ===========================================================

  ISetExpressionsAdaptationVariant copy();
}
