/* (c) https://github.com/MontiCore/monticore */
package de.monticore.types.check;

import de.monticore.expressions.testoclexpressions._ast.ASTOCLExtType;
import de.monticore.expressions.testoclexpressions._visitor.TestOCLExpressionsVisitor;

/**
 * Visitor for BitExpressions
 */
public class DeriveSymTypeOfTestOCLExpressions extends DeriveSymTypeOfExpression
    implements TestOCLExpressionsVisitor {

  private TestOCLExpressionsVisitor realThis;

  @Override
  public void setRealThis(TestOCLExpressionsVisitor realThis) {
    this.realThis = realThis;
  }

  @Override
  public TestOCLExpressionsVisitor getRealThis() {
    return realThis;
  }

  public DeriveSymTypeOfTestOCLExpressions() {
    realThis = this;
  }

  public void setLastResult(LastResult lastResult) {
    this.lastResult = lastResult;
  }

  @Override
  public void traverse(ASTOCLExtType node) {
    node.accept(getRealThis());
  }

}
