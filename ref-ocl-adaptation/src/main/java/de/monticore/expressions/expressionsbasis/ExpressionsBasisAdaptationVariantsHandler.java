package de.monticore.expressions.expressionsbasis;

import de.monticore.expressions.expressionsbasis._ast.ASTArguments;

/*
 * TODO Separate handler from binding variants visitor?
 *  -> BindingVariantsVisitor should only handle the binding variants
 *  -> Handler defines general traversal and basic like clearing variants before traversal
 */
/**
 * Handwritten traversal behavior for the ExpressionBasis language.
 */
public class ExpressionsBasisAdaptationVariantsHandler extends ExpressionsBasisAdaptationVariantsHandlerTOP {

  @Override
  public void traverse(ASTArguments arguments) {
    traverseForConsistentVariants(arguments, arguments.getExpressionList());
  }
}
