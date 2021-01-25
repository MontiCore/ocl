// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.oclexpressions.prettyprint;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.prettyprint.ExpressionsBasisPrettyPrinter;
import de.monticore.ocl.oclexpressions.OCLExpressionsMill;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.prettyprint.MCBasicsPrettyPrinter;

public class OCLExpressionsFullPrettyPrinter {
  protected OCLExpressionsTraverser traverser;

  protected IndentPrinter printer;

  public OCLExpressionsFullPrettyPrinter() {
    this(new IndentPrinter());
  }

  public OCLExpressionsFullPrettyPrinter(IndentPrinter printer) {
    this.printer = printer;
    traverser = OCLExpressionsMill.traverser();

    OCLExpressionsPrettyPrinter oclPP = new OCLExpressionsPrettyPrinter(printer);
    ExpressionsBasisPrettyPrinter expPP = new ExpressionsBasisPrettyPrinter(printer);
    MCBasicsPrettyPrinter mcbPP = new MCBasicsPrettyPrinter(printer);

    traverser.setOCLExpressionsHandler(oclPP);
    traverser.add4ExpressionsBasis(expPP);
    traverser.setExpressionsBasisHandler(expPP);
    traverser.add4MCBasics(mcbPP);
  }

  public String prettyprint(ASTExpression node) {
    this.getPrinter().clearBuffer();
    node.accept(traverser);
    return this.getPrinter().getContent();
  }

  /* ============================================================ */
  /* ======================= GENERATED CODE ===================== */
  /* ============================================================ */

  public IndentPrinter getPrinter() {
    return printer;
  }

  public void setPrinter(IndentPrinter printer) {
    this.printer = printer;
  }

  public OCLExpressionsTraverser getTraverser() {
    return traverser;
  }

  public void setTraverser(OCLExpressionsTraverser traverser) {
    this.traverser = traverser;
  }
}
