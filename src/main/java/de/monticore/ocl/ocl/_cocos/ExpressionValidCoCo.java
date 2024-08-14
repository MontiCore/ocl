/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.ocl._cocos;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._cocos.ExpressionsBasisASTExpressionCoCo;
import de.monticore.types3.TypeCheck3;
import java.util.Optional;

public class ExpressionValidCoCo implements ExpressionsBasisASTExpressionCoCo {

  protected ASTExpression top;

  protected Optional<ASTExpression> getTop() {
    return Optional.ofNullable(top);
  }

  protected void setTop(ASTExpression top) {
    this.top = top;
  }

  @Override
  public void endVisit(ASTExpression expr) {
    Optional<ASTExpression> top = this.getTop();
    if (top.isPresent() && top.get() == expr) {
      this.setTop(null);
    }
  }

  @Override
  public void check(ASTExpression expr) {
    Optional<ASTExpression> top = this.getTop();
    if (!top.isPresent()) {
      TypeCheck3.typeOf(expr);
      this.setTop(expr);
    }
  }
}
