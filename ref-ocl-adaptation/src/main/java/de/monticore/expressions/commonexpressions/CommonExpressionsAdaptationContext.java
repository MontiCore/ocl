package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationContext;
import de.monticore.refadaptation.BindingConflictException;
import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.symbols.OOSymbolsBindings;
import de.monticore.symbols.OOSymbolsIncMapping;
import de.monticore.symbols.OOSymbolsLocalIncMapping;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.se_rwth.commons.SourcePosition;

public interface CommonExpressionsAdaptationContext extends IAdaptationContext, ExpressionsBasisAdaptationContext {

  // ==========================================================
  // Methods from IAdaptationContext redefined for type safety
  // ===========================================================

  CommonExpressionsAdaptationVariant createVariant();

  CommonExpressionsAdaptationContext fork();

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  OOSymbolsBindings getOOSymbolsBindings();

  OOSymbolsLocalIncMapping getOOSymbolsIncMapping();

  OOSymbolsIncMapping getOriginalOOSymbolsIncMapping();

  @Override
  default CommonExpressionsAdaptationVariant createVariantForIncarnation(
          VariableSymbol referenceSymbol,
          VariableSymbol incarnation,
          SourcePosition sourcePosition) throws BindingConflictException {
    return (CommonExpressionsAdaptationVariant) ExpressionsBasisAdaptationContext
            .super.createVariantForIncarnation(referenceSymbol, incarnation, sourcePosition);
  }

  @Override
  default CommonExpressionsAdaptationVariant createVariantForIncarnation(
          FunctionSymbol referenceSymbol,
          FunctionSymbol incarnation,
          SourcePosition sourcePosition) throws BindingConflictException {
    return (CommonExpressionsAdaptationVariant) ExpressionsBasisAdaptationContext
            .super.createVariantForIncarnation(referenceSymbol, incarnation, sourcePosition);
  }
}
