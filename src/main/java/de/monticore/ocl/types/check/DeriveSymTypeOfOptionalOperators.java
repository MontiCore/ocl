package de.monticore.ocl.types.check;

import de.monticore.expressions.commonexpressions._ast.ASTInfixExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.optionaloperators._ast.*;
import de.monticore.ocl.optionaloperators._visitor.OptionalOperatorsVisitor;
import de.monticore.types.check.DeriveSymTypeOfExpression;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

import static de.monticore.types.check.TypeCheck.compatible;
import static de.monticore.types.check.TypeCheck.isBoolean;

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
    SymTypeExpression optionalResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getLeft(), "0xA0300");
    SymTypeExpression exprResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getRight(), "0xA0301");
    Optional<SymTypeExpression> wholeResult = OCLTypeCheck.unwrapOptional(optionalResult);

    //check compatibility of type of optional and expression
    if(wholeResult.isPresent() && !OCLTypeCheck.compatible(wholeResult.get(), exprResult)){
      Log.error("types of OptionalExpressionPrefix are not compatible!");
    }
    
    storeResultOrLogError(wholeResult, node, "0xA0302");
  }

  @Override
  public void traverse(ASTOptionalLessEqualExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeCompareOptional(node, node.getRight(), node.getLeft());;
    storeResultOrLogError(wholeResult, node, "0xA0303");
  }

  @Override
  public void traverse(ASTOptionalGreaterEqualExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeCompareOptional(node, node.getRight(), node.getLeft());;
    storeResultOrLogError(wholeResult, node, "0xA0304");
  }

  @Override
  public void traverse(ASTOptionalLessThanExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeCompareOptional(node, node.getRight(), node.getLeft());;
    storeResultOrLogError(wholeResult, node, "0xA0305");
  }

  @Override
  public void traverse(ASTOptionalGreaterThanExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeCompareOptional(node, node.getRight(), node.getLeft());;
    storeResultOrLogError(wholeResult, node, "0xA0306");
  }

  @Override
  public void traverse(ASTOptionalEqualsExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeLogicalOptional(node, node.getRight(), node.getLeft());;
    storeResultOrLogError(wholeResult, node, "0xA0307");
  }

  @Override
  public void traverse(ASTOptionalNotEqualsExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeLogicalOptional(node, node.getRight(), node.getLeft());;
    storeResultOrLogError(wholeResult, node, "0xA0308");
  }

  @Override
  public void traverse(ASTOptionalSimilarExpression node){
    //no compatiblity check necessary, therefore only check for optional
    SymTypeExpression leftResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getLeft(), "0xA0241");
    if(!OCLTypeCheck.unwrapOptional(leftResult).isPresent()) {
      Log.error("Couldn't determine type of Optional");
    }
    acceptThisAndReturnSymTypeExpressionOrLogError(node.getRight(), "0xA0242");
    Optional<SymTypeExpression> wholeResult = Optional.of(SymTypeExpressionFactory.createTypeConstant("boolean"));;
    storeResultOrLogError(wholeResult, node, "0xA0307");
  }

  @Override
  public void traverse(ASTOptionalNotSimilarExpression node){
    //no compatiblity check necessary, therefore only check for optional
    SymTypeExpression leftResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getLeft(), "0xA0241");
    if(!OCLTypeCheck.unwrapOptional(leftResult).isPresent()) {
      Log.error("Couldn't determine type of Optional");
    }
    acceptThisAndReturnSymTypeExpressionOrLogError(node.getRight(), "0xA0242");
    Optional<SymTypeExpression> wholeResult = Optional.of(SymTypeExpressionFactory.createTypeConstant("boolean"));;
    storeResultOrLogError(wholeResult, node, "0xA0308");
  }

  private Optional<SymTypeExpression> calculateTypeCompareOptional(ASTInfixExpression expr, ASTExpression right, ASTExpression left) {
    SymTypeExpression leftResult = acceptThisAndReturnSymTypeExpressionOrLogError(left, "0xA0241");
    //check that leftResult is of type Optional
    if(OCLTypeCheck.unwrapOptional(leftResult).isPresent()){
      leftResult = OCLTypeCheck.unwrapOptional(leftResult).get();
    }
    else{
      return Optional.empty();
    }
    SymTypeExpression rightResult = acceptThisAndReturnSymTypeExpressionOrLogError(right, "0xA0242");
    return calculateTypeCompareOptional(expr, rightResult, leftResult);
  }

  protected Optional<SymTypeExpression> calculateTypeCompareOptional(ASTInfixExpression expr, SymTypeExpression rightResult, SymTypeExpression leftResult) {
    if (isNumericType(rightResult) && isNumericType(leftResult)) {
      return Optional.of(SymTypeExpressionFactory.createTypeConstant("boolean"));
    }
    return Optional.empty();
  }

  private Optional<SymTypeExpression> calculateTypeLogicalOptional(ASTInfixExpression expr, ASTExpression right, ASTExpression left) {
    SymTypeExpression leftResult = acceptThisAndReturnSymTypeExpressionOrLogError(left, "0xA0244");
    //check that leftResult is of type Optional
    if(OCLTypeCheck.unwrapOptional(leftResult).isPresent()){
      leftResult = OCLTypeCheck.unwrapOptional(leftResult).get();
    }
    else{
      return Optional.empty();
    }
    SymTypeExpression rightResult = acceptThisAndReturnSymTypeExpressionOrLogError(right, "0xA0245");
    return calculateTypeLogicalOptional(expr, rightResult, leftResult);
  }

  protected Optional<SymTypeExpression> calculateTypeLogicalOptional(ASTInfixExpression expr, SymTypeExpression rightResult, SymTypeExpression leftResult) {
    //Option one: they are both numeric types
    if (isNumericType(leftResult) && isNumericType(rightResult)
            || isBoolean(leftResult) && isBoolean(rightResult)) {
      return Optional.of(SymTypeExpressionFactory.createTypeConstant("boolean"));
    }
    //Option two: none of them is a primitive type and they are either the same type or in a super/sub type relation
    if (!leftResult.isTypeConstant() && !rightResult.isTypeConstant() &&
            (compatible(leftResult, rightResult) || compatible(rightResult, leftResult))
    ) {
      return Optional.of(SymTypeExpressionFactory.createTypeConstant("boolean"));
    }
    //should never happen, no valid result, error will be handled in traverse
    return Optional.empty();
  }
}
