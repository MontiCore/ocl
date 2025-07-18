package de.monticore.expressions.expressionsbasis;

import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;

public interface ExpressionsBasisAdaptationVariant extends ExpressionsBasisAdaptationVariantTOP {

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  BasicSymbolsBindings getBasicSymbolsBindings();
}
