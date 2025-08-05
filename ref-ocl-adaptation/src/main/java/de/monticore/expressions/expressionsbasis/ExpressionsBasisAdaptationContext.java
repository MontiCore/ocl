package de.monticore.expressions.expressionsbasis;

import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;
import de.monticore.symbols.basicsymbols.BasicSymbolsIncMapping;
import de.monticore.symbols.basicsymbols.BasicSymbolsLocalIncMapping;

public interface ExpressionsBasisAdaptationContext extends IAdaptationContext {

  // ==========================================================
  // Methods from IAdaptationContext redefined for type safety
  // ===========================================================

  ExpressionsBasisAdaptationVariant createVariant();

  ExpressionsBasisAdaptationContext fork();

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  BasicSymbolsBindings getBasicSymbolsBindings();

  BasicSymbolsLocalIncMapping getBasicSymbolsIncMapping();

  BasicSymbolsIncMapping getOriginalBasicSymbolsIncMapping();
}
