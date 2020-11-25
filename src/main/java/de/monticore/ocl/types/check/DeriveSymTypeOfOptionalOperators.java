package de.monticore.ocl.types.check;

import de.monticore.expressions.commonexpressions._ast.ASTInfixExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.expressions.optionaloperators._ast.*;
import de.monticore.ocl.expressions.optionaloperators._visitor.OptionalOperatorsVisitor;
import de.monticore.types.check.DeriveSymTypeOfExpression;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.TypeCheck;

import java.util.Optional;

public class DeriveSymTypeOfOptionalOperators extends DeriveSymTypeOfExpression implements OptionalOperatorsVisitor {

  private OptionalOperatorsVisitor realThis;

  public DeriveSymTypeOfOptionalOperators(){
    this.realThis = this;
  }

  @Override
  public OptionalOperatorsVisitor getRealThis(){
    return realThis;
  }

  @Override
  public void setRealThis(OptionalOperatorsVisitor realThis) {
    this.realThis = realThis;
  }

  @Override
  public void traverse(ASTOptionalExpressionPrefix node){
    SymTypeExpression conditionResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getLeft(), "0xA0300");
    SymTypeExpression exprResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getRight(), "0xA0301");
    Optional<SymTypeExpression> wholeResult = Optional.empty();
    //condition has to be boolean
    if (TypeCheck.isBoolean(conditionResult)) {
      wholeResult = Optional.of(exprResult);
    }
    storeResultOrLogError(wholeResult, node, "0xA0302");
  }

  @Override
  public void traverse(ASTOptionalLessEqualExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeCompare(node, node.getRight(), node.getLeft());;
    storeResultOrLogError(wholeResult, node, "0xA0303");
  }

  @Override
  public void traverse(ASTOptionalGreaterEqualExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeCompare(node, node.getRight(), node.getLeft());;
    storeResultOrLogError(wholeResult, node, "0xA0304");
  }

  @Override
  public void traverse(ASTOptionalLessThanExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeCompare(node, node.getRight(), node.getLeft());;
    storeResultOrLogError(wholeResult, node, "0xA0305");
  }

  @Override
  public void traverse(ASTOptionalGreaterThanExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeCompare(node, node.getRight(), node.getLeft());;
    storeResultOrLogError(wholeResult, node, "0xA0306");
  }

  @Override
  public void traverse(ASTOptionalEqualsExpression node){
    //TODO: Compatibility check for OptionalEquals and OptionalNotEqualsExpression
    Optional<SymTypeExpression> wholeResult = calculateTypeLogical(node, node.getRight(), node.getLeft());;
    storeResultOrLogError(wholeResult, node, "0xA0307");
  }

  @Override
  public void traverse(ASTOptionalNotEqualsExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeLogical(node, node.getRight(), node.getLeft());;
    storeResultOrLogError(wholeResult, node, "0xA0308");
  }

  @Override
  public void traverse(ASTOptionalSimilarExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeLogical(node, node.getRight(), node.getLeft());;
    storeResultOrLogError(wholeResult, node, "0xA0307");
  }

  @Override
  public void traverse(ASTOptionalNotSimilarExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeLogical(node, node.getRight(), node.getLeft());;
    storeResultOrLogError(wholeResult, node, "0xA0308");
  }

  private Optional<SymTypeExpression> calculateTypeCompare(ASTInfixExpression expr, ASTExpression right, ASTExpression left) {
    SymTypeExpression leftResult = acceptThisAndReturnSymTypeExpressionOrLogError(left, "0xA0241");
    SymTypeExpression rightResult = acceptThisAndReturnSymTypeExpressionOrLogError(right, "0xA0242");
    return calculateTypeCompare(expr, rightResult, leftResult);
  }

  protected Optional<SymTypeExpression> calculateTypeCompare(ASTInfixExpression expr, SymTypeExpression rightResult, SymTypeExpression leftResult) {
    if (isNumericType(rightResult)) {
      return Optional.of(SymTypeExpressionFactory.createTypeConstant("boolean"));
    }
    return Optional.empty();
  }

  private Optional<SymTypeExpression> calculateTypeLogical(ASTInfixExpression expr, ASTExpression right, ASTExpression left) {
    SymTypeExpression leftResult = acceptThisAndReturnSymTypeExpressionOrLogError(left, "0xA0244");
    SymTypeExpression rightResult = acceptThisAndReturnSymTypeExpressionOrLogError(right, "0xA0245");
    return calculateTypeLogical(expr, rightResult, leftResult);
  }

  protected Optional<SymTypeExpression> calculateTypeLogical(ASTInfixExpression expr, SymTypeExpression rightResult, SymTypeExpression leftResult) {
    if (isNumericType(rightResult) || TypeCheck.isBoolean(rightResult)) {
      return Optional.of(SymTypeExpressionFactory.createTypeConstant("boolean"));
    }
    if (!rightResult.isTypeConstant()) {
      return Optional.of(SymTypeExpressionFactory.createTypeConstant("boolean"));
    }
    return Optional.empty();
  }
}
