/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import com.google.common.base.Preconditions;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsHandler;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsTraverser;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsVisitor2;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.oclexpressions._ast.ASTEquivalentExpression;
import de.monticore.ocl.types.check.OCLTypeCalculator;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.TypeCheckResult;
import de.se_rwth.commons.logging.Log;

public class CommonExpressionsPrinter extends AbstractPrinter implements CommonExpressionsHandler,
    CommonExpressionsVisitor2 {

  protected CommonExpressionsTraverser traverser;

  protected OCLTypeCalculator typeCalculator;

  protected IndentPrinter printer;

  protected OCLTypeCalculator getTypeCalculator() {
    return typeCalculator;
  }

  public CommonExpressionsPrinter(IndentPrinter printer, VariableNaming naming,
      OCLTypeCalculator typeCalculator) {
    Preconditions.checkNotNull(printer);
    Preconditions.checkNotNull(naming);
    Preconditions.checkNotNull(typeCalculator);
    this.printer = printer;
    this.naming = naming;
    this.typeCalculator = typeCalculator;
  }

  @Override
  public CommonExpressionsTraverser getTraverser() {
    return this.traverser;
  }

  @Override
  public void setTraverser(CommonExpressionsTraverser traverser) {
    Preconditions.checkNotNull(traverser);
    this.traverser = traverser;
  }

  public IndentPrinter getPrinter() {
    return this.printer;
  }

  @Override
  public void handle(ASTBooleanNotExpression node) {
    Preconditions.checkNotNull(node);
    node.getExpression().accept(getTraverser());
    this.getPrinter().print("Boolean ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().print(" = !");
    this.getPrinter().print(this.getNaming().getName(node.getExpression()));
    this.getPrinter().println(";");
  }

  @Override
  public void handle(ASTLogicalNotExpression node) {
    Preconditions.checkNotNull(node);
    node.getExpression().accept(getTraverser());
    this.getPrinter().print("Boolean ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().print(" = !");
    this.getPrinter().print(this.getNaming().getName(node.getExpression()));
    this.getPrinter().println(";");
  }

  @Override
  public void handle(ASTMultExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "*");
  }

  @Override
  public void handle(ASTDivideExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "/");
  }

  @Override
  public void handle(ASTModuloExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "%");
  }

  @Override
  public void handle(ASTPlusExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "+");
  }

  @Override
  public void handle(ASTMinusExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "-");
  }

  //TODO
  public void handle(ASTEquivalentExpression node) {
    Preconditions.checkNotNull(node);
    node.getLeft().accept(getTraverser());
    node.getRight().accept(getTraverser());
    this.getPrinter().print("Boolean ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().print(" = ");
    this.getPrinter().print(this.getNaming().getName(node.getLeft()));
    this.getPrinter().print(".equals(");
    this.getPrinter().print(this.getNaming().getName(node.getRight()));
    this.getPrinter().println(");");
  }

  @Override
  public void handle(ASTLessEqualExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "<=");
  }

  @Override
  public void handle(ASTGreaterEqualExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, ">=");
  }

  @Override
  public void handle(ASTLessThanExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "<");
  }

  @Override
  public void handle(ASTGreaterThanExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, ">=");
  }

  @Override
  public void handle(ASTEqualsExpression node) {
    Preconditions.checkNotNull(node);
    Preconditions.checkNotNull(node);
    node.getLeft().accept(getTraverser());
    node.getRight().accept(getTraverser());
    this.getPrinter().print("Boolean ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().print(" = ");
    this.getPrinter().print(this.getNaming().getName(node.getLeft()));
    this.getPrinter().print(".equals(");
    this.getPrinter().print(this.getNaming().getName(node.getRight()));
    this.getPrinter().println(");");
  }

  @Override
  public void handle(ASTNotEqualsExpression node) {
    Preconditions.checkNotNull(node);
    node.getLeft().accept(getTraverser());
    node.getRight().accept(getTraverser());
    this.getPrinter().print("Boolean ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().print(" = !");
    this.getPrinter().print(this.getNaming().getName(node.getLeft()));
    this.getPrinter().print(".equals(");
    this.getPrinter().print(this.getNaming().getName(node.getRight()));
    this.getPrinter().println(");");
  }

  protected void handleInfixExpression(ASTInfixExpression node, String operator) {
    Preconditions.checkNotNull(node);
    Preconditions.checkNotNull(operator);
    Preconditions.checkArgument(!operator.isEmpty());
    node.getLeft().accept(getTraverser());
    node.getRight().accept(getTraverser());
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
    this.getPrinter().print(this.getNaming().getName(node.getLeft()));
    this.getPrinter().print(" ");
    this.getPrinter().print(operator);
    this.getPrinter().print(" ");
    this.getPrinter().print(this.getNaming().getName(node.getRight()));
    this.getPrinter().println(";");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().print(" &= ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println(";");
  }

}
