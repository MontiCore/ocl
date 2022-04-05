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
import de.monticore.types.check.TypeCheckResult;
import de.se_rwth.commons.logging.Log;

public class CommonExpressionsPrinter extends AbstractPrinter implements CommonExpressionsHandler,
  CommonExpressionsVisitor2 {

  protected CommonExpressionsTraverser traverser;
  protected OCLTypeCalculator typeCalculator;

  protected OCLTypeCalculator getTypeCalculator() {
    return typeCalculator;
  }

  public CommonExpressionsPrinter(StringBuilder stringBuilder, VariableNaming naming, OCLTypeCalculator typeCalculator) {
    Preconditions.checkNotNull(stringBuilder);
    Preconditions.checkNotNull(naming);
    Preconditions.checkNotNull(typeCalculator);
    this.stringBuilder = stringBuilder;
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

  @Override
  public void endVisit(ASTBooleanNotExpression node) {
    Preconditions.checkNotNull(node);
    this.getStringBuilder().append("Boolean ").append(this.getNaming().getName(node));
    this.getStringBuilder().append(" = !");
    this.getStringBuilder().append(this.getNaming().getName(node.getExpression()));
    this.getStringBuilder().append(";\n");
  }

  @Override
  public void endVisit(ASTLogicalNotExpression node) {
    Preconditions.checkNotNull(node);
    this.getStringBuilder().append("Boolean ").append(this.getNaming().getName(node));
    this.getStringBuilder().append(" = !");
    this.getStringBuilder().append(this.getNaming().getName(node.getExpression()));
    this.getStringBuilder().append(";\n");
  }

  @Override
  public void endVisit(ASTMultExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "*");
  }

  @Override
  public void endVisit(ASTDivideExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "/");
  }

  @Override
  public void endVisit(ASTModuloExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "%");
  }

  @Override
  public void endVisit(ASTPlusExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "+");
  }

  @Override
  public void endVisit(ASTMinusExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "-");
  }

  //TODO
  public void endVisit(ASTEquivalentExpression node) {
    Preconditions.checkNotNull(node);
    this.getStringBuilder().append("Boolean ")
      .append(this.getNaming().getName(node))
      .append(" = ")
      .append(this.getNaming().getName(node.getLeft()))
      .append(".equals(")
      .append(this.getNaming().getName(node.getRight()))
      .append(")").append(";").append(System.lineSeparator());
  }

  @Override
  public void endVisit(ASTLessEqualExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "<=");
  }

  @Override
  public void endVisit(ASTGreaterEqualExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, ">=");
  }

  @Override
  public void endVisit(ASTLessThanExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "<");
  }

  @Override
  public void endVisit(ASTGreaterThanExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, ">=");
  }

  @Override
  public void endVisit(ASTEqualsExpression node) {
    Preconditions.checkNotNull(node);
    Preconditions.checkNotNull(node);
    this.getStringBuilder().append("Boolean ")
      .append(this.getNaming().getName(node))
      .append(" = ")
      .append(this.getNaming().getName(node.getLeft()))
      .append(".equals(")
      .append(this.getNaming().getName(node.getRight()))
      .append(")").append(";").append(System.lineSeparator());
  }

  @Override
  public void endVisit(ASTNotEqualsExpression node) {
    Preconditions.checkNotNull(node);
    this.getStringBuilder().append("Boolean ")
      .append(this.getNaming().getName(node))
      .append(" = !")
      .append(this.getNaming().getName(node.getLeft()))
      .append(".equals(")
      .append(this.getNaming().getName(node.getRight()))
      .append(")").append(";").append(System.lineSeparator());
  }

  protected void handleInfixExpression(ASTInfixExpression node, String operator) {
    Preconditions.checkNotNull(node);
    Preconditions.checkNotNull(operator);
    Preconditions.checkArgument(!operator.isEmpty());
    TypeCheckResult type = this.getTypeCalculator().deriveType(node);
    if (!type.isPresentCurrentResult()) {
      Log.error("Could not calculate type of expression", node.get_SourcePositionStart());
    } else {
      this.getStringBuilder().append(type.getCurrentResult()).append(" ")
        .append(this.getNaming().getName(node));
      this.getStringBuilder().append(" = ");
      this.getStringBuilder().append(this.getNaming().getName(node.getLeft()));
      this.getStringBuilder().append(" ").append(operator).append(" ");
      this.getStringBuilder().append(this.getNaming().getName(node.getRight()));
      this.getStringBuilder().append(";\n");
      this.getStringBuilder().append(this.getNaming().getName(node)).append(" &= ");
      this.getStringBuilder().append(this.getNaming().getName(node));
      this.getStringBuilder().append(";\n");
    }
  }
}
