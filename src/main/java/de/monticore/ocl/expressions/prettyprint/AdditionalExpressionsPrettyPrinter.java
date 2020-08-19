/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.expressions.prettyprint;

import de.monticore.ocl.expressions.additionalexpressions._visitor.AdditionalExpressionsVisitor;
import de.monticore.prettyprint.IndentPrinter;

public class AdditionalExpressionsPrettyPrinter
    implements AdditionalExpressionsVisitor {

  protected AdditionalExpressionsVisitor realThis;
  protected IndentPrinter printer;

  public AdditionalExpressionsPrettyPrinter(IndentPrinter printer) {
    this.printer = printer;
    realThis = this;
  }

  public IndentPrinter getPrinter() {
    return printer;
  }

  @Override
  public void setRealThis(AdditionalExpressionsVisitor realThis) {
    this.realThis = realThis;
  }

  @Override
  public AdditionalExpressionsVisitor getRealThis() {
    return realThis;
  }
}
