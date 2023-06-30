package de.monticore.ocl.types.check;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.optionaloperators._ast.*;
import de.monticore.ocl.optionaloperators._visitor.OptionalOperatorsVisitor2;
import de.monticore.ocl.util.LogHelper;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types3.AbstractTypeVisitor;
import de.monticore.types3.SymTypeRelations;

import static de.monticore.types.check.SymTypeExpressionFactory.createObscureType;

public class OptionalOperatorsTypeVisitor extends AbstractTypeVisitor
    implements OptionalOperatorsVisitor2 {
  
  protected SymTypeRelations typeRelations;
  
  protected OptionalOperatorsTypeVisitor(SymTypeRelations typeRelations) {
    this.typeRelations = typeRelations;
  }
  
  protected SymTypeRelations getTypeRel() {
    return typeRelations;
  }
  
  @Override
  public void endVisit(ASTOptionalExpressionPrefix expr) {
    var optionalResult = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    var exprResult = getType4Ast().getPartialTypeOfExpr(expr.getRight());
    
    SymTypeExpression result;
    if (optionalResult.isObscureType() || exprResult.isObscureType()) {
      result = createObscureType();
    }
    else {
      result = OCLTypeCheck.unwrapOptional(optionalResult);
      
      // check compatibility of type of optional and expression
      if (!result.isObscureType() && !OCLTypeCheck.compatible(result, exprResult)) {
        LogHelper.error(expr, "0xA0330", "types of OptionalExpressionPrefix are not compatible!");
        result = createObscureType(); // TODO MSm added
      }
    }
    
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTOptionalLessEqualExpression expr) {
    var result = calculateTypeCompareOptional(expr.getRight(), expr.getLeft());
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTOptionalGreaterEqualExpression expr) {
    var result = calculateTypeCompareOptional(expr.getRight(), expr.getLeft());
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTOptionalLessThanExpression expr) {
    var result = calculateTypeCompareOptional(expr.getRight(), expr.getLeft());
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTOptionalGreaterThanExpression expr) {
    var result = calculateTypeCompareOptional(expr.getRight(), expr.getLeft());
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTOptionalEqualsExpression expr) {
    var result = calculateTypeLogicalOptional(expr.getRight(), expr.getLeft());
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTOptionalNotEqualsExpression expr) {
    var result = calculateTypeLogicalOptional(expr.getRight(), expr.getLeft());
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTOptionalSimilarExpression expr) {
    // no compatibility check necessary, therefore only check for optional
    var leftResult = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    var rightResult = getType4Ast().getPartialTypeOfExpr(expr.getRight());
    
    SymTypeExpression result;
    if (leftResult.isObscureType() || rightResult.isObscureType()) {
      result = createObscureType();
    }
    else {
      if (OCLTypeCheck.unwrapOptional(leftResult).isObscureType()) {
        LogHelper.error(expr, "0xA0331", "Couldn't determine type of Optional");
      }
      result = SymTypeExpressionFactory.createPrimitive("boolean");
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTOptionalNotSimilarExpression expr) {
    // no compatibility check necessary, therefore only check for optional
    var leftResult = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    var rightResult = getType4Ast().getPartialTypeOfExpr(expr.getRight());
    
    SymTypeExpression result;
    if (leftResult.isObscureType() || rightResult.isObscureType()) {
      result = createObscureType();
    }
    else {
      if (OCLTypeCheck.unwrapOptional(leftResult).isObscureType()) {
        LogHelper.error(expr, "0xA0331", "Couldn't determine type of Optional");
      }
      result = SymTypeExpressionFactory.createPrimitive("boolean");
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  private SymTypeExpression calculateTypeCompareOptional(ASTExpression right, ASTExpression left) {
    var leftResult = getType4Ast().getPartialTypeOfExpr(left);
    var rightResult = getType4Ast().getPartialTypeOfExpr(right);
    
    // check that leftResult is of type Optional
    if (!OCLTypeCheck.unwrapOptional(leftResult).isObscureType()) {
      leftResult = OCLTypeCheck.unwrapOptional(leftResult);
      return calculateTypeCompareOptional(rightResult, leftResult);
    }
    else {
      return SymTypeExpressionFactory.createObscureType();
    }
  }
  
  protected SymTypeExpression calculateTypeCompareOptional(SymTypeExpression rightResult,
      SymTypeExpression leftResult) {
    if (getTypeRel().isNumericType(rightResult) && getTypeRel().isNumericType(leftResult)) {
      return SymTypeExpressionFactory.createPrimitive("boolean");
    }
    else {
      return SymTypeExpressionFactory.createObscureType();
    }
  }
  
  private SymTypeExpression calculateTypeLogicalOptional(ASTExpression right, ASTExpression left) {
    var leftResult = getType4Ast().getPartialTypeOfExpr(left);
    var rightResult = getType4Ast().getPartialTypeOfExpr(right);
    
    // check that leftResult is of type Optional
    if (!OCLTypeCheck.unwrapOptional(leftResult).isObscureType()) {
      leftResult = OCLTypeCheck.unwrapOptional(leftResult);
      return calculateTypeLogicalOptional(rightResult, leftResult);
    }
    else {
      return SymTypeExpressionFactory.createObscureType();
    }
  }
  
  protected SymTypeExpression calculateTypeLogicalOptional(SymTypeExpression rightResult,
      SymTypeExpression leftResult) {
    // Option one: they are both numeric types
    if (getTypeRel().isNumericType(leftResult) && getTypeRel().isNumericType(rightResult)
        || getTypeRel().isBoolean(leftResult) && getTypeRel().isBoolean(rightResult)) {
      return SymTypeExpressionFactory.createPrimitive("boolean");
    }
    // Option two: none of them is a primitive type, and they are either the same type or in a
    // super/ subtype relation
    if (!leftResult.isPrimitive() && !rightResult.isPrimitive()
        && (getTypeRel().isCompatible(leftResult, rightResult) ||
        getTypeRel().isCompatible(rightResult, leftResult))) {
      return SymTypeExpressionFactory.createPrimitive("boolean");
    }
    else {
      // should never happen, no valid result
      return SymTypeExpressionFactory.createObscureType();
    }
  }
}