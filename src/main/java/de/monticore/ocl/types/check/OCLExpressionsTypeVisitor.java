/*
 *  (c) https://github.com/MontiCore/monticore
 */

package de.monticore.ocl.types.check;

import static de.monticore.types.check.SymTypeExpressionFactory.createObscureType;
import static de.monticore.types.check.SymTypeExpressionFactory.createUnion;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.types.check.SymTypeArray;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types3.AbstractTypeVisitor;
import de.monticore.types3.util.TypeVisitorLifting;
import de.se_rwth.commons.logging.Log;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OCLExpressionsTypeVisitor extends AbstractTypeVisitor
    implements OCLExpressionsVisitor2 {

  // some parts are identical to CommonExpressionsTypeVisitor?...

  public OCLExpressionsTypeVisitor() {
    OCLSymTypeRelations.init();
  }

  @Override
  public void endVisit(ASTTypeCastExpression expr) {
    var left = getType4Ast().getPartialTypeOfTypeId(expr.getMCType());
    var right = getType4Ast().getPartialTypeOfExpr(expr.getExpression());

    SymTypeExpression result =
        TypeVisitorLifting.liftDefault(
                (leftPar, rightPar) -> calculateTypeCastExpression(expr, leftPar, rightPar))
            .apply(left, right);
    getType4Ast().setTypeOfExpression(expr, result);
  }

  protected SymTypeExpression calculateTypeCastExpression(
      ASTTypeCastExpression expr, SymTypeExpression leftResult, SymTypeExpression rightResult) {
    if (OCLSymTypeRelations.isNumericType(leftResult)
        && OCLSymTypeRelations.isNumericType(rightResult)) {
      // allow to cast numbers down, e.g., (int) 5.0 or (byte) 5
      return leftResult;
    } else if (OCLSymTypeRelations.isSubTypeOf(rightResult, leftResult)) {
      // check whether typecast is possible
      return leftResult;
    } else {
      Log.error(
          String.format(
              "0xFD204 The expression of type '%s' " + "can't be cast to the given type '%s'.",
              rightResult.printFullName(), leftResult.printFullName()),
          expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd());
      return createObscureType();
    }
  }

  @Override
  public void endVisit(ASTTypeIfExpression expr) {
    // typeif [var] instanceof [type] then [then] else [else]
    SymTypeExpression varResult = expr.getNameSymbol().getType();
    SymTypeExpression typeResult = getType4Ast().getPartialTypeOfTypeId(expr.getMCType());
    SymTypeExpression thenResult =
        getType4Ast().getPartialTypeOfExpr(expr.getThenExpression().getExpression());
    SymTypeExpression elseResult = getType4Ast().getPartialTypeOfExpr(expr.getElseExpression());

    SymTypeExpression result;
    if (typeResult.isObscureType()
        || varResult.isObscureType()
        || thenResult.isObscureType()
        || elseResult.isObscureType()) {
      // if any inner obscure then error already logged
      result = createObscureType();
    } else if (!OCLSymTypeRelations.isSubTypeOf(typeResult, varResult)) {
      if (OCLSymTypeRelations.isSubTypeOf(varResult, typeResult)) {
        Log.error(
            "0xFD290 checking whether '"
                + expr.getName()
                + "' of type "
                + varResult.printFullName()
                + " is of type "
                + typeResult.printFullName()
                + ", which is redundant"
                + ", since it is always true",
            expr.get_SourcePositionStart(),
            expr.get_SourcePositionEnd());
      } else {
        Log.error(
            "0xFD289 checking whether '"
                + expr.getName()
                + "' of type "
                + varResult.printFullName()
                + " is of type "
                + typeResult.printFullName()
                + ", which is impossible",
            expr.get_SourcePositionStart(),
            expr.get_SourcePositionEnd());
      }
      result = createObscureType();
    } else {
      result = calculateConditionalExpression(thenResult, elseResult);
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTIfThenElseExpression expr) {
    SymTypeExpression conditionResult = getType4Ast().getPartialTypeOfExpr(expr.getCondition());
    SymTypeExpression thenResult = getType4Ast().getPartialTypeOfExpr(expr.getThenExpression());
    SymTypeExpression elseResult = getType4Ast().getPartialTypeOfExpr(expr.getElseExpression());

    SymTypeExpression result;
    if (conditionResult.isObscureType()
        || thenResult.isObscureType()
        || elseResult.isObscureType()) {
      // if any inner obscure then error already logged
      result = createObscureType();
    } else if (!OCLSymTypeRelations.isBoolean(conditionResult)) {
      Log.error(
          "0xFD286 condition must be a Boolean expression, "
              + "but is of type "
              + conditionResult.printFullName(),
          expr.getCondition().get_SourcePositionStart(),
          expr.getCondition().get_SourcePositionEnd());
      result = createObscureType();
    } else {
      result = calculateConditionalExpression(thenResult, elseResult);
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }

  protected SymTypeExpression calculateConditionalExpression(
      SymTypeExpression thenType, SymTypeExpression elseType) {
    SymTypeExpression result;
    if (thenType.isObscureType() || elseType.isObscureType()) {
      result = createObscureType();
    } else {
      result = createUnion(thenType, elseType);
    }
    return result;
  }

  @Override
  public void endVisit(ASTImpliesExpression expr) {
    var left = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    var right = getType4Ast().getPartialTypeOfExpr(expr.getRight());

    SymTypeExpression result =
        TypeVisitorLifting.liftDefault(
                (leftPar, rightPar) ->
                    calculateConditionalBooleanOperation(
                        expr.getLeft(), expr.getRight(), leftPar, rightPar, "implies"))
            .apply(left, right);
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTEquivalentExpression expr) {
    var left = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    var right = getType4Ast().getPartialTypeOfExpr(expr.getRight());

    SymTypeExpression result =
        TypeVisitorLifting.liftDefault(
                (leftPar, rightPar) ->
                    calculateConditionalBooleanOperation(
                        expr.getLeft(), expr.getRight(), leftPar, rightPar, "equivalent"))
            .apply(left, right);
    getType4Ast().setTypeOfExpression(expr, result);
  }

  protected SymTypeExpression calculateConditionalBooleanOperation(
      ASTExpression left,
      ASTExpression right,
      SymTypeExpression leftResult,
      SymTypeExpression rightResult,
      String operator) {

    if (OCLSymTypeRelations.isBoolean(leftResult) && OCLSymTypeRelations.isBoolean(rightResult)) {
      return SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
    } else {
      // operator not applicable
      Log.error(
          String.format(
              "0xFD207 The operator '%s' is not applicable to "
                  + "the expressions of type '%s' and '%s' "
                  + "but only to expressions of type boolean.",
              operator, leftResult.printFullName(), rightResult.printFullName()),
          left.get_SourcePositionStart(),
          right.get_SourcePositionEnd());
      return createObscureType();
    }
  }

  @Override
  public void endVisit(ASTForallExpression expr) {
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getExpression());

    SymTypeExpression result;
    if (right.isObscureType()) {
      result = createObscureType();
    } else if (OCLSymTypeRelations.isBoolean(right)) {
      result = SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
    } else {
      Log.error(
          String.format(
              "0xFD208 The type of the expression "
                  + "in the ForallExpression is '%s' but has to be boolean.",
              right.printFullName()),
          expr.getExpression().get_SourcePositionStart(),
          expr.getExpression().get_SourcePositionEnd());
      result = createObscureType();
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTExistsExpression expr) {
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getExpression());

    SymTypeExpression result;
    if (right.isObscureType()) {
      result = createObscureType();
    } else if (OCLSymTypeRelations.isBoolean(right)) {
      result = SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
    } else {
      result = createObscureType();

      Log.error(
          String.format(
              "The type of the expression in the ExistsExpression is '%s' but has to be boolean.",
              right.printFullName()),
          expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd());
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTAnyExpression expr) {
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getExpression());

    SymTypeExpression result;
    if (right.isObscureType()) {
      result = createObscureType();
    } else if (!OCLSymTypeRelations.isOCLCollection(right)) {
      Log.error(
          "0xFD292 expected a collection for 'any', but got " + right.getTypeInfo(),
          expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd());
      result = createObscureType();
    } else {
      result = OCLSymTypeRelations.getCollectionElementType(right);
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTLetinExpression expr) {
    boolean obscureDeclaration =
        expr.streamOCLVariableDeclarations()
            .anyMatch(
                decl -> getType4Ast().getPartialTypeOfExpr(decl.getExpression()).isObscureType());

    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getExpression());

    SymTypeExpression result;
    if (right.isObscureType() || obscureDeclaration) {
      result = createObscureType();
    } else {
      result = right;
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTIterateExpression expr) {
    SymTypeExpression result;
    // cannot iterate without initialization
    if (!expr.getInit().isPresentExpression()) {
      Log.error(
          "0xFD75C to 'iterate'"
              + "an initialization of the variable '"
              + expr.getInit().getName()
              + "'is required",
          expr.getInit().get_SourcePositionStart(),
          expr.getInit().get_SourcePositionEnd());
      result = createObscureType();
    } else {
      SymTypeExpression initResult =
          getType4Ast().getPartialTypeOfExpr(expr.getInit().getExpression());
      SymTypeExpression inDelcResult = evaluateInDeclarationType(expr.getIteration());
      SymTypeExpression valueResult = getType4Ast().getPartialTypeOfExpr(expr.getValue());

      if (initResult.isObscureType()
          || inDelcResult.isObscureType()
          || valueResult.isObscureType()) {
        result = createObscureType();
      } else if (!OCLSymTypeRelations.isCompatible(initResult, valueResult)) {
        Log.error(
            "0xFDC53 unable to assign the result of the expression ("
                + valueResult.printFullName()
                + ") to the variable '"
                + expr.getName()
                + "' of type "
                + initResult.printFullName(),
            expr.get_SourcePositionStart(),
            expr.get_SourcePositionEnd());
        result = createObscureType();
      } else {
        result = initResult.deepClone();
      }
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }

  /**
   * checks the InDeclaration for typing issues, logs on error
   *
   * @return the type of the variable, Obscure on error
   */
  protected SymTypeExpression evaluateInDeclarationType(ASTInDeclaration inDeclaration) {
    Optional<SymTypeExpression> left =
        inDeclaration.isPresentMCType()
            ? Optional.of(getType4Ast().getPartialTypeOfTypeId(inDeclaration.getMCType()))
            : Optional.empty();
    Optional<SymTypeExpression> expressionResult =
        inDeclaration.isPresentExpression()
            ? Optional.of(getType4Ast().getPartialTypeOfExpr(inDeclaration.getExpression()))
            : Optional.empty();
    SymTypeExpression result;

    if (expressionResult.isPresent()
        && !OCLSymTypeRelations.isOCLCollection(expressionResult.get())) {
      Log.error(
          "0xFD297 expected collection after 'in', but got "
              + expressionResult.get().printFullName(),
          inDeclaration.getExpression().get_SourcePositionStart(),
          inDeclaration.getExpression().get_SourcePositionEnd());
      result = createObscureType();
    } else if (expressionResult.isPresent()
        && left.isPresent()
        && !OCLSymTypeRelations.isCompatible(
            left.get(), OCLSymTypeRelations.getCollectionElementType(expressionResult.get()))) {
      Log.error(
          "0xFD298 cannot assign element of "
              + expressionResult.get().printFullName()
              + " to variable of type "
              + left.get().printFullName(),
          inDeclaration.get_SourcePositionStart(),
          inDeclaration.get_SourcePositionEnd());
      result = createObscureType();
    } else if (expressionResult.isEmpty()
        && (OCLSymTypeRelations.isNumericType(left.get())
            || OCLSymTypeRelations.isBoolean(left.get()))) {
      // this is technically not enough,
      // the correct check is whether the type is a domain class ->
      // if it is, one can get all instantiations
      Log.error(
          "0xFD7E4 iterate based on primitives is undefined, "
              + "try providing a collection to iterate over",
          inDeclaration.getMCType().get_SourcePositionStart(),
          inDeclaration.getMCType().get_SourcePositionEnd());
      result = createObscureType();
    } else {
      result =
          left.orElseGet(
              () -> OCLSymTypeRelations.getCollectionElementType(expressionResult.get()));
    }
    return result;
  }

  @Override
  public void endVisit(ASTInstanceOfExpression expr) {
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getExpression());
    SymTypeExpression left = getType4Ast().getPartialTypeOfTypeId(expr.getMCType());

    SymTypeExpression result;
    if (right.isObscureType() || left.isObscureType()) {
      result = createObscureType();
    } else {
      result = SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTOCLArrayQualification expr) {
    SymTypeExpression result;
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getExpression());
    List<SymTypeExpression> argumentTypes =
        expr.getArgumentsList().stream()
            .map(getType4Ast()::getPartialTypeOfExpr)
            .collect(Collectors.toList());
    if (right.isObscureType()
        || argumentTypes.stream().anyMatch(SymTypeExpression::isObscureType)) {
      result = createObscureType();
    } else {
      result = calculateArrayQualificationRec(expr, right, argumentTypes);
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }

  protected SymTypeExpression calculateArrayQualificationRec(
      ASTOCLArrayQualification expr,
      SymTypeExpression toBeAccessed,
      List<SymTypeExpression> arguments) {
    // stop recursion
    if (arguments.isEmpty()) {
      return toBeAccessed;
    }
    SymTypeExpression result;
    SymTypeExpression currentArg = arguments.get(0);
    if (!toBeAccessed.isArrayType()
        && !OCLSymTypeRelations.isOCLCollection(toBeAccessed)
        && !OCLSymTypeRelations.isOptional(toBeAccessed)
        && !OCLSymTypeRelations.isMap(toBeAccessed)) {
      Log.error(
          "0xFD3D6 trying a qualified access on "
              + toBeAccessed.printFullName()
              + " which is not a type "
              + "applicable to qualified accesses",
          expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd());
      result = createObscureType();
    }
    // case qualified access based on order: List
    else if (OCLSymTypeRelations.isIntegralType(currentArg)
        && OCLSymTypeRelations.isList(toBeAccessed)) {
      result =
          calculateArrayQualificationRec(
              expr,
              OCLSymTypeRelations.getCollectionElementType(toBeAccessed),
              arguments.subList(1, arguments.size()));
    }
    // case qualified access based on order: Array
    else if (OCLSymTypeRelations.isIntegralType(currentArg) && toBeAccessed.isArrayType()) {
      result =
          calculateArrayQualificationRec(
              expr,
              ((SymTypeArray) toBeAccessed).cloneWithLessDim(1),
              arguments.subList(1, arguments.size()));
    }
    // case qualified access on OCLCollection
    // container.role[qualifier] == {elem.role[qualifier] | elem in container}
    else if (OCLSymTypeRelations.isOCLCollection(toBeAccessed)
        || OCLSymTypeRelations.isOptional(toBeAccessed)) {
      SymTypeExpression preResultInnerType =
          calculateArrayQualificationRec(
              expr,
              OCLSymTypeRelations.getCollectionElementType(toBeAccessed),
              arguments.subList(0, 1));
      // wrap in same kind of collection
      SymTypeOfGenerics wrappedPreResult = (SymTypeOfGenerics) toBeAccessed.deepClone();
      wrappedPreResult.setArgument(0, preResultInnerType);
      // now we actually reduce the argument count
      result =
          calculateArrayQualificationRec(
              expr, wrappedPreResult, arguments.subList(1, arguments.size()));
    }
    // case map access
    else if (OCLSymTypeRelations.isMap(toBeAccessed)) {
      if (OCLSymTypeRelations.isCompatible(
          OCLSymTypeRelations.getMapKeyType(toBeAccessed), currentArg)) {
        result =
            calculateArrayQualificationRec(
                expr,
                OCLSymTypeRelations.getCollectionElementType(toBeAccessed),
                arguments.subList(1, arguments.size()));
      } else {
        Log.error(
            "0xFDC85 trying to access a map of type "
                + toBeAccessed.printFullName()
                + " with a key of type "
                + currentArg.printFullName()
                + ", which is not applicable",
            expr.get_SourcePositionStart(),
            expr.get_SourcePositionEnd());
        result = createObscureType();
      }
    } else {
      Log.error(
          "0xFDC86 trying to access expression of type "
              + toBeAccessed.printFullName()
              + " (collections may have been unwrapped) "
              + "with qualifier of type "
              + currentArg.printFullName()
              + " which is not applicable",
          expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd());
      result = createObscureType();
    }
    return result;
  }

  protected SymTypeExpression evaluateArrayType(
      ASTOCLArrayQualification expr, SymTypeArray arrayResult) {
    SymTypeExpression result;
    if (arrayResult.getDim() == 1) {
      // case: A[] bar -> bar[3] returns the type A
      result = arrayResult.getArgument();
    } else if (arrayResult.getDim() > 1) {
      // case: A[][] bar -> bar[3] returns the type A[] -> decrease the dimension of the array by 1
      SymTypeExpression intType = SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.INT);
      result = SymTypeExpressionFactory.createTypeArray(intType, arrayResult.getDim() - 1);
    } else {
      // case: dim < 1, should never occur
      result = createObscureType();

      Log.error(
          "0xFD218 Array qualifications must have an array dimension of at least 1.",
          expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd());
    }
    return result;
  }

  protected SymTypeExpression evaluateGenericType(
      ASTOCLArrayQualification expr, SymTypeOfGenerics right) {
    SymTypeExpression result;
    if (right.getArgumentList().size() > 1
        && !right.getTypeConstructorFullName().equals("java.util.Map")) {
      result = createObscureType();

      Log.error(
          "0xFD211 Array qualifications can only be used with one type argument or a map.",
          expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd());
    } else if (right.getTypeConstructorFullName().equals("java.util.Map")) {
      result = right.getArgument(1);
    } else {
      result = right.getArgument(0);
    }
    return result;
  }

  @Override
  public void endVisit(ASTOCLAtPreQualification expr) {
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getExpression());

    SymTypeExpression result;
    if (right.isObscureType()) {
      result = createObscureType();
    } else {
      result = right;
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTOCLTransitiveQualification expr) {
    SymTypeExpression right = getType4Ast().getPartialTypeOfExpr(expr.getExpression());

    SymTypeExpression result;
    if (right.isObscureType()) {
      result = createObscureType();
    } else {
      result = right;
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
}
