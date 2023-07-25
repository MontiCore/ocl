package de.monticore.ocl.types.check;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.optionaloperators._ast.*;
import de.monticore.ocl.optionaloperators._visitor.OptionalOperatorsVisitor2;
import de.monticore.ocl.types3.IOCLSymTypeRelations;
import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types3.AbstractTypeVisitor;
import de.se_rwth.commons.logging.Log;

import static de.monticore.types.check.SymTypeExpressionFactory.createObscureType;

public class OptionalOperatorsTypeVisitor extends AbstractTypeVisitor
    implements OptionalOperatorsVisitor2 {

  protected IOCLSymTypeRelations typeRelations;

  public OptionalOperatorsTypeVisitor() {
    this(new OCLSymTypeRelations());
  }

  protected OptionalOperatorsTypeVisitor(IOCLSymTypeRelations typeRelations) {
    this.typeRelations = typeRelations;
  }

  public void setSymTypeRelations(IOCLSymTypeRelations typeRelations) {
    this.typeRelations = typeRelations;
  }

  protected IOCLSymTypeRelations getTypeRel() {
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
    else if (!getTypeRel().isOptional(optionalResult)) {
      Log.error("0xFDB74 expected Optional at '?:' but got "
              + optionalResult.printFullName(),
          expr.getLeft().get_SourcePositionStart(),
          expr.getLeft().get_SourcePositionEnd()
      );
      result = createObscureType();
    }
    // check compatibility of type of optional and expression
    else {
      SymTypeExpression elementType =
          getTypeRel().getCollectionElementType(optionalResult);
      if (!getTypeRel().isCompatible(elementType, exprResult)) {
        Log.error(String.format(
                "0xFD201 The types '%s' and '%s' of OptionalExpressionPrefix are not compatible!",
                optionalResult.printFullName(), exprResult.printFullName()),
            expr.get_SourcePositionStart(),
            expr.get_SourcePositionEnd());
        result = createObscureType();
      }
      else {
        result = elementType;
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
      if (getTypeRel().isOptional(leftResult)) {
        Log.error("0xFD202 Couldn't determine type of Optional.", expr.get_SourcePositionStart(),
            expr.get_SourcePositionEnd());
      }
      result = SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
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
      if (!getTypeRel().isOptional(leftResult)) {
        Log.error("0xFD203 Couldn't determine type of Optional.", expr.get_SourcePositionStart(),
            expr.get_SourcePositionEnd());
      }
      result = SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }

  private SymTypeExpression calculateTypeCompareOptional(ASTExpression right, ASTExpression left) {
    var leftResult = getType4Ast().getPartialTypeOfExpr(left);
    var rightResult = getType4Ast().getPartialTypeOfExpr(right);

    // check that leftResult is of type Optional
    if (!getTypeRel().isOptional(leftResult)) {
      SymTypeExpression elementType =
          getTypeRel().getCollectionElementType(leftResult);
      return calculateTypeCompareOptional(rightResult, elementType);
    }
    else {
      return SymTypeExpressionFactory.createObscureType();
    }
  }

  protected SymTypeExpression calculateTypeCompareOptional(SymTypeExpression rightResult,
      SymTypeExpression leftResult) {
    if (getTypeRel().isNumericType(rightResult) && getTypeRel().isNumericType(leftResult)) {
      return SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
    }
    else {
      return SymTypeExpressionFactory.createObscureType();
    }
  }

  private SymTypeExpression calculateTypeLogicalOptional(ASTExpression right, ASTExpression left) {
    var leftResult = getType4Ast().getPartialTypeOfExpr(left);
    var rightResult = getType4Ast().getPartialTypeOfExpr(right);

    // check that leftResult is of type Optional
    if (!getTypeRel().isOptional(leftResult)) {
      leftResult = getTypeRel().getCollectionElementType(leftResult);
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
      return SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
    }
    // Option two: none of them is a primitive type, and they are either the same type or in a
    // super/ subtype relation
    if (!leftResult.isPrimitive() && !rightResult.isPrimitive()
        && (getTypeRel().isCompatible(leftResult, rightResult) ||
        getTypeRel().isCompatible(rightResult, leftResult))) {
      return SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
    }
    else {
      // should never happen, no valid result
      return SymTypeExpressionFactory.createObscureType();
    }
  }

  // TODO MSm move setexpressions.types3.SetExpressionsTypeVisitor & OCLTrav in ocl.types3
}