package de.monticore.ocl.oclexpressions;

import de.monticore.expressions.expressionsbasis.IExpressionsBasisAdaptationVariant;
import de.monticore.refadaptation.IAdaptationVariant;

public interface IOCLExpressionsAdaptationVariant extends IAdaptationVariant,
        IExpressionsBasisAdaptationVariant {

  // ==========================================================
  // Methods from IAdaptationVariant redefined for type safety
  // ===========================================================

  IOCLExpressionsAdaptationVariant copy();
}
