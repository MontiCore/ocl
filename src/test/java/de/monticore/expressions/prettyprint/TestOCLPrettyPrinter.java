/*
 (c) https://github.com/MontiCore/monticore
 */

package de.monticore.expressions.prettyprint;

import de.monticore.expressions.testoclexpressions._visitor.TestOCLExpressionsVisitor;
import de.monticore.prettyprint.IndentPrinter;

public class TestOCLPrettyPrinter extends ExpressionsBasisPrettyPrinter implements TestOCLExpressionsVisitor {

  private TestOCLExpressionsVisitor realThis;

  public TestOCLPrettyPrinter(IndentPrinter printer) {
    super(printer);
  }

  @Override
  public void setRealThis(TestOCLExpressionsVisitor realThis) {
    this.realThis = realThis;
  }

  @Override
  public TestOCLExpressionsVisitor getRealThis() {
    return realThis;
  }
}
