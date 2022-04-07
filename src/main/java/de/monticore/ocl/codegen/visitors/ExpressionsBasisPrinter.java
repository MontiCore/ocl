/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import com.google.common.base.Preconditions;
import de.monticore.expressions.expressionsbasis._ast.*;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisHandler;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisTraverser;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisVisitor2;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.types.check.OCLTypeCalculator;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.TypeCheckResult;
import de.se_rwth.commons.logging.Log;

public class ExpressionsBasisPrinter extends AbstractPrinter implements ExpressionsBasisHandler,
    ExpressionsBasisVisitor2 {

  protected ExpressionsBasisTraverser traverser;

  protected IndentPrinter printer;

  public ExpressionsBasisPrinter(IndentPrinter printer, VariableNaming naming,
      OCLTypeCalculator typeCalculator) {
    Preconditions.checkNotNull(printer);
    Preconditions.checkNotNull(naming);
    Preconditions.checkNotNull(typeCalculator);
    this.printer = printer;
    this.naming = naming;
    this.typeCalculator = typeCalculator;
  }

  @Override
  public ExpressionsBasisTraverser getTraverser() {
    return this.traverser;
  }

  @Override
  public void setTraverser(ExpressionsBasisTraverser traverser) {
    Preconditions.checkNotNull(traverser);
    this.traverser = traverser;
  }

  public IndentPrinter getPrinter() {
    return this.printer;
  }

  @Override
  public void handle(ASTNameExpression node) {
    Preconditions.checkNotNull(node);
    TypeCheckResult type = this.getTypeCalculator().deriveType(node);
    if (!type.isPresentCurrentResult()) {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
    }
    else {
      getPrinter().print(type.getCurrentResult().print());
      getPrinter().print(" ");
    }
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().print(" = ");
    this.getPrinter().print(node.getName());
    this.getPrinter().println(";");
  }

  @Override
  public void handle(ASTLiteralExpression node) {
    Preconditions.checkNotNull(node);
    TypeCheckResult type = this.getTypeCalculator().deriveType(node);
    if (!type.isPresentCurrentResult()) {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
    }
    else {
      this.getPrinter().print(type.getCurrentResult().print());
      this.getPrinter().print(" ");
    }
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().print(" = ");
    node.getLiteral().accept(getTraverser());
    this.getPrinter().println(";");
  }

}
