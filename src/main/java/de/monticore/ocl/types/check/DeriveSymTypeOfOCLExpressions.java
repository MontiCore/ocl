package de.monticore.ocl.types.check;

import de.monticore.ocl.expressions.oclexpressions._visitor.OCLExpressionsVisitor;
import de.monticore.types.check.DeriveSymTypeOfExpression;

public class DeriveSymTypeOfOCLExpressions extends DeriveSymTypeOfExpression implements OCLExpressionsVisitor {

  private OCLExpressionsVisitor realThis;

  public DeriveSymTypeOfOCLExpressions() {
    this.realThis = this;
  }

  @Override
  public void setRealThis(OCLExpressionsVisitor realThis) {
    this.realThis = realThis;
  }

  @Override
  public OCLExpressionsVisitor getRealThis() {
    return realThis;
  }
}
