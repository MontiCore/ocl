package de.monticore.types.mcbasictypes;

import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.symbols.basicsymbols.IBasicSymbolsBindings;

public interface IMCBasicTypesAdaptationVariant extends IAdaptationVariant {

  IBasicSymbolsBindings getBasicSymbolsBindings();
}
