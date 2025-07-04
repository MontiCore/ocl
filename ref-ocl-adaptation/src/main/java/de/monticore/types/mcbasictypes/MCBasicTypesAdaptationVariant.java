package de.monticore.types.mcbasictypes;

import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;

public interface MCBasicTypesAdaptationVariant extends IAdaptationVariant {

  BasicSymbolsBindings getBasicSymbolsBindings();

  // TODO Consider inheritance of supported binding interfaces instead of delegating
  //  simplifies API usage in visitors and maybe even easier to implement because symbols with
  //  inheritance may share the same datastructure
}
