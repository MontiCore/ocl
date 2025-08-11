package de.monticore.ocl;

import de.monticore.expressions.commonexpressions.ICommonExpressionsAdaptationVariant;
import de.monticore.ocl.oclexpressions.IOCLExpressionsAdaptationVariant;
import de.monticore.ocl.setexpressions.ISetExpressionsAdaptationVariant;
import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.types.mccollectiontypes.IMCCollectionTypesAdaptationVariant;

// NOTE: Can be generated
public interface IOCLAdaptationVariant extends
        IAdaptationVariant,
        IOCLExpressionsAdaptationVariant,
        ISetExpressionsAdaptationVariant,
        ICommonExpressionsAdaptationVariant,
        IMCCollectionTypesAdaptationVariant
{
  // ==========================================================
  // Methods from IAdaptationVariant redefined for type safety
  // ===========================================================

  IOCLAdaptationVariant copy();
}
