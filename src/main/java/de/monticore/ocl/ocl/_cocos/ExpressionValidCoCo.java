/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.ocl._cocos;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._cocos.ExpressionsBasisASTExpressionCoCo;
import de.monticore.ocl.types.check.OCLDeriver;

import java.util.Optional;

public class ExpressionValidCoCo implements ExpressionsBasisASTExpressionCoCo {

  protected ASTExpression top;

  protected OCLDeriver deriver;

  public ExpressionValidCoCo(OCLDeriver deriver) {
    this.deriver = deriver;
  }

  protected Optional<ASTExpression> getTop() {
    return Optional.ofNullable(top);
  }

  protected void setTop(ASTExpression top) {
    this.top = top;
  }

  protected OCLDeriver getDeriver() {
    return this.deriver;
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
      this.getDeriver().deriveType(expr);
      this.setTop(expr);
    }
  }
}
