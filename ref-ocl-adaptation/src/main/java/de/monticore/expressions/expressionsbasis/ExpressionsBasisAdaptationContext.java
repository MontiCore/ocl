package de.monticore.expressions.expressionsbasis;

import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;
import de.monticore.symbols.basicsymbols.BasicSymbolsIncMapping;
import de.monticore.symbols.basicsymbols.BasicSymbolsLocalIncMapping;

public interface ExpressionsBasisAdaptationContext extends ExpressionsBasisAdaptationContextTOP {

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  BasicSymbolsBindings getBasicSymbolsBindings();

  BasicSymbolsLocalIncMapping getBasicSymbolsIncMapping();

  BasicSymbolsIncMapping getOriginalBasicSymbolsIncMapping();
}
