package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.expressions.commonexpressions._ast.ASTInfixExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.refadaptation.Binding;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symboltable.ISymbol;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;

public class CommonExpressionsASTAdaptationVisitor
        extends CommonExpressionsASTAdaptationVisitorTOP {

  private static final String LOG_NAME = CommonExpressionsASTAdaptationVisitor.class.getName();

  @Override
  public void endVisit(ASTInfixExpression expr) {
    /*
     * TODO Once we generate the "adapt" method for each AST node this is a quick way to define the adaptation
     *  for all infix expressions in a "handwritten way"
     */
    /*
     * Get all result variants that were found during traversal of the expression.
     * Each entry "AdaptationVariant" holds a consistent combination of all adapted
     * sub-nodes/expressions and the bindings that were used to adapt them.
     */
    List<CommonExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(expr);

    // Create a new ASTInfixExpression for each variant with the adapted left and right expressions.
    for (CommonExpressionsAdaptationVariant variant : variants) {
      ASTInfixExpression adaptedExpr = expr.deepClone();

      Optional<ASTExpression> leftAdapted = variant.getAdaptedNode(expr.getLeft());
      leftAdapted.ifPresent(adaptedExpr::setLeft);
      Optional<ASTExpression> rightAdapted = variant.getAdaptedNode(expr.getRight());
      rightAdapted.ifPresent(adaptedExpr::setRight);

      // store adapted expression in variant
      variant.setAdaptedNode(expr, adaptedExpr);
    }
  }

  @Override
  protected ASTFieldAccessExpression adapt(ASTFieldAccessExpression original, CommonExpressionsAdaptationVariant variant) {
    /*
     * 1. Reuse the default generated adaptation logic which takes care of using lower level adapted
     * nodes or deepCloning.
     */
    ASTFieldAccessExpression adapted = super.adapt(original, variant);

    // 2. Get the source symbol for the field name
    Optional<ISymbol> sourceSymbolOpt = TypeCheck3.typeOf(original).getSourceInfo().getSourceSymbol();

    if (sourceSymbolOpt.isPresent()) {
      // 3. adapt name depending on the symbol kind
      if (sourceSymbolOpt.get() instanceof VariableSymbol) {
        adaptFieldName(adapted, variant, (VariableSymbol) sourceSymbolOpt.get());
      } else if (sourceSymbolOpt.get() instanceof FunctionSymbol) {
        /*
         * This is required as FieldAccessExpressions are also used to represent method calls as
         * part of a CallExpression. Thus, the name of the FieldAccessExpression can be a
         * FunctionSymbol as well.
         */
        adaptFieldName(adapted, variant, (FunctionSymbol) sourceSymbolOpt.get());
      }
    }
    return adapted;
  }

  /**
   * Adapts the field name in a FieldAccessExpression if a variant has a binding for the given
   * variable symbol.
   *
   * @param adapted the adapted ASTFieldAccessExpression
   * @param variant the variant that si currently processed
   * @param refVariableSymbol the VariableSymbol that is referenced in the ASTFieldAccessExpression
   */
  private void adaptFieldName(ASTFieldAccessExpression adapted, CommonExpressionsAdaptationVariant variant, VariableSymbol refVariableSymbol) {
    Optional<Binding<VariableSymbol>> binding = variant.getOOSymbolsBindings().getBinding(refVariableSymbol);
    if (binding.isPresent()) {
      // a variable binding attached to a ASTFieldAccessExpression is always required to be strict (??)
      VariableSymbol fieldSymbolInc = binding.get().getStrictConcreteElement();
      Log.debug("Adapting field name: " + adapted.getName()
              + " to " + fieldSymbolInc.getName() + " in " + adapted.get_SourcePositionStart(), LOG_NAME);
      adapted.setName(fieldSymbolInc.getName());
    } else {
      // This is not an error. it is completely normal for fields that are not declared in the
      // reference model
      Log.debug("No binding found for VariableSymbol: " + refVariableSymbol.getFullName()
              + ". Using original field name: " + refVariableSymbol, LOG_NAME);
    }
  }

  /**
   * Adapts the field name in a FieldAccessExpression if a variant has a binding for the given
   * FunctionSymbol.
   *
   * @param adapted the adapted ASTFieldAccessExpression
   * @param variant the variant that is currently processed
   * @param refFunctionSymbol the FunctionSymbol that is referenced in the ASTFieldAccessExpression
   */
  private void adaptFieldName(ASTFieldAccessExpression adapted, CommonExpressionsAdaptationVariant variant, FunctionSymbol refFunctionSymbol) {
    Optional<Binding<FunctionSymbol>> binding = variant.getOOSymbolsBindings().getBinding(refFunctionSymbol);
    if (binding.isPresent()) {
      // a function binding attached to a ASTFieldAccessExpression is always required to be strict (??)
      FunctionSymbol fieldSymbolInc = binding.get().getStrictConcreteElement();
      Log.debug("Adapting method name: " + adapted.getName()
              + " to " + fieldSymbolInc.getName() + " in " + adapted.get_SourcePositionStart(), LOG_NAME);
      adapted.setName(fieldSymbolInc.getName());
    } else {
      // This is not an error. it is completely normal for fields that are not declared in the
      // reference model
      Log.debug("No binding found for FunctionSymbol " + refFunctionSymbol.getFullName()
              + ". Using original method name: " + refFunctionSymbol, LOG_NAME);
    }
  }
}
