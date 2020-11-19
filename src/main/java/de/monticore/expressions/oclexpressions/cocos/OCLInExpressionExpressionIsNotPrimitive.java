/*
 (c) https://github.com/MontiCore/monticore
 */

package de.monticore.expressions.oclexpressions.cocos;

import de.monticore.expressions.oclexpressions._ast.ASTOCLInExpression;
import de.monticore.expressions.oclexpressions._cocos.OCLExpressionsASTOCLInExpressionCoCo;
import de.monticore.types.mcbasictypes._ast.ASTMCPrimitiveType;
import de.se_rwth.commons.logging.Log;

public class OCLInExpressionExpressionIsNotPrimitive
    implements OCLExpressionsASTOCLInExpressionCoCo {
  @Override
  public void check(ASTOCLInExpression node) {
    if (node.getExpression() instanceof ASTMCPrimitiveType) {
      Log.error("0xA3600 The type of the expression of the InExpression can't be a primitive type, but was " + node.getExpression().getClass().getName());
    }
  }
}
