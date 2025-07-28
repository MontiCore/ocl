package de.monticore.ocl.oclexpressions;

import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationContext;
import de.monticore.refadaptation.IAdaptationContext;

public interface OCLExpressionsAdaptationContext extends IAdaptationContext,
        ExpressionsBasisAdaptationContext {

  // ==========================================================
  // Methods from IAdaptationContext redefined for type safety
  // ===========================================================

  OCLExpressionsAdaptationVariant createVariant();

  OCLExpressionsAdaptationContext fork();
}
