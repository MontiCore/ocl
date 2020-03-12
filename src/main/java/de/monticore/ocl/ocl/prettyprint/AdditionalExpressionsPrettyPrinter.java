/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl.prettyprint;

import de.monticore.ocl.additionalexpressions._visitor.AdditionalExpressionsVisitor;
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
  public AdditionalExpressionsVisitor getRealThis() {
    return realThis;
  }
}
