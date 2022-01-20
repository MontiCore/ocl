/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen;

import com.google.common.base.Preconditions;
import de.monticore.expressions.commonexpressions._ast.ASTBooleanNotExpression;
import de.monticore.expressions.commonexpressions._ast.ASTInfixExpression;
import de.monticore.expressions.commonexpressions._ast.ASTLogicalNotExpression;
import de.monticore.expressions.commonexpressions._ast.ASTMultExpression;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsHandler;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsVisitor2;

public class OCL2JavaCommonExpressions implements CommonExpressionsHandler, CommonExpressionsVisitor2 {

  protected StringBuilder stringBuilder;

  protected StringBuilder getStringBuilder() {
    return this.stringBuilder;
  }

  protected OCLVariableNaming naming;

  protected OCLVariableNaming getNaming() {
    return this.naming;
  }

  public OCL2JavaCommonExpressions(StringBuilder stringBuilder, OCLVariableNaming naming) {
    Preconditions.checkNotNull(stringBuilder);
    Preconditions.checkNotNull(naming);
    this.stringBuilder = stringBuilder;
    this.naming = naming;
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
    handleInfixExpression(node, "*", "times");
  }

  protected void handleInfixExpression(ASTInfixExpression node, String operator) {
    handleInfixExpression(node, operator, "");
  }

  protected void handleInfixExpression(ASTInfixExpression node, String operator, String amountMethod) {
    Preconditions.checkNotNull(node);
    Preconditions.checkNotNull(operator);
    Preconditions.checkNotNull(amountMethod);
    Preconditions.checkArgument(!operator.isEmpty());
    this.getStringBuilder().append("Dummy").append(" ").append(this.getNaming().getName(node));
    this.getStringBuilder().append(" = ");
    this.getStringBuilder().append(this.getNaming().getName(node.getLeft()));
    this.getStringBuilder().append(" ").append(operator).append(" ");
    this.getStringBuilder().append(this.getNaming().getName(node.getRight()));
    this.getStringBuilder().append(";\n");
  }
}