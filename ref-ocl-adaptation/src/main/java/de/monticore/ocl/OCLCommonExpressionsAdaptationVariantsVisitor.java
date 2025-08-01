package de.monticore.ocl;

import de.monticore.cdconcretization.util.SymbolUtil;
import de.monticore.expressions.commonexpressions.CommonExpressionsAdaptationVariantsVisitor;
import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;

import java.util.Optional;

/**
 * OCL-specific additions to the variant discovery for the CommonExpressions language.
 * This visitor resolves symbols in the original reference model of the incarnation mapping
 * to ensure that the correct symbols are used for the incarnation lookup.
 */
public class OCLCommonExpressionsAdaptationVariantsVisitor extends CommonExpressionsAdaptationVariantsVisitor {

  /*
   * Some symbols in the OCL reference model are located in the OCL global scope and not some same
   * instances as the related symbols in the CD4Code scope. In order for the incarnation mapping
   * to work, we try to resolve the symbol name in the scope of the reference model.
   * If it is found, we use the symbol from the reference model if no we proceed with the
   * original symbol as there could still be bindings defined for.it.
   *
   * NOTES for how we resolve the symbols:
   * - we use "resolveDown" as we are only interested in symbols from the reference model.
   *   Otherwise, we might get errors because of multiple symbols with same name!
   * - we have to use the "internal qualified name" (full name without diagram name). Otherwise,
   *   "resolveDown" will not enter the sub scopes of the reference model. It only checks scopes
   *   where the simple name matches the first part of the qualified name to be resoled!
   */

  @Override
  protected void addVariantsForEachVariableIncarnation(ASTFieldAccessExpression refExpr, VariableSymbol refVariableSymbol) {
    // maybe the variable symbols needs to be translated to a CD4Code symbol first?
    Optional<VariableSymbol> cd4cTranslatedSymbolOpt = getAdaptationContext()
            .getOriginalBasicSymbolsIncMapping().getReferenceScope()
            .resolveVariableDown(SymbolUtil.getFullNameWithoutCD(refVariableSymbol));
    if (cd4cTranslatedSymbolOpt.isPresent()) {
      super.addVariantsForEachVariableIncarnation(refExpr, cd4cTranslatedSymbolOpt.get());
    } else {
      // normal handling as defined for ExpressionsBasis language
      super.addVariantsForEachVariableIncarnation(refExpr, refVariableSymbol);
    }
  }

  @Override
  protected void addVariantsForEachFunctionIncarnation(ASTFieldAccessExpression refExpr, FunctionSymbol refFunctionSymbol) {
    // maybe the function symbols needs to be translated to a CD4Code symbol first?
    Optional<FunctionSymbol> cd4cTranslatedSymbolOpt = getAdaptationContext()
            .getOriginalBasicSymbolsIncMapping().getReferenceScope()
            .resolveFunctionDown(SymbolUtil.getFullNameWithoutCD(refFunctionSymbol));
    if (cd4cTranslatedSymbolOpt.isPresent()) {
      super.addVariantsForEachFunctionIncarnation(refExpr, cd4cTranslatedSymbolOpt.get());
    } else {
      // normal handling as defined for ExpressionsBasis language
      super.addVariantsForEachFunctionIncarnation(refExpr, refFunctionSymbol);
    }
  }
}
