package de.monticore.types.mcbasictypes;

import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.symbols.basicsymbols.IBasicSymbolsIncMapping;
import de.monticore.symbols.basicsymbols.IBasicSymbolsLocalIncMapping;

public interface IMCBasicTypesAdaptationContext extends IAdaptationContext {

  IMCBasicTypesAdaptationVariant createVariant();
  IMCBasicTypesAdaptationContext fork();

  IBasicSymbolsLocalIncMapping getBasicSymbolsIncMapping();

  IBasicSymbolsIncMapping getOriginalBasicSymbolsIncMapping();
}
