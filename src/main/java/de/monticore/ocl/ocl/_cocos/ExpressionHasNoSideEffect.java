// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._cocos;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._cocos.ExpressionsBasisASTExpressionCoCo;
import de.monticore.ocl.util.SideEffectFreeExpressions;
import de.se_rwth.commons.logging.Log;

public class ExpressionHasNoSideEffect implements ExpressionsBasisASTExpressionCoCo {
  @Override
  public void check(ASTExpression astExpression) {
    if (!SideEffectFreeExpressions.sideEffectFree(astExpression)) {
      Log.error(
          String.format(
              "0xOCL21 Expressions can't have side effects, Expression %s is not allowed.",
              astExpression));
    }
  }
}
