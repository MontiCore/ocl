// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types.check;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.optionaloperators._ast.*;
import de.monticore.ocl.optionaloperators._visitor.OptionalOperatorsHandler;
import de.monticore.ocl.optionaloperators._visitor.OptionalOperatorsTraverser;
import de.monticore.ocl.util.LogHelper;
import de.monticore.types.check.AbstractDeriveFromExpression;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;

import java.util.Optional;

import static de.monticore.types.check.TypeCheck.compatible;
import static de.monticore.types.check.TypeCheck.isBoolean;

public class DeriveSymTypeOfOptionalOperators
  extends AbstractDeriveFromExpression
  implements OptionalOperatorsHandler {

  private OptionalOperatorsTraverser traverser;

  @Override
  public void traverse(ASTOptionalExpressionPrefix node){
    Optional<SymTypeExpression> optionalResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getLeft(), "0xA0300");
    Optional<SymTypeExpression> exprResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getRight(), "0xA0301");
    Optional<SymTypeExpression> wholeResult = optionalResult.isPresent()?
    OCLTypeCheck.unwrapOptional(optionalResult.get()) : Optional.empty();

    //check compatibility of type of optional and expression
    if(wholeResult.isPresent() && exprResult.isPresent() && !OCLTypeCheck.compatible(wholeResult.get(), exprResult.get())){
      LogHelper.error(node, "0xA0330", "types of OptionalExpressionPrefix are not compatible!");
    }
    
    storeResultOrLogError(wholeResult, node, "0xA0302");
  }

  @Override
  public void traverse(ASTOptionalLessEqualExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeCompareOptional(node.getRight(), node.getLeft());
    storeResultOrLogError(wholeResult, node, "0xA0303");
  }

  @Override
  public void traverse(ASTOptionalGreaterEqualExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeCompareOptional(node.getRight(), node.getLeft());
    storeResultOrLogError(wholeResult, node, "0xA0304");
  }

  @Override
  public void traverse(ASTOptionalLessThanExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeCompareOptional(node.getRight(), node.getLeft());
    storeResultOrLogError(wholeResult, node, "0xA0305");
  }

  @Override
  public void traverse(ASTOptionalGreaterThanExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeCompareOptional(node.getRight(), node.getLeft());
    storeResultOrLogError(wholeResult, node, "0xA0306");
  }

  @Override
  public void traverse(ASTOptionalEqualsExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeLogicalOptional(node.getRight(), node.getLeft());
    storeResultOrLogError(wholeResult, node, "0xA0307");
  }

  @Override
  public void traverse(ASTOptionalNotEqualsExpression node){
    Optional<SymTypeExpression> wholeResult = calculateTypeLogicalOptional(node.getRight(), node.getLeft());
    storeResultOrLogError(wholeResult, node, "0xA0308");
  }

  @Override
  public void traverse(ASTOptionalSimilarExpression node){
    //no compatiblity check necessary, therefore only check for optional
    Optional<SymTypeExpression> leftResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getLeft(), "0xA0241");
    if(!leftResult.isPresent() || !OCLTypeCheck.unwrapOptional(leftResult.get()).isPresent()) {
      LogHelper.error(node, "0xA0331", "Couldn't determine type of Optional");
    }
    acceptThisAndReturnSymTypeExpressionOrLogError(node.getRight(), "0xA0242");
    Optional<SymTypeExpression> wholeResult = Optional.of(SymTypeExpressionFactory.createPrimitive("boolean"));
    storeResultOrLogError(wholeResult, node, "0xA0307");
  }

  @Override
  public void traverse(ASTOptionalNotSimilarExpression node){
    //no compatiblity check necessary, therefore only check for optional
    Optional<SymTypeExpression> leftResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getLeft(), "0xA0241");
    if(!leftResult.isPresent() || !OCLTypeCheck.unwrapOptional(leftResult.get()).isPresent()) {
      LogHelper.error(node, "0xA0332", "Couldn't determine type of Optional");
    }
    acceptThisAndReturnSymTypeExpressionOrLogError(node.getRight(), "0xA0242");
    Optional<SymTypeExpression> wholeResult = Optional.of(SymTypeExpressionFactory.createPrimitive("boolean"));
    storeResultOrLogError(wholeResult, node, "0xA0308");
  }

  private Optional<SymTypeExpression> calculateTypeCompareOptional(ASTExpression right, ASTExpression left) {
    Optional<SymTypeExpression> leftResult = acceptThisAndReturnSymTypeExpressionOrLogError(left, "0xA0241");
    //check that leftResult is of type Optional
    if(leftResult.isPresent() && OCLTypeCheck.unwrapOptional(leftResult.get()).isPresent()){
      leftResult = Optional.of(OCLTypeCheck.unwrapOptional(leftResult.get()).get());
    }
    else{
      return Optional.empty();
    }
    Optional<SymTypeExpression> rightResult = acceptThisAndReturnSymTypeExpressionOrLogError(right, "0xA0242");
    if (rightResult.isPresent()) {
      return calculateTypeCompareOptional(rightResult.get(), leftResult.get());
    } else {
      typeCheckResult.reset();
      return Optional.empty();
    }
  }

  protected Optional<SymTypeExpression> calculateTypeCompareOptional(SymTypeExpression rightResult, SymTypeExpression leftResult) {
    if (isNumericType(rightResult) && isNumericType(leftResult)) {
      return Optional.of(SymTypeExpressionFactory.createPrimitive("boolean"));
    }
    return Optional.empty();
  }

  private Optional<SymTypeExpression> calculateTypeLogicalOptional(ASTExpression right, ASTExpression left) {
    Optional<SymTypeExpression> leftResult = acceptThisAndReturnSymTypeExpressionOrLogError(left, "0xA0244");
    //check that leftResult is of type Optional
    if(leftResult.isPresent() && OCLTypeCheck.unwrapOptional(leftResult.get()).isPresent()){
      leftResult = Optional.of(OCLTypeCheck.unwrapOptional(leftResult.get()).get());
    }
    else{
      return Optional.empty();
    }
    Optional<SymTypeExpression> rightResult = acceptThisAndReturnSymTypeExpressionOrLogError(right, "0xA0245");
    if (leftResult.isPresent() && rightResult.isPresent()) {
      return calculateTypeLogicalOptional(rightResult.get(), leftResult.get());
    } else {
      typeCheckResult.reset();
      return Optional.empty();
    }
  }

  protected Optional<SymTypeExpression> calculateTypeLogicalOptional(SymTypeExpression rightResult, SymTypeExpression leftResult) {
    //Option one: they are both numeric types
    if (isNumericType(leftResult) && isNumericType(rightResult)
            || isBoolean(leftResult) && isBoolean(rightResult)) {
      return Optional.of(SymTypeExpressionFactory.createPrimitive("boolean"));
    }
    //Option two: none of them is a primitive type and they are either the same type or in a super/sub type relation
    if (!leftResult.isPrimitive() && !rightResult.isPrimitive() &&
            (compatible(leftResult, rightResult) || compatible(rightResult, leftResult))
    ) {
      return Optional.of(SymTypeExpressionFactory.createPrimitive("boolean"));
    }
    //should never happen, no valid result, error will be handled in traverse
    return Optional.empty();
  }

  @Override public OptionalOperatorsTraverser getTraverser() {
    return traverser;
  }

  @Override public void setTraverser(
    OptionalOperatorsTraverser traverser) {
    this.traverser = traverser;
  }
}
