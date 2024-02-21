// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types.check;

import static de.monticore.types.check.TypeCheck.compatible;
import static de.monticore.types.check.TypeCheck.isBoolean;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.optionaloperators._ast.*;
import de.monticore.ocl.optionaloperators._visitor.OptionalOperatorsHandler;
import de.monticore.ocl.optionaloperators._visitor.OptionalOperatorsTraverser;
import de.monticore.ocl.util.LogHelper;
import de.monticore.types.check.AbstractDeriveFromExpression;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;

/**
 * @deprecated This class is no longer acceptable since we use <b>Type Check 3</b> to calculate the
 *     type of expressions and literals related to OCL. Use OptionalOperatorsTypeVisitor instead.
 */
@Deprecated(forRemoval = true)
public class DeriveSymTypeOfOptionalOperators extends AbstractDeriveFromExpression
    implements OptionalOperatorsHandler {

  private OptionalOperatorsTraverser traverser;

  @Override
  public void traverse(ASTOptionalExpressionPrefix node) {
    SymTypeExpression optionalResult = acceptThisAndReturnSymTypeExpression(node.getLeft());
    SymTypeExpression exprResult = acceptThisAndReturnSymTypeExpression(node.getRight());
    if (!optionalResult.isObscureType() && !exprResult.isObscureType()) {
      SymTypeExpression wholeResult = OCLTypeCheck.unwrapOptional(optionalResult);

      // check compatibility of type of optional and expression
      if (!wholeResult.isObscureType() && !OCLTypeCheck.compatible(wholeResult, exprResult)) {
        LogHelper.error(node, "0xA0330", "types of OptionalExpressionPrefix are not compatible!");
      }

      storeResultOrLogError(wholeResult, node, "0xA0302");
    } else {
      getTypeCheckResult().reset();
      getTypeCheckResult().setResult(SymTypeExpressionFactory.createObscureType());
    }
  }

  @Override
  public void traverse(ASTOptionalLessEqualExpression node) {
    SymTypeExpression wholeResult = calculateTypeCompareOptional(node.getRight(), node.getLeft());
    storeResultOrLogError(wholeResult, node, "0xA0303");
  }

  @Override
  public void traverse(ASTOptionalGreaterEqualExpression node) {
    SymTypeExpression wholeResult = calculateTypeCompareOptional(node.getRight(), node.getLeft());
    storeResultOrLogError(wholeResult, node, "0xA0304");
  }

  @Override
  public void traverse(ASTOptionalLessThanExpression node) {
    SymTypeExpression wholeResult = calculateTypeCompareOptional(node.getRight(), node.getLeft());
    storeResultOrLogError(wholeResult, node, "0xA0305");
  }

  @Override
  public void traverse(ASTOptionalGreaterThanExpression node) {
    SymTypeExpression wholeResult = calculateTypeCompareOptional(node.getRight(), node.getLeft());
    storeResultOrLogError(wholeResult, node, "0xA0306");
  }

  @Override
  public void traverse(ASTOptionalEqualsExpression node) {
    SymTypeExpression wholeResult = calculateTypeLogicalOptional(node.getRight(), node.getLeft());
    storeResultOrLogError(wholeResult, node, "0xA0307");
  }

  @Override
  public void traverse(ASTOptionalNotEqualsExpression node) {
    SymTypeExpression wholeResult = calculateTypeLogicalOptional(node.getRight(), node.getLeft());
    storeResultOrLogError(wholeResult, node, "0xA0308");
  }

  @Override
  public void traverse(ASTOptionalSimilarExpression node) {
    // no compatiblity check necessary, therefore only check for optional
    SymTypeExpression leftResult = acceptThisAndReturnSymTypeExpression(node.getLeft());
    SymTypeExpression rightResult = acceptThisAndReturnSymTypeExpression(node.getRight());
    if (!leftResult.isObscureType() && !rightResult.isObscureType()) {
      if (OCLTypeCheck.unwrapOptional(leftResult).isObscureType()) {
        LogHelper.error(node, "0xA0331", "Couldn't determine type of Optional");
      }

      SymTypeExpression wholeResult = SymTypeExpressionFactory.createPrimitive("boolean");
      storeResultOrLogError(wholeResult, node, "0xA0307");
    } else {
      getTypeCheckResult().reset();
      getTypeCheckResult().setResult(SymTypeExpressionFactory.createObscureType());
    }
  }

  @Override
  public void traverse(ASTOptionalNotSimilarExpression node) {
    // no compatiblity check necessary, therefore only check for optional
    SymTypeExpression leftResult = acceptThisAndReturnSymTypeExpression(node.getLeft());
    SymTypeExpression rightResult = acceptThisAndReturnSymTypeExpression(node.getRight());
    if (!leftResult.isObscureType() && !rightResult.isObscureType()) {
      if (OCLTypeCheck.unwrapOptional(leftResult).isObscureType()) {
        LogHelper.error(node, "0xA0332", "Couldn't determine type of Optional");
      }

      SymTypeExpression wholeResult = SymTypeExpressionFactory.createPrimitive("boolean");
      storeResultOrLogError(wholeResult, node, "0xA0308");
    } else {
      getTypeCheckResult().reset();
      getTypeCheckResult().setResult(SymTypeExpressionFactory.createObscureType());
    }
  }

  private SymTypeExpression calculateTypeCompareOptional(ASTExpression right, ASTExpression left) {
    SymTypeExpression leftResult = acceptThisAndReturnSymTypeExpression(left);
    SymTypeExpression rightResult = acceptThisAndReturnSymTypeExpression(right);
    // check that leftResult is of type Optional
    if (!OCLTypeCheck.unwrapOptional(leftResult).isObscureType()) {
      leftResult = OCLTypeCheck.unwrapOptional(leftResult);
    } else {
      return SymTypeExpressionFactory.createObscureType();
    }
    return calculateTypeCompareOptional(rightResult, leftResult);
  }

  protected SymTypeExpression calculateTypeCompareOptional(
      SymTypeExpression rightResult, SymTypeExpression leftResult) {
    if (isNumericType(rightResult) && isNumericType(leftResult)) {
      return SymTypeExpressionFactory.createPrimitive("boolean");
    }
    return SymTypeExpressionFactory.createObscureType();
  }

  private SymTypeExpression calculateTypeLogicalOptional(ASTExpression right, ASTExpression left) {
    SymTypeExpression leftResult = acceptThisAndReturnSymTypeExpression(left);
    SymTypeExpression rightResult = acceptThisAndReturnSymTypeExpression(right);
    // check that leftResult is of type Optional
    if (!OCLTypeCheck.unwrapOptional(leftResult).isObscureType()) {
      leftResult = OCLTypeCheck.unwrapOptional(leftResult);
    } else {
      return SymTypeExpressionFactory.createObscureType();
    }
    return calculateTypeLogicalOptional(rightResult, leftResult);
  }

  protected SymTypeExpression calculateTypeLogicalOptional(
      SymTypeExpression rightResult, SymTypeExpression leftResult) {
    // Option one: they are both numeric types
    if (isNumericType(leftResult) && isNumericType(rightResult)
        || isBoolean(leftResult) && isBoolean(rightResult)) {
      return SymTypeExpressionFactory.createPrimitive("boolean");
    }
    // Option two: none of them is a primitive type and they are either the same type or in a
    // super/sub type relation
    if (!leftResult.isPrimitive()
        && !rightResult.isPrimitive()
        && (compatible(leftResult, rightResult) || compatible(rightResult, leftResult))) {
      return SymTypeExpressionFactory.createPrimitive("boolean");
    }
    // should never happen, no valid result, error will be handled in traverse
    return SymTypeExpressionFactory.createObscureType();
  }

  @Override
  public OptionalOperatorsTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(OptionalOperatorsTraverser traverser) {
    this.traverser = traverser;
  }
}
