/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.ocl._cocos;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._cocos.ExpressionsBasisASTExpressionCoCo;
import de.monticore.types.check.IDerive;

import java.util.Optional;

public class ExpressionValidCoCo implements ExpressionsBasisASTExpressionCoCo {

  protected ASTExpression top;

  protected IDerive deriver;

  public ExpressionValidCoCo(IDerive deriver) {
    this.deriver = deriver;
  }

  protected Optional<ASTExpression> getTop() {
    return Optional.ofNullable(top);
  }

  protected void setTop(ASTExpression top) {
    this.top = top;
  }

  protected IDerive getDeriver() {
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
