package de.monticore.types.mcbasictypes;

import de.monticore.refadapt.IAdaptationVariant;
import de.monticore.symbols.basicsymbols.refmodel.IBasicSymbolsBindings;

public interface IMCBasicTypesAdaptationVariant extends IAdaptationVariant {

  IBasicSymbolsBindings getBasicSymbolsBindings();
}
