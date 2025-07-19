package de.monticore.expressions.expressionsbasis;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisVisitor2;
import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.refadaptation.Binding;
import de.monticore.refadaptation.BindingConflictException;
import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;
import java.util.Set;

public class ExpressionsBasisBindingVariantsVisitor
        extends AbstractAdaptationVisitor<ExpressionsBasisAdaptationContext>
        implements ExpressionsBasisVisitor2 {

  private static final String LOG_NAME = ExpressionsBasisBindingVariantsVisitor.class.getName();

  @Override
  public void endVisit(ASTNameExpression refExpr) {
    /*
     * TODO If this is called for a NameExpression which is part of a CallExpression th TypeCheck
     *  will fail!
     *  -> only call TypeCheck for valid expression -> should traverse of ASTCallExpression already stop this visit from being called?
     */
    SymTypeExpression expressionType = TypeCheck3.typeOf(refExpr);
    Optional<VariableSymbol> sourceSymbolOpt = expressionType.getSourceInfo().getSourceSymbol()
            .filter(s -> s instanceof VariableSymbol)
            .map(s -> (VariableSymbol) s);

    // TODO add support for FunctionSymbol/MethodSymbol here -> NameExpression can be part of method call

    // TODO What symbols do we even expect here?
    /*
     * 1. name expressions can point to fields of a class if we process an invariant
     * 2. name expressions can point to fields of a class if we process an operation constraint
     * 3. name expressions can point to parameters of an operation if we process an operation constraint
     * ...
     */

    /*
     * TODO Decide / discuss where we need to do this translation from variable symbols in OCL scope to CD4C symbols
     *  here?
     *  I think we should only try to do the lookup in OOSymbols if we have a VariableSymbol && it can be translated to a CD4Code symbol
     *  -> but then we would have a tight coupling in ExpressionBasisAdapter to CD4CodeMill
     *  ALTERNATIVE:
     *  - add an "adapter" class around the incarnating mapping that translates the VariableSymbol to a FieldSymbol
     */
    // TODO Also handle method parameter variable symbols ! -> this should be covered by using resolveVariable instead of resolveField
    Optional<VariableSymbol> cd4cTranslatedSymbolOpt = sourceSymbolOpt.flatMap(s -> CD4CodeMill.globalScope().resolveVariable(s.getFullName()));

    if (cd4cTranslatedSymbolOpt.isPresent()) {
      // If we have a VariableSymbol, get all incarnations and create variants for it
      VariableSymbol refVarSymbol = cd4cTranslatedSymbolOpt.get();
      System.out.println("variable Source symbol: " + refVarSymbol);
      System.out.println("symbol full name: " + refVarSymbol.getFullName());

      Set<VariableSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping().getIncarnations(refVarSymbol);
      if (incarnations.isEmpty()) {
        // no field symbol, use the constraints from the parent expression
        getAdaptations4Ast().addVariant(refExpr, getAdaptationContext().createVariant());
      } else {
        // we have the incarnations which are possible in this context
        for (VariableSymbol variableIncarnation : incarnations) {
          ExpressionsBasisAdaptationVariant newVariant = getAdaptationContext().createVariant();
          // 1. Add strict binding for the selected variable
          // (Implicitly adds type bindings for variable type)
          try {
            newVariant.getBasicSymbolsBindings().addVariableBinding(Binding.createStrict(refVarSymbol, variableIncarnation));
          } catch (BindingConflictException e) {
            // This is unexpected as the current adaptation context should only return incarnations
            // that are valid in the current context, i.e., no conflicts with existing bindings.
            Log.warn("getIncarnations returned incarnation that conflicts with existing binding: "
                    + variableIncarnation.getFullName() + " in " + refExpr.get_SourcePositionStart(), e);
            continue;
          }
          // 2. Add bindings from the original model attached to the method
          // TODO WIP
          //
          BasicSymbolsBindings bindingsFromModel = getAdaptationContext().getOriginalBasicSymbolsIncMapping().getScopedBindings(variableIncarnation);
          try {
            newVariant.getBasicSymbolsBindings().addAll(bindingsFromModel);
          } catch (BindingConflictException e) {
            // This is expected as some bindings implied by the incarnation may not be compatibl
            // with the existing bindings in the adaptation context.
            // We ignore this incarnation. Example: employee.firstName == employeeBuilder.lastName
            // TODO switch to debug level
            Log.info("Ignoring incarnation due to binding conflict: "
                    + variableIncarnation.getFullName() + " in " + refExpr.get_SourcePositionStart(), LOG_NAME);
            continue;
          }
          getAdaptations4Ast().addVariant(refExpr, newVariant);
        }
      }
    } else {
      // no VariableSymbol, should not be adapted and just deepCloned
      // TODO Pass single variant upwards? How do we handle default cases with do adaptable code? .-> look at traverse, e.g. in EqualsExpression
      // alternative: a singleton instance EmptyVariant ??
      // TODO should we split variant and adapted AST nodes ?? or keep it mixed?
      getAdaptations4Ast().addVariant(refExpr, getAdaptationContext().createVariant());
    }
  }

  @Override
  public void visit(ASTLiteralExpression node) {
    // TODO Either we do not process these at all and introduce the convention:
    // - If no binding variant is present -> just use the reference node
    // OR
    // - we return an atomic "empty binding variant" and pass this upwards
    // TODO We need to decide in "traverse(AST...)" what we do if the list is empty -> default variant or is this a conflict?
    getAdaptations4Ast().addVariant(node, getAdaptationContext().createVariant());
  }
}
