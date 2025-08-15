package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.commonexpressions._ast.ASTInfixExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;

import java.util.List;
import java.util.Optional;

public class CommonExpressionsASTAdaptationVisitor
        extends CommonExpressionsASTAdaptationVisitorTOP {

  @Override
  public void endVisit(ASTInfixExpression expr) {
    /*
     * TODO Remove once we generate the "adapt" method for each AST node!
     *  This is a quick way to define the adaptation for all infix expressions in a "handwritten way"
     */
    /*
     * Get all result variants that were found during traversal of the expression.
     * Each entry "AdaptationVariant" holds a consistent combination of all adapted
     * sub-nodes/expressions and the bindings that were used to adapt them.
     */
    List<ICommonExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(expr);

    // Create a new ASTInfixExpression for each variant with the adapted left and right expressions.
    for (ICommonExpressionsAdaptationVariant variant : variants) {
      ASTInfixExpression adaptedExpr = expr.deepClone();

      Optional<ASTExpression> leftAdapted = variant.getAdaptedNode(expr.getLeft());
      leftAdapted.ifPresent(adaptedExpr::setLeft);
      Optional<ASTExpression> rightAdapted = variant.getAdaptedNode(expr.getRight());
      rightAdapted.ifPresent(adaptedExpr::setRight);

      // store adapted expression in variant
      variant.setAdaptedNode(expr, adaptedExpr);
    }
  }
}
