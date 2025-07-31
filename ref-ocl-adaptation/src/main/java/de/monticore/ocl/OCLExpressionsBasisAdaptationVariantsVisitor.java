package de.monticore.ocl;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationVariant;
import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationVariantsVisitor;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.refadaptation.Binding;
import de.monticore.refadaptation.BindingConflictException;
import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;
import java.util.Set;

/**
 * OCL-specific additions to the variant discovery for the ExpressionsBasis language.
 */
public class OCLExpressionsBasisAdaptationVariantsVisitor extends ExpressionsBasisAdaptationVariantsVisitor {

  @Override
  protected void addVariantsForVariableSymbol(ASTNameExpression refExpr, VariableSymbol variableSymbol) {
    /*
     * TODO Decide / discuss where we need to do this translation from variable symbols in OCL scope to CD4C symbols
     *  here?
     *  I think we should only try to do the lookup in OOSymbols if we have a VariableSymbol && it can be translated to a CD4Code symbol
     *  -> but then we would have a tight coupling in ExpressionBasisAdapter to CD4CodeMill
     *  ALTERNATIVE:
     *  - add an "adapter" class around the incarnating mapping that translates the VariableSymbol to a FieldSymbol
     */
    // TODO NEW idea: move this translation logic to a special 'OCLExpressionsBasisAdaptationVariantsVisitor' that overrides the behavior

    Set<VariableSymbol> varIncarnations = getAdaptationContext().getBasicSymbolsIncMapping()
            .getIncarnations(variableSymbol);
    if (varIncarnations.isEmpty()) {
      // maybe the variable symbols needs to be translated to a CD4Code symbol first?
      // TODO CD4CodeMill.globalScope() vs. getAdaptationContext().getOriginalBasicSymbolsIncMapping()
      Optional<VariableSymbol> cd4cTranslatedSymbolOpt = CD4CodeMill.globalScope()
              .resolveVariable(variableSymbol.getFullName());
      if (cd4cTranslatedSymbolOpt.isPresent()) {
        super.addVariantsForVariableSymbol(refExpr, cd4cTranslatedSymbolOpt.get());
      }
    }
    // normal handling as defined for ExpressionsBasis language
    super.addVariantsForVariableSymbol(refExpr, variableSymbol);
  }
}
