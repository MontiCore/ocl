package de.monticore.ocl;

import de.monticore.expressions.commonexpressions.ICommonExpressionsAdaptationContext;
import de.monticore.ocl.oclexpressions.IOCLExpressionsAdaptationContext;
import de.monticore.ocl.setexpressions.ISetExpressionsAdaptationContext;
import de.monticore.refmodel.BindingConflictException;
import de.monticore.refadapt.IAdaptationContext;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.types.mccollectiontypes.IMCCollectionTypesAdaptationContext;
import de.se_rwth.commons.SourcePosition;

public interface IOCLAdaptationContext extends
        // TODO extend from all sub languages
        IAdaptationContext,
        IMCCollectionTypesAdaptationContext,
        IOCLExpressionsAdaptationContext,
        ISetExpressionsAdaptationContext,
        ICommonExpressionsAdaptationContext {

  // ==========================================================
  // Methods from IAdaptationContext redefined for type safety
  // ===========================================================

  IOCLAdaptationVariant createVariant();
  IOCLAdaptationContext fork();

  // ==============================================================
  // Language specific incarnation mappings required for adaptation
  // ==============================================================

  @Override
  default IOCLAdaptationVariant createVariantForIncarnation(
          TypeSymbol referenceSymbol,
          TypeSymbol incarnation,
          SourcePosition sourcePosition) throws BindingConflictException {
    return (IOCLAdaptationVariant) ICommonExpressionsAdaptationContext
            .super.createVariantForIncarnation(referenceSymbol, incarnation, sourcePosition);
  }

  @Override
  default IOCLAdaptationVariant createVariantForIncarnation(
          VariableSymbol referenceSymbol,
          VariableSymbol incarnation,
          SourcePosition sourcePosition) throws BindingConflictException {
    return (IOCLAdaptationVariant) ICommonExpressionsAdaptationContext
            .super.createVariantForIncarnation(referenceSymbol, incarnation, sourcePosition);
  }

  @Override
  default IOCLAdaptationVariant createVariantForIncarnation(
          FunctionSymbol referenceSymbol,
          FunctionSymbol incarnation,
          SourcePosition sourcePosition) throws BindingConflictException {
    return (IOCLAdaptationVariant) ICommonExpressionsAdaptationContext
            .super.createVariantForIncarnation(referenceSymbol, incarnation, sourcePosition);
  }

  @Override
  default IOCLAdaptationVariant createVariantForIncarnation(
          MethodSymbol referenceSymbol,
          MethodSymbol incarnation,
          SourcePosition sourcePosition) throws BindingConflictException {
    return (IOCLAdaptationVariant) ICommonExpressionsAdaptationContext
            .super.createVariantForIncarnation(referenceSymbol, incarnation, sourcePosition);
  }
}
