package de.monticore.types.mcbasictypes;

import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.symbols.basicsymbols.BasicSymbolsIncMapping;
import de.monticore.symbols.basicsymbols.BasicSymbolsLocalIncMapping;

public interface MCBasicTypesAdaptationContext extends IAdaptationContext {

  MCBasicTypesAdaptationVariant createVariant();
  MCBasicTypesAdaptationContext fork();

  BasicSymbolsLocalIncMapping getBasicSymbolsIncMapping();

  BasicSymbolsIncMapping getOriginalBasicSymbolsIncMapping();
}
