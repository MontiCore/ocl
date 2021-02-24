package de.monticore.ocl.optionaloperators.prettyprint;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.prettyprint.CommonExpressionsPrettyPrinter;
import de.monticore.expressions.prettyprint.ExpressionsBasisPrettyPrinter;
import de.monticore.ocl.optionaloperators.OptionalOperatorsMill;
import de.monticore.ocl.optionaloperators._visitor.OptionalOperatorsTraverser;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.prettyprint.MCBasicsPrettyPrinter;

public class OptionalOperatorsFullPrettyPrinter {

  protected OptionalOperatorsTraverser traverser;

  protected IndentPrinter printer;

  public OptionalOperatorsFullPrettyPrinter() {
    this(new IndentPrinter());
  }

  public OptionalOperatorsFullPrettyPrinter(IndentPrinter printer) {
    this.printer = printer;
    traverser = OptionalOperatorsMill.traverser();

    OptionalOperatorsPrettyPrinter optPP = new OptionalOperatorsPrettyPrinter(printer);
    CommonExpressionsPrettyPrinter cePP = new CommonExpressionsPrettyPrinter(printer);
    ExpressionsBasisPrettyPrinter ebPP = new ExpressionsBasisPrettyPrinter(printer);
    MCBasicsPrettyPrinter mcbPP = new MCBasicsPrettyPrinter(printer);

    traverser.setOptionalOperatorsHandler(optPP);
    traverser.add4CommonExpressions(cePP);
    traverser.setCommonExpressionsHandler(cePP);
    traverser.add4ExpressionsBasis(ebPP);
    traverser.setExpressionsBasisHandler(ebPP);
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

  public OptionalOperatorsTraverser getTraverser() {
    return traverser;
  }

  public void setTraverser(OptionalOperatorsTraverser traverser) {
    this.traverser = traverser;
  }
}
