package de.monticore.ocl.setexpressions;

import de.monticore.expressions.expressionsbasis.IExpressionsBasisAdaptationContext;
import de.monticore.refadaptation.IAdaptationContext;

public interface ISetExpressionsAdaptationContext extends IAdaptationContext,
        IExpressionsBasisAdaptationContext {

  // ==========================================================
  // Methods from IAdaptationContext redefined for type safety
  // ===========================================================

  ISetExpressionsAdaptationVariant createVariant();

  ISetExpressionsAdaptationContext fork();
}
