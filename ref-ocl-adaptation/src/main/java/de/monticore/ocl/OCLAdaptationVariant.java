package de.monticore.ocl;

import de.monticore.expressions.commonexpressions.CommonExpressionsAdaptationVariant;
import de.monticore.ocl.oclexpressions.OCLExpressionsAdaptationVariant;
import de.monticore.ocl.setexpressions.SetExpressionsAdaptationVariant;
import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.types.mccollectiontypes.MCCollectionTypesAdaptationVariant;

// NOTE: Can be generated
public interface OCLAdaptationVariant extends
        IAdaptationVariant,
        OCLExpressionsAdaptationVariant,
        SetExpressionsAdaptationVariant,
        CommonExpressionsAdaptationVariant,
        MCCollectionTypesAdaptationVariant
{
  // ==========================================================
  // Methods from IAdaptationVariant redefined for type safety
  // ===========================================================

  OCLAdaptationVariant copy();
}
