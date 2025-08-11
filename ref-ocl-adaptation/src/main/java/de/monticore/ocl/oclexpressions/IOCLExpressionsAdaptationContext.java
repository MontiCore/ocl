package de.monticore.ocl.oclexpressions;

import de.monticore.expressions.expressionsbasis.IExpressionsBasisAdaptationContext;
import de.monticore.refadaptation.IAdaptationContext;

public interface IOCLExpressionsAdaptationContext extends IAdaptationContext,
        IExpressionsBasisAdaptationContext {

  // ==========================================================
  // Methods from IAdaptationContext redefined for type safety
  // ===========================================================

  IOCLExpressionsAdaptationVariant createVariant();

  IOCLExpressionsAdaptationContext fork();
}
