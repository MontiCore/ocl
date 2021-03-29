// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.setexpressions.prettyprint;

import de.monticore.expressions.prettyprint.ExpressionsBasisPrettyPrinter;
import de.monticore.ocl.setexpressions.SetExpressionsMill;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsTraverser;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.prettyprint.MCBasicsPrettyPrinter;

public class SetExpressionsFullPrettyPrinter {

  protected SetExpressionsTraverser traverser;

  protected IndentPrinter printer;

  public SetExpressionsFullPrettyPrinter() {
    this(new IndentPrinter());
  }

  public SetExpressionsFullPrettyPrinter(IndentPrinter printer) {
    this.printer = printer;

    traverser = SetExpressionsMill.traverser();

    SetExpressionsPrettyPrinter sePP = new SetExpressionsPrettyPrinter(printer);
    ExpressionsBasisPrettyPrinter expPP = new ExpressionsBasisPrettyPrinter(printer);
    MCBasicsPrettyPrinter mcbPP = new MCBasicsPrettyPrinter(printer);

    traverser.setSetExpressionsHandler(sePP);
    traverser.add4ExpressionsBasis(expPP);
    traverser.setExpressionsBasisHandler(expPP);
    traverser.add4MCBasics(mcbPP);
  }
}
