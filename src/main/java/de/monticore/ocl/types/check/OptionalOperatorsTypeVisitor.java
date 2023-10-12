package de.monticore.ocl.types.check;

import static de.monticore.types.check.SymTypeExpressionFactory.createObscureType;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.optionaloperators._ast.*;
import de.monticore.ocl.optionaloperators._visitor.OptionalOperatorsVisitor2;
import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types3.AbstractTypeVisitor;
import de.monticore.types3.util.TypeVisitorLifting;
import de.se_rwth.commons.logging.Log;

public class OptionalOperatorsTypeVisitor extends AbstractTypeVisitor
    implements OptionalOperatorsVisitor2 {

  public OptionalOperatorsTypeVisitor() {
    OCLSymTypeRelations.init();
  }

  @Override
  public void endVisit(ASTOptionalExpressionPrefix expr) {
    SymTypeExpression left = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getRight());

    SymTypeExpression result =
        TypeVisitorLifting.liftDefault(
                (leftPar, rightPar) ->
                    calculateOptionalExpressionPrefix(
                        expr.getLeft(), expr.getRight(), leftPar, rightPar))
            .apply(left, right);
    getType4Ast().setTypeOfExpression(expr, result);
  }

  protected SymTypeExpression calculateOptionalExpressionPrefix(
      ASTExpression left,
      ASTExpression right,
      SymTypeExpression leftResult,
      SymTypeExpression rightResult) {
    if (!OCLSymTypeRelations.isOptional(leftResult)) {
      Log.error(
          "0xFDB74 expected Optional at '?:' but got " + leftResult.printFullName(),
          left.get_SourcePositionStart(),
          left.get_SourcePositionEnd());
      return createObscureType();
    }
    // check compatibility of type of optional and expression
    else {
      SymTypeExpression elementType = OCLSymTypeRelations.getCollectionElementType(leftResult);
      if (!OCLSymTypeRelations.isCompatible(elementType, rightResult)) {
        Log.error(
            String.format(
                "0xFD201 The types '%s' and '%s' of OptionalExpressionPrefix are not compatible!",
                leftResult.printFullName(), rightResult.printFullName()),
            right.get_SourcePositionStart(),
            right.get_SourcePositionEnd());
        return createObscureType();
      } else {
        return elementType;
      }
    }
  }

  @Override
  public void endVisit(ASTOptionalLessEqualExpression expr) {
    SymTypeExpression left = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getRight());
    SymTypeExpression result =
        TypeVisitorLifting.liftDefault(
                (leftPar, rightPar) ->
                    calculateTypeCompareOptional(
                        expr.getLeft(), expr.getRight(), leftPar, rightPar))
            .apply(left, right);
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTOptionalGreaterEqualExpression expr) {
    SymTypeExpression left = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getRight());
    SymTypeExpression result =
        TypeVisitorLifting.liftDefault(
                (leftPar, rightPar) ->
                    calculateTypeCompareOptional(
                        expr.getLeft(), expr.getRight(), leftPar, rightPar))
            .apply(left, right);
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTOptionalLessThanExpression expr) {
    SymTypeExpression left = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getRight());
    SymTypeExpression result =
        TypeVisitorLifting.liftDefault(
                (leftPar, rightPar) ->
                    calculateTypeCompareOptional(
                        expr.getLeft(), expr.getRight(), leftPar, rightPar))
            .apply(left, right);
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTOptionalGreaterThanExpression expr) {
    SymTypeExpression left = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getRight());
    SymTypeExpression result =
        TypeVisitorLifting.liftDefault(
                (leftPar, rightPar) ->
                    calculateTypeCompareOptional(
                        expr.getLeft(), expr.getRight(), leftPar, rightPar))
            .apply(left, right);
    getType4Ast().setTypeOfExpression(expr, result);
  }

  protected SymTypeExpression calculateTypeCompareOptional(
      ASTExpression left,
      ASTExpression right,
      SymTypeExpression leftResult,
      SymTypeExpression rightResult) {
    if (!OCLSymTypeRelations.isOptional(leftResult)
        || !OCLSymTypeRelations.isNumericType(
            OCLSymTypeRelations.getCollectionElementType(leftResult))) {
      Log.error(
          "0xFD209 expected Optional of a numeric type, but got " + leftResult.printFullName(),
          left.get_SourcePositionStart(),
          left.get_SourcePositionEnd());
      return createObscureType();
    } else if (!OCLSymTypeRelations.isNumericType(rightResult)) {
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
    SymTypeExpression left = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getRight());

    SymTypeExpression result =
        TypeVisitorLifting.liftDefault(
                (leftPar, rightPar) ->
                    calculateTypeLogicalOptional(
                        expr.getLeft(), expr.getRight(), leftPar, rightPar))
            .apply(left, right);
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTOptionalNotEqualsExpression expr) {
    SymTypeExpression left = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getRight());

    SymTypeExpression result =
        TypeVisitorLifting.liftDefault(
                (leftPar, rightPar) ->
                    calculateTypeLogicalOptional(
                        expr.getLeft(), expr.getRight(), leftPar, rightPar))
            .apply(left, right);
    getType4Ast().setTypeOfExpression(expr, result);
  }

  protected SymTypeExpression calculateTypeLogicalOptional(
      ASTExpression left,
      ASTExpression right,
      SymTypeExpression leftResult,
      SymTypeExpression rightResult) {

    SymTypeExpression elementType = OCLSymTypeRelations.getCollectionElementType(leftResult);
    // Option one: they are both numeric types
    if (OCLSymTypeRelations.isNumericType(elementType)
            && OCLSymTypeRelations.isNumericType(rightResult)
        || OCLSymTypeRelations.isBoolean(elementType)
            && OCLSymTypeRelations.isBoolean(rightResult)) {
      return SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
    }
    // Option two: none of them is a primitive type, and they are either the same type or in a
    // super/ subtype relation
    if (!leftResult.isPrimitive()
        && !rightResult.isPrimitive()
        && (OCLSymTypeRelations.isCompatible(elementType, rightResult)
            || OCLSymTypeRelations.isCompatible(rightResult, elementType))) {
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

  @Override
  public void endVisit(ASTOptionalSimilarExpression expr) {
    // no compatibility check necessary, therefore only check for optional
    SymTypeExpression left = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getRight());

    SymTypeExpression result =
        TypeVisitorLifting.liftDefault(
                (leftPar, rightPar) ->
                    calculateOptionalSimilarityExpression(expr, leftPar, rightPar))
            .apply(left, right);
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTOptionalNotSimilarExpression expr) {
    // no compatibility check necessary, therefore only check for optional
    SymTypeExpression left = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getRight());
    SymTypeExpression result =
        TypeVisitorLifting.liftDefault(
                (leftPar, rightPar) ->
                    calculateOptionalSimilarityExpression(expr, leftPar, rightPar))
            .apply(left, right);
    getType4Ast().setTypeOfExpression(expr, result);
  }

  protected SymTypeExpression calculateOptionalSimilarityExpression(
      ASTExpression expr, SymTypeExpression leftType, SymTypeExpression rightType) {
    SymTypeExpression result;

    if (!OCLSymTypeRelations.isOptional(leftType)) {
      Log.error(
          "0xFD203 expected Optional but got " + leftType.printFullName(),
          expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd());
      result = createObscureType();
    } else {
      result = SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
    }
    return result;
  }
}
