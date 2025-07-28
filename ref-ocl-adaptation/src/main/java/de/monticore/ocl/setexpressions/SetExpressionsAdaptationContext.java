package de.monticore.ocl.setexpressions;

import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationContext;
import de.monticore.refadaptation.IAdaptationContext;

public interface SetExpressionsAdaptationContext extends IAdaptationContext,
        ExpressionsBasisAdaptationContext {

  // ==========================================================
  // Methods from IAdaptationContext redefined for type safety
  // ===========================================================

  SetExpressionsAdaptationVariant createVariant();

  SetExpressionsAdaptationContext fork();
}
