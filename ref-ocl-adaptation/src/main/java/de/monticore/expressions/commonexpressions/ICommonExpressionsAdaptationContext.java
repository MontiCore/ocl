package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.expressionsbasis.IExpressionsBasisAdaptationContext;
import de.monticore.refadaptation.BindingConflictException;
import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.symbols.IOOSymbolsBindings;
import de.monticore.symbols.IOOSymbolsIncMapping;
import de.monticore.symbols.IOOSymbolsLocalIncMapping;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.se_rwth.commons.SourcePosition;

public interface ICommonExpressionsAdaptationContext extends IAdaptationContext, IExpressionsBasisAdaptationContext {

  // ==========================================================
  // Methods from IAdaptationContext redefined for type safety
  // ===========================================================

  ICommonExpressionsAdaptationVariant createVariant();

  ICommonExpressionsAdaptationContext fork();

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  IOOSymbolsBindings getOOSymbolsBindings();

  IOOSymbolsLocalIncMapping getOOSymbolsIncMapping();

  IOOSymbolsIncMapping getOriginalOOSymbolsIncMapping();

  @Override
  default ICommonExpressionsAdaptationVariant createVariantForIncarnation(
          VariableSymbol referenceSymbol,
          VariableSymbol incarnation,
          SourcePosition sourcePosition) throws BindingConflictException {
    return (ICommonExpressionsAdaptationVariant) IExpressionsBasisAdaptationContext
            .super.createVariantForIncarnation(referenceSymbol, incarnation, sourcePosition);
  }

  @Override
  default ICommonExpressionsAdaptationVariant createVariantForIncarnation(
          FunctionSymbol referenceSymbol,
          FunctionSymbol incarnation,
          SourcePosition sourcePosition) throws BindingConflictException {
    return (ICommonExpressionsAdaptationVariant) IExpressionsBasisAdaptationContext
            .super.createVariantForIncarnation(referenceSymbol, incarnation, sourcePosition);
  }
}
