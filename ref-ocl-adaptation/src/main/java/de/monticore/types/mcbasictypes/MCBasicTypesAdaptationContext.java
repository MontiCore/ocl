package de.monticore.types.mcbasictypes;

import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.symbols.basicsymbols.BasicSymbolsIncMapping;

public interface MCBasicTypesAdaptationContext extends IAdaptationContext {

  MCBasicTypesAdaptationVariant createVariant();
  MCBasicTypesAdaptationContext fork();
  void addBindings(MCBasicTypesAdaptationVariant variant);

  BasicSymbolsIncMapping getBasicSymbolsIncMapping();
}
