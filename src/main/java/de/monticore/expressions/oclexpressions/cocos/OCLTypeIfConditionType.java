/*
 (c) https://github.com/MontiCore/monticore
 */

package de.monticore.expressions.oclexpressions.cocos;

import de.monticore.expressions.oclexpressions._ast.ASTOCLInstanceOfExpression;
import de.monticore.expressions.oclexpressions._ast.ASTOCLTypeIfExpression;
import de.monticore.expressions.oclexpressions._cocos.OCLExpressionsASTOCLTypeIfExpressionCoCo;
import de.se_rwth.commons.logging.Log;

public class OCLTypeIfConditionType
    implements OCLExpressionsASTOCLTypeIfExpressionCoCo {
  @Override
  public void check(ASTOCLTypeIfExpression node) {
    if (!(node.getCondition() instanceof ASTOCLInstanceOfExpression)) {
      Log.error("0xA3500 The type of the condition expression of the TypeIfExpression has to be an InstanceOfExpression, but was " + node.getCondition().getClass().getName());
    }
  }
}
