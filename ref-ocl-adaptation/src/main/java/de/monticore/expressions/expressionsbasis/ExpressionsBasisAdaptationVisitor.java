package de.monticore.expressions.expressionsbasis;

import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisVisitor2;
import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.refadaptation.Binding;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;

public class ExpressionsBasisAdaptationVisitor
        extends AbstractAdaptationVisitor<ExpressionsBasisAdaptationContext>
        implements ExpressionsBasisVisitor2 {

  private static final String LOG_NAME = ExpressionsBasisAdaptationVisitor.class.getName();

  @Override
  public void endVisit(ASTNameExpression refExpr) {
    SymTypeExpression expressionType = TypeCheck3.typeOf(refExpr);
    Optional<VariableSymbol> sourceSymbolOpt = expressionType.getSourceInfo().getSourceSymbol()
            .filter(s -> s instanceof VariableSymbol)
            .map(s -> (VariableSymbol) s);
    if (sourceSymbolOpt.isPresent()) {
      VariableSymbol sourceSymbol = sourceSymbolOpt.get();
      System.out.println("Variable Source symbol: " + sourceSymbol);
      System.out.println("symbol full name: " + sourceSymbol.getFullName());
    }

    /*
     * 2. get all result variants that were found during traversal of the expression.
     * Each entry "AdaptationVariant" holds a consistent combination of all adapted
     * sub-nodes/expressions and the bindings that were used to adapt them.
     */
    List<ExpressionsBasisAdaptationVariant> variants = getAdaptations4Ast().getVariants(refExpr);

    // 2. We can now create a new ASTEqualsExpression with the adapted left and right expressions.
    for (ExpressionsBasisAdaptationVariant variant : variants) {
      ASTNameExpression adaptedExpr = refExpr.deepClone();

      if (sourceSymbolOpt.isPresent()) {
        VariableSymbol refFieldSymbol = sourceSymbolOpt.get();
        Optional<Binding<VariableSymbol>> binding = variant.getBasicSymbolsBindings().getBinding(refFieldSymbol);
        if (binding.isPresent()) {
          // a variable binding attached to a ASTFieldAccessExpression is always required to be strict (??)
          VariableSymbol fieldSymbolInc = binding.get().getStrictConcreteElement();
          adaptedExpr.setName(fieldSymbolInc.getName());
        } else {
          // This is not an error. it is completely normal for fields that are not declared in the
          // reference model
          Log.debug("No binding found for VariableSymol: " + refFieldSymbol.getFullName()
                  + ". Using original variable: " + refFieldSymbol, LOG_NAME);
        }
      }
      variant.setAdaptedNode(refExpr, adaptedExpr);
    }
  }
}
