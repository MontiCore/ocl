package de.monticore.ocl.types.check;

import static de.monticore.types.check.SymTypeExpressionFactory.createObscureType;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.optionaloperators._ast.ASTOptionalEqualsExpression;
import de.monticore.ocl.optionaloperators._ast.ASTOptionalExpressionPrefix;
import de.monticore.ocl.optionaloperators._ast.ASTOptionalGreaterEqualExpression;
import de.monticore.ocl.optionaloperators._ast.ASTOptionalGreaterThanExpression;
import de.monticore.ocl.optionaloperators._ast.ASTOptionalLessEqualExpression;
import de.monticore.ocl.optionaloperators._ast.ASTOptionalLessThanExpression;
import de.monticore.ocl.optionaloperators._ast.ASTOptionalNotEqualsExpression;
import de.monticore.ocl.optionaloperators._ast.ASTOptionalNotSimilarExpression;
import de.monticore.ocl.optionaloperators._ast.ASTOptionalSimilarExpression;
import de.monticore.ocl.optionaloperators._visitor.OptionalOperatorsVisitor2;
import de.monticore.ocl.types3.IOCLSymTypeRelations;
import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types3.AbstractTypeVisitor;
import de.se_rwth.commons.logging.Log;

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
    } else if (!getTypeRel().isOptional(optionalResult)) {
      Log.error(
          "0xFDB74 expected Optional at '?:' but got " + optionalResult.printFullName(),
          expr.getLeft().get_SourcePositionStart(),
          expr.getLeft().get_SourcePositionEnd());
      result = createObscureType();
    }
    // check compatibility of type of optional and expression
    else {
      SymTypeExpression elementType = getTypeRel().getCollectionElementType(optionalResult);
      if (!getTypeRel().isCompatible(elementType, exprResult)) {
        Log.error(
            String.format(
                "0xFD201 The types '%s' and '%s' of OptionalExpressionPrefix are not compatible!",
                optionalResult.printFullName(), exprResult.printFullName()),
            expr.get_SourcePositionStart(),
            expr.get_SourcePositionEnd());
        result = createObscureType();
      } else {
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

  protected SymTypeExpression calculateTypeCompareOptional(
      ASTExpression right, ASTExpression left) {
    var leftResult = getType4Ast().getPartialTypeOfExpr(left);
    var rightResult = getType4Ast().getPartialTypeOfExpr(right);

    if (leftResult.isObscureType() || rightResult.isObscureType()) {
      return createObscureType();
    }
    // check that leftResult is of type Optional
    else if (!getTypeRel().isOptional(leftResult)
        || !getTypeRel().isNumericType(getTypeRel().getCollectionElementType(leftResult))) {
      Log.error(
          "0xFD209 expected Optional of a numeric type, but got " + leftResult.printFullName(),
          left.get_SourcePositionStart(),
          left.get_SourcePositionEnd());
      return createObscureType();
    } else if (!getTypeRel().isNumericType(rightResult)) {
      Log.error(
          "0xFD280 expected numeric type but got " + rightResult.printFullName(),
          right.get_SourcePositionStart(),
          right.get_SourcePositionEnd());
      return createObscureType();
    } else {
      return SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
    }
  }

  @Override
  public void endVisit(ASTOptionalEqualsExpression expr) {
    var result = calculateTypeLogicalOptional(expr.getLeft(), expr.getRight());
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTOptionalNotEqualsExpression expr) {
    var result = calculateTypeLogicalOptional(expr.getLeft(), expr.getRight());
    getType4Ast().setTypeOfExpression(expr, result);
  }

  protected SymTypeExpression calculateTypeLogicalOptional(
      ASTExpression left, ASTExpression right) {
    var leftResult = getType4Ast().getPartialTypeOfExpr(left);
    var rightResult = getType4Ast().getPartialTypeOfExpr(right);

    if (leftResult.isObscureType() || rightResult.isObscureType()) {
      return createObscureType();
    } else if (!getTypeRel().isOptional(leftResult)) {
      Log.error(
          "0xFD283 expected Optional, but got " + leftResult.printFullName(),
          left.get_SourcePositionStart(),
          right.get_SourcePositionEnd());
      return createObscureType();
    } else {
      SymTypeExpression elementType = getTypeRel().getCollectionElementType(leftResult);
      // Option one: they are both numeric types
      if (getTypeRel().isNumericType(elementType) && getTypeRel().isNumericType(rightResult)
          || getTypeRel().isBoolean(elementType) && getTypeRel().isBoolean(rightResult)) {
        return SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
      }
      // Option two: none of them is a primitive type, and they are either the same type or in a
      // super/ subtype relation
      if (!leftResult.isPrimitive()
          && !rightResult.isPrimitive()
          && (getTypeRel().isCompatible(elementType, rightResult)
              || getTypeRel().isCompatible(rightResult, elementType))) {
        return SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
      } else {
        // should never happen, no valid result
        Log.error(
            "0xFD285 types "
                + elementType.printFullName()
                + " and "
                + rightResult.printFullName()
                + " are not comparable",
            left.get_SourcePositionStart(),
            right.get_SourcePositionEnd());
        return SymTypeExpressionFactory.createObscureType();
      }
    }
  }

  @Override
  public void endVisit(ASTOptionalSimilarExpression expr) {
    // no compatibility check necessary, therefore only check for optional
    var leftResult = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    var rightResult = getType4Ast().getPartialTypeOfExpr(expr.getRight());
    calculateOptionalSimilarityExpression(expr, leftResult, rightResult);
  }

  @Override
  public void endVisit(ASTOptionalNotSimilarExpression expr) {
    // no compatibility check necessary, therefore only check for optional
    var leftResult = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    var rightResult = getType4Ast().getPartialTypeOfExpr(expr.getRight());
    calculateOptionalSimilarityExpression(expr, leftResult, rightResult);
  }

  protected void calculateOptionalSimilarityExpression(
      ASTExpression expr, SymTypeExpression leftType, SymTypeExpression rightType) {
    SymTypeExpression result;
    if (leftType.isObscureType() || rightType.isObscureType()) {
      result = createObscureType();
    } else {
      if (!getTypeRel().isOptional(leftType)) {
        Log.error(
            "0xFD203 expected Optional but got " + leftType.printFullName(),
            expr.get_SourcePositionStart(),
            expr.get_SourcePositionEnd());
        result = createObscureType();
      } else {
        result = SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
      }
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
}
