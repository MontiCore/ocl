package de.monticore.expressions.expressionsbasis;

import de.monticore.refadaptation.Binding;
import de.monticore.refadaptation.BindingConflictException;
import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.symbols.basicsymbols.IBasicSymbolsBindings;
import de.monticore.symbols.basicsymbols.IBasicSymbolsIncMapping;
import de.monticore.symbols.basicsymbols.IBasicSymbolsLocalIncMapping;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Log;

public interface IExpressionsBasisAdaptationContext extends IAdaptationContext {

  // ==========================================================
  // Methods from IAdaptationContext redefined for type safety
  // ===========================================================

  IExpressionsBasisAdaptationVariant createVariant();

  IExpressionsBasisAdaptationContext fork();

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  IBasicSymbolsBindings getBasicSymbolsBindings();

  IBasicSymbolsLocalIncMapping getBasicSymbolsIncMapping();

  IBasicSymbolsIncMapping getOriginalBasicSymbolsIncMapping();

  // NOTE: This can be generated for any symbol in each incarnation mapping this language depends on
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
  default IExpressionsBasisAdaptationVariant createVariantForIncarnation(
          VariableSymbol referenceSymbol,
          VariableSymbol incarnation,
          SourcePosition sourcePosition) throws BindingConflictException {
    IExpressionsBasisAdaptationVariant newVariant = createVariant();
    // 1. Add strict binding for the selected variable
    // (Implicitly adds type bindings for variable type)
    try {
      newVariant.getBasicSymbolsBindings().addVariableBinding(Binding.createStrict(referenceSymbol, incarnation));
    } catch (BindingConflictException e) {
      // This is unexpected as the current adaptation context should only return incarnations
      // that are valid in the current context, i.e., no conflicts with existing bindings.
      Log.warn("getIncarnations returned incarnation that conflicts with existing binding: "
              + incarnation.getFullName() + " in " + sourcePosition, e);
      throw e;
    }
    // 2. Add bindings from the original model attached to the method
    IBasicSymbolsBindings bindingsFromModel = getOriginalBasicSymbolsIncMapping().getScopedBindings(incarnation);
    try {
      newVariant.getBasicSymbolsBindings().addAll(bindingsFromModel);
    } catch (BindingConflictException e) {
      // This is expected as some bindings implied by the incarnation may not be compatible
      // with the existing bindings in the adaptation context.
      // We ignore this incarnation. Example: employee.firstName == employeeBuilder.lastName
      Log.debug("Ignoring incarnation due to binding conflict: " + incarnation.getFullName()
              + " in " + sourcePosition, IExpressionsBasisAdaptationContext.class.getName());
      throw e;
    }
    return newVariant;
  }

  // NOTE: This can be generated for any symbol in each incarnation mapping this language depends on
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
  default IExpressionsBasisAdaptationVariant createVariantForIncarnation(
          FunctionSymbol referenceSymbol,
          FunctionSymbol incarnation,
          SourcePosition sourcePosition) throws BindingConflictException {
    IExpressionsBasisAdaptationVariant newVariant = createVariant();
    // 1. Add strict binding for the selected variable
    // (Implicitly adds type bindings for variable type)
    try {
      newVariant.getBasicSymbolsBindings().addFunctionBinding(Binding.createStrict(referenceSymbol, incarnation));
    } catch (BindingConflictException e) {
      // This is unexpected as the current adaptation context should only return incarnations
      // that are valid in the current context, i.e., no conflicts with existing bindings.
      Log.warn("getIncarnations returned incarnation that conflicts with existing binding: "
              + incarnation.getFullName() + " in " + sourcePosition, e);
      throw e;
    }
    // 2. Add bindings from the original model attached to the method
    IBasicSymbolsBindings bindingsFromModel = getOriginalBasicSymbolsIncMapping().getScopedBindings(incarnation);
    try {
      newVariant.getBasicSymbolsBindings().addAll(bindingsFromModel);
    } catch (BindingConflictException e) {
      // This is expected as some bindings implied by the incarnation may not be compatible
      // with the existing bindings in the adaptation context.
      // We ignore this incarnation. Example: employee.firstName == employeeBuilder.lastName
      Log.debug("Ignoring incarnation due to binding conflict: " + incarnation.getFullName()
              + " in " + sourcePosition, IExpressionsBasisAdaptationContext.class.getName());
      throw e;
    }
    return newVariant;
  }
}
