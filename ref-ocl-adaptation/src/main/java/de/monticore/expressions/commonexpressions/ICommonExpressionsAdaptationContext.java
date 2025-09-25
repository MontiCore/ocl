package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.expressionsbasis.IExpressionsBasisAdaptationContext;
import de.monticore.expressions.expressionsbasis.IExpressionsBasisAdaptationVariant;
import de.monticore.refmodel.Binding;
import de.monticore.refmodel.BindingConflictException;
import de.monticore.refadapt.IAdaptationContext;
import de.monticore.symbols.oosymbols.refmodel.IOOSymbolsBindings;
import de.monticore.symbols.oosymbols.refmodel.IOOSymbolsIncMapping;
import de.monticore.symbols.oosymbols.refmodel.IOOSymbolsLocalIncMapping;
import de.monticore.symbols.basicsymbols.refmodel.IBasicSymbolsBindings;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.symboltable.ISymbol;
import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Log;

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
          TypeSymbol referenceSymbol,
          TypeSymbol incarnation,
          SourcePosition sourcePosition) throws BindingConflictException {
    return (ICommonExpressionsAdaptationVariant) IExpressionsBasisAdaptationContext
            .super.createVariantForIncarnation(referenceSymbol, incarnation, sourcePosition);
  }

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

  /**
   * Creates a new adaptation variant for the given incarnation. Specifically this:
   * <ul>
   *   <li>Adds a binding for the reference symbol to the incarnation.</li>
   *   <li>Adds all bindings attached to the incarnation in the original incarnation mapping</li>
   * </ul>
   * @param referenceSymbol the reference symbol
   * @param incarnation the incarnation of the reference symbol
   * @param sourcePosition for debugging purposes
   * @return a new adaptation variant with the specified bindings
   * @throws BindingConflictException if the binding conflicts with existing bindings in the context
   *      i.e. the variant cannot be created and higher level code should ignore this incarnation
   */
  default ICommonExpressionsAdaptationVariant createVariantForIncarnation(
          MethodSymbol referenceSymbol,
          MethodSymbol incarnation,
          SourcePosition sourcePosition) throws BindingConflictException {
    ICommonExpressionsAdaptationVariant newVariant = createVariant();
    // 1. Add strict binding for the selected function
    try {
      newVariant.getOOSymbolsBindings().addMethodBinding(Binding.createStrict(referenceSymbol, incarnation));
    } catch (BindingConflictException e) {
      // This is unexpected as the current adaptation context should only return incarnations
      // that are valid in the current context, i.e., no conflicts with existing bindings.
      Log.warn("getIncarnations returned incarnation that conflicts with existing binding: "
              + incarnation.getFullName() + " in " + sourcePosition, e);
      throw e;
    }
    // 2. Add bindings from the original model attached to the method
    addOriginalBindings(newVariant,incarnation, sourcePosition);
    return newVariant;
  }

  /**
   * Adds all bindings to the given variant that are attached to the given context symbol in the
   * original incarnation mapping.
   *
   * @param variant the variant to add the bindings to
   * @param contextSymbol the context symbol for which the bindings should be added
   * @param sourcePosition for debugging purposes
   * @throws BindingConflictException if the bindings conflict with existing bindings in the variant
   */
  default void addOriginalBindings(
          IExpressionsBasisAdaptationVariant variant,
          ISymbol contextSymbol,
          SourcePosition sourcePosition)
          throws BindingConflictException {
    IBasicSymbolsBindings bindingsFromModel = getOriginalOOSymbolsIncMapping().getScopedBindings(contextSymbol);
    try {
      variant.getBasicSymbolsBindings().addAll(bindingsFromModel);
    } catch (BindingConflictException e) {
      // This is expected as some bindings implied by the incarnation may not be compatible
      // with the existing bindings in the adaptation context.
      // We ignore this incarnation. Example: employee.firstName == employeeBuilder.lastName
      Log.debug("Ignoring incarnation due to binding conflict: " + contextSymbol.getFullName()
              + " in " + sourcePosition, ICommonExpressionsAdaptationContext.class.getName());
      throw e;
    }
  }
}
