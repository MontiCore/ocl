package de.monticore.ocl;

import de.monticore.expressions.commonexpressions.CommonExpressionsAdaptationContext;
import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.symbols.oosymbols._symboltable.IOOSymbolsGlobalScope;
import de.monticore.types.mccollectiontypes.MCCollectionTypesAdaptationContext;

public interface OCLAdaptationContext extends
        // TODO extend from all sub languages
        IAdaptationContext,
        MCCollectionTypesAdaptationContext,
        CommonExpressionsAdaptationContext {

  OCLAdaptationVariant createVariant();
  OCLAdaptationContext fork();

  // TODO decide if we should move this to the OOSymbolsIncMapping interface
  IOOSymbolsGlobalScope getOOSymbolsGlobalScope();
}
