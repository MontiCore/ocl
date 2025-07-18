package de.monticore.expressions.expressionsbasis;

import de.monticore.symbols.basicsymbols.BasicSymbolsIncMapping;
import de.monticore.symbols.basicsymbols.BasicSymbolsLocalIncMapping;

public interface ExpressionsBasisAdaptationContext extends ExpressionsBasisAdaptationContextTOP {

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  BasicSymbolsLocalIncMapping getBasicSymbolsIncMapping();

  BasicSymbolsIncMapping getOriginalBasicSymbolsIncMapping();
}
