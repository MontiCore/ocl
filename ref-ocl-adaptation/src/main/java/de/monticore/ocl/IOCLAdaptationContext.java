package de.monticore.ocl;

import de.monticore.expressions.commonexpressions.ICommonExpressionsAdaptationContext;
import de.monticore.ocl.oclexpressions.IOCLExpressionsAdaptationContext;
import de.monticore.ocl.setexpressions.ISetExpressionsAdaptationContext;
import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.types.mccollectiontypes.IMCCollectionTypesAdaptationContext;

public interface IOCLAdaptationContext extends
        // TODO extend from all sub languages
        IAdaptationContext,
        IMCCollectionTypesAdaptationContext,
        IOCLExpressionsAdaptationContext,
        ISetExpressionsAdaptationContext,
        ICommonExpressionsAdaptationContext {

  // ==========================================================
  // Methods from IAdaptationContext redefined for type safety
  // ===========================================================

  IOCLAdaptationVariant createVariant();
  IOCLAdaptationContext fork();

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================
}
