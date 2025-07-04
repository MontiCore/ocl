package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.expressions.commonexpressions._ast.ASTInfixExpression;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsVisitor2;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.refadaptation.Binding;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;

public class CommonExpressionsAdaptationVisitor
        extends AbstractAdaptationVisitor<CommonExpressionsAdaptationContext>
        implements CommonExpressionsVisitor2 {

  private static final String LOG_NAME = CommonExpressionsAdaptationVisitor.class.getName();

  @Override
  public void endVisit(ASTInfixExpression expr) {
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
  public void endVisit(ASTFieldAccessExpression refExpr) {
    SymTypeExpression expressionType = TypeCheck3.typeOf(refExpr);
    Optional<FieldSymbol> sourceSymbolOpt = expressionType.getSourceInfo().getSourceSymbol()
            .filter(s -> s instanceof FieldSymbol)
            .map(s -> (FieldSymbol) s);
    if (sourceSymbolOpt.isPresent()) {
      FieldSymbol sourceSymbol = sourceSymbolOpt.get();
      System.out.println("Field Source symbol: " + sourceSymbol);
      System.out.println("symbol full name: " + sourceSymbol.getFullName());
    }

    /*
     * 2. get all result variants that were found during traversal of the expression.
     * Each entry "AdaptationVariant" holds a consistent combination of all adapted
     * sub-nodes/expressions and the bindings that were used to adapt them.
     */
    List<CommonExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refExpr);

    // 2. We can now create a new ASTEqualsExpression with the adapted left and right expressions.
    for (CommonExpressionsAdaptationVariant variant : variants) {
      ASTFieldAccessExpression adaptedExpr = refExpr.deepClone();

      // 1. use the adapted "parent expression"
      Optional<ASTExpression> parentExprAdapted = variant.getAdaptedNode(refExpr.getExpression());
      parentExprAdapted.ifPresent(adaptedExpr::setExpression);

      // 2. if we have a field symbol, get all incarnations and create variants for it
      if (sourceSymbolOpt.isPresent()) {
        FieldSymbol refFieldSymbol = sourceSymbolOpt.get();
        Optional<Binding<FieldSymbol>> binding = variant.getOOSymbolsBindings().getBinding(refFieldSymbol);
        if (binding.isPresent()) {
          // a field binding attached to a ASTFieldAccessExpression is always required to be strict (??)
          FieldSymbol fieldSymbolInc = binding.get().getStrictConcreteElement();
          adaptedExpr.setName(fieldSymbolInc.getName());
        } else {
          // This is not an error. it is completely normal for fields that are not declared in the
          // reference model
          Log.debug("No binding found for FieldSymbol: " + refFieldSymbol.getFullName()
                  + ". Using original field: " + refFieldSymbol, LOG_NAME);
        }
      }
      variant.setAdaptedNode(refExpr, adaptedExpr);
    }
  }
}
