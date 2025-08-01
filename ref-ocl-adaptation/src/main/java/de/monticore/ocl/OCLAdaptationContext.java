package de.monticore.ocl;

import de.monticore.expressions.commonexpressions.CommonExpressionsAdaptationContext;
import de.monticore.ocl.oclexpressions.OCLExpressionsAdaptationContext;
import de.monticore.ocl.setexpressions.SetExpressionsAdaptationContext;
import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.symbols.OOSymbolsIncMapping;
import de.monticore.symbols.oosymbols._symboltable.IOOSymbolsGlobalScope;
import de.monticore.types.mccollectiontypes.MCCollectionTypesAdaptationContext;

public interface OCLAdaptationContext extends
        // TODO extend from all sub languages
        IAdaptationContext,
        MCCollectionTypesAdaptationContext,
        OCLExpressionsAdaptationContext,
        SetExpressionsAdaptationContext,
        CommonExpressionsAdaptationContext {

  // ==========================================================
  // Methods from IAdaptationContext redefined for type safety
  // ===========================================================

  OCLAdaptationVariant createVariant();
  OCLAdaptationContext fork();

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================
}
