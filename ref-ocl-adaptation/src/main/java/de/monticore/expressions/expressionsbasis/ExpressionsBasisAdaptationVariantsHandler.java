package de.monticore.expressions.expressionsbasis;

import de.monticore.expressions.expressionsbasis._ast.ASTArguments;

/**
 * Handwritten traversal behavior for the ExpressionBasis language.
 */
public class ExpressionsBasisAdaptationVariantsHandler extends ExpressionsBasisAdaptationVariantsHandlerTOP {

  @Override
  public void traverse(ASTArguments arguments) {
    traverseForConsistentVariants(arguments, arguments.getExpressionList());
  }
}
