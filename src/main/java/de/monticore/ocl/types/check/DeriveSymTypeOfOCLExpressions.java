// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types.check;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._symboltable.IExpressionsBasisScope;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.ocl.util.LogHelper;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.types.check.*;
import de.se_rwth.commons.logging.Log;
import java.util.List;
import java.util.Map;

/**
 * @deprecated This class is no longer acceptable since we use <b>Type Check 3</b> to calculate the
 *     type of expressions and literals related to OCL. Use {@link OCLExpressionsTypeVisitor}
 *     instead.
 */
@Deprecated(forRemoval = true)
public class DeriveSymTypeOfOCLExpressions extends AbstractDeriveFromExpression
    implements OCLExpressionsHandler {

  protected OCLExpressionsTraverser traverser;

  @Override
  public void traverse(ASTTypeCastExpression node) {
    SymTypeExpression exprResult = null;
    SymTypeExpression typeResult = null;

    // check type of Expression
    if (node.getExpression() != null) {
      node.getExpression().accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      exprResult = typeCheckResult.getResult();
      typeCheckResult.reset();
    } else {
      LogHelper.error(
          node,
          "0xA3080",
          "The type of the expression of the OCLTypeCastExpression could not be calculated");
      return;
    }

    // check type of type to cast expression to
    if (node.getMCType() != null) {
      node.getMCType().accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      typeResult = typeCheckResult.getResult();
      typeCheckResult.reset();
    } else {
      LogHelper.error(
          node,
          "0xA3081",
          "The type of the MCType of the OCLTypeCastExpression could not be calculated");
      return;
    }

    // check whether typecast is possible
    if (!OCLTypeCheck.compatible(typeResult, exprResult)) {
      typeCheckResult.reset();
      LogHelper.error(
          node,
          "0xA3082",
          "The type of the expression of the OCLTypeCastExpression can't be cast to given type");
      return;
    } else {
      // set result to typecasted expression
      typeCheckResult.setResult(typeResult.deepClone());
    }
  }

  @Override
  public void traverse(ASTTypeIfExpression node) {
    SymTypeExpression thenResult = null;
    SymTypeExpression elseResult = null;

    // resolve MCType to SymTypeExpression
    if (node.getMCType() != null) {
      node.getMCType().accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      typeCheckResult.reset();
    } else {
      LogHelper.error(
          node,
          "0xA3012",
          "The type of the MCType of the OCLInstanceOfExpression of the OCLTypeIfExpr could not be calculated");
      return;
    }

    if (node.getThenExpression() != null) {
      node.getThenExpression().accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      thenResult = typeCheckResult.getResult();
      typeCheckResult.reset();
    } else {
      LogHelper.error(
          node,
          "0xA3013",
          "The type of the then expression of the OCLTypeIfExpression could not be calculated");
      return;
    }

    if (node.getElseExpression() != null) {
      node.getElseExpression().accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      elseResult = typeCheckResult.getResult();
      typeCheckResult.reset();
    } else {
      LogHelper.error(
          node,
          "0xA3014",
          "The type of the else expression of the OCLTypeIfExpr could not be calculated");
      return;
    }

    if (OCLTypeCheck.compatible(thenResult, elseResult)) {
      typeCheckResult.setResult(thenResult);
    } else if (OCLTypeCheck.isSubtypeOf(thenResult, elseResult)) {
      typeCheckResult.setResult(elseResult);
    } else {
      typeCheckResult.reset();
      LogHelper.error(
          node,
          "0xA3015",
          "The type of the else expression of the OCLTypeIfExpr doesn't match the then expression");
      return;
    }
  }

  @Override
  public void traverse(ASTIfThenElseExpression node) {
    SymTypeExpression conditionResult = null;
    SymTypeExpression thenResult = null;
    SymTypeExpression elseResult = null;

    if (node.getCondition() != null) {
      node.getCondition().accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      conditionResult = typeCheckResult.getResult();
      typeCheckResult.reset();
    } else {
      LogHelper.error(
          node,
          "0xA3040",
          "The type of the left expression of the OCLIfThenElseExpr could not be calculated");
      return;
    }

    // the condition has to be boolean
    if (!TypeCheck.isBoolean(conditionResult)) {
      typeCheckResult.reset();
      LogHelper.error(
          node, "0xA3041", "The type of the condition of the OCLIfThenElseExpr has to be boolean");
      return;
    }

    if (node.getThenExpression() != null) {
      node.getThenExpression().accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      thenResult = typeCheckResult.getResult();
      typeCheckResult.reset();
    } else {
      LogHelper.error(
          node,
          "0xA3042",
          "The type of the then expression of the OCLIfThenElseExpr could not be calculated");
    }

    if (node.getElseExpression() != null) {
      node.getElseExpression().accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      elseResult = typeCheckResult.getResult();
      typeCheckResult.reset();
    } else {
      LogHelper.error(
          node,
          "0xA3043",
          "The type of the else expression of the OCLIfThenElseExpr could not be calculated");
      return;
    }

    if (OCLTypeCheck.compatible(thenResult, elseResult)) {
      // Type of else is subtype of/or same type as then -> return then-type
      typeCheckResult.setResult(thenResult);
    } else if (OCLTypeCheck.isSubtypeOf(thenResult, elseResult)) {
      // Type of then is subtype of else -> return else-type
      typeCheckResult.setResult(elseResult);
    } else {
      typeCheckResult.reset();
      LogHelper.error(
          node,
          "0xA3044",
          "The type of the else expression of the OCLIfThenElseExpr doesn't match the then expression");
      return;
    }
  }

  @Override
  public void traverse(ASTImpliesExpression node) {
    // sets the last result
    checkAndSetBooleanTypes(node.getLeft(), node.getRight(), "ImpliesExpression");
  }

  @Override
  public void traverse(ASTEquivalentExpression node) {
    // sets the last result
    checkAndSetBooleanTypes(node.getLeft(), node.getRight(), "EquivalentExpression");
  }

  @Override
  public void traverse(ASTForallExpression node) {
    SymTypeExpression exprResult = null;

    if (node.getExpression() != null) {
      node.getExpression().accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      exprResult = typeCheckResult.getResult();
      typeCheckResult.reset();
    } else {
      LogHelper.error(
          node,
          "0xA3211",
          "The type of the expression in the ForallExpression could not be calculated");
    }
    if (!TypeCheck.isBoolean(exprResult)) {
      typeCheckResult.reset();
      LogHelper.error(
          node, "0xA3212", "The type of the expression in the ForallExpression has to be boolean");
      return;
    } else {
      typeCheckResult.setResult(createBoolean());
    }
  }

  @Override
  public void traverse(ASTExistsExpression node) {
    SymTypeExpression exprResult = null;

    if (node.getExpression() != null) {
      node.getExpression().accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      exprResult = typeCheckResult.getResult();
      typeCheckResult.reset();
    } else {
      LogHelper.error(
          node,
          "0xA3211",
          "The type of the expression in the ExistsExpression could not be calculated");
    }
    if (!TypeCheck.isBoolean(exprResult)) {
      typeCheckResult.reset();
      LogHelper.error(
          node, "0xA3212", "The type of the expression in the ExistsExpression has to be boolean");
      return;
    } else {
      typeCheckResult.setResult(createBoolean());
    }
  }

  @Override
  public void traverse(ASTAnyExpression node) {
    SymTypeExpression exprResult = null;

    if (node.getExpression() != null) {
      node.getExpression().accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      exprResult = typeCheckResult.getResult();
      typeCheckResult.reset();
    } else {
      LogHelper.error(
          node, "0xA3050", "The type of the expression of the OCLAnyExpr could not be calculated");
      return;
    }

    if (exprResult instanceof SymTypeOfGenerics) {
      typeCheckResult.setResult(OCLTypeCheck.unwrapSet(exprResult));
    } else {
      typeCheckResult.setResult(exprResult);
    }
  }

  @Override
  public void traverse(ASTLetinExpression node) {
    SymTypeExpression exprResult = null;

    if (node.getOCLVariableDeclarationList() != null
        && !node.getOCLVariableDeclarationList().isEmpty()) {
      for (ASTOCLVariableDeclaration dec : node.getOCLVariableDeclarationList()) {
        dec.accept(getTraverser());
        if (!typeCheckResult.isPresentResult()) {
          LogHelper.error(
              node,
              "0xA3060",
              "The type of the OCLVariableDeclaration of the LetinExpr could not be calculated");
          return;
        }
      }
    }

    if (node.getExpression() != null) {
      node.getExpression().accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      exprResult = typeCheckResult.getResult();
      typeCheckResult.reset();
    } else {
      LogHelper.error(
          node, "0xA3061", "The type of the expression of the LetinExpr could not be calculated");
      return;
    }

    typeCheckResult.setResult(exprResult);
  }

  @Override
  public void traverse(ASTIterateExpression node) {
    SymTypeExpression valueResult = null;
    SymTypeExpression initResult = null;

    if (node.getInit() != null) {
      node.getInit().accept(getTraverser());
    }
    if (!typeCheckResult.isPresentResult()) {
      LogHelper.error(
          node,
          "0xA3071",
          "The type of the init of the OCLIterateExpression could not be calculated");
      return;
    } else {
      initResult = typeCheckResult.getResult();
      typeCheckResult.reset();
    }

    if (node.getValue() != null) {
      node.getValue().accept(getTraverser());
    }
    if (!typeCheckResult.isPresentResult()) {
      LogHelper.error(
          node,
          "0xA3073",
          "The type of the value of the OCLIterateExpression could not be calculated");
      return;
    } else {
      valueResult = typeCheckResult.getResult();
      typeCheckResult.reset();
    }

    if (!OCLTypeCheck.compatible(initResult, valueResult)) {
      typeCheckResult.reset();
      Log.error(
          "0xA3074 The type of the value of the OCLIterateExpression ("
              + valueResult.print()
              + ") has to match the type of the init declaration ("
              + initResult.print()
              + ")");
      return;
    } else {
      typeCheckResult.setResult(initResult);
    }
  }

  @Override
  public void traverse(ASTInstanceOfExpression node) {
    if (node.getExpression() != null) {
      node.getExpression().accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      typeCheckResult.reset();
    } else {
      LogHelper.error(
          node,
          "0xA3000",
          "The type of the left expression of the OCLInstanceOfExpression could not be calculated");
      return;
    }

    if (node.getMCType() != null) {
      node.getMCType().accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      typeCheckResult.reset();
    } else {
      LogHelper.error(
          node,
          "0xA3001",
          "The type of the MCType of the OCLInstanceOfExpression could not be calculated");
      return;
    }

    final SymTypeExpression wholeResult = createBoolean();
    typeCheckResult.setResult(wholeResult);
  }

  @Override
  public void traverse(ASTOCLArrayQualification node) {
    SymTypeExpression exprResult;
    if (node.getExpression() != null) {
      node.getExpression().accept(getTraverser());
    }
    if (!typeCheckResult.isPresentResult()) {
      LogHelper.error(
          node,
          "0xA3001",
          "The type of the expression of the OCLArrayQualification could not be calculated");
      return;
    }
    exprResult = typeCheckResult.getResult();
    typeCheckResult.reset();
    for (ASTExpression e : node.getArgumentsList()) {
      if (e != null) {
        e.accept(getTraverser());
      }
      if (!typeCheckResult.isPresentResult()) {
        LogHelper.error(
            node,
            "0xA3001",
            "The type of a expression in the arguments of the OCLArrayQualification could not be calculated");
        typeCheckResult.reset();
        return;
      }
      if (!isIntegralType(typeCheckResult.getResult())) {
        LogHelper.error(
            node,
            "0xA3001",
            "The type of one of the arguments of the OCLArrayQualification is not integral");
        typeCheckResult.reset();
        return;
      }
      typeCheckResult.reset();
    }
    if (exprResult instanceof SymTypeArray) {
      LogHelper.setCurrentNode(node);
      exprResult =
          getCorrectResultArrayExpression(
              node.getEnclosingScope(), exprResult, (SymTypeArray) exprResult);
      typeCheckResult.setResult(exprResult);
      return;
    }
    if (exprResult instanceof SymTypeOfGenerics) {
      SymTypeOfGenerics collection = (SymTypeOfGenerics) exprResult;
      if (collection.getArgumentList().size() > 1
          && !collection.getTypeConstructorFullName().equals("java.util.Map")) {
        LogHelper.error(
            node,
            "0xA3002",
            "Array qualifications can only be used with one type argument or a map");
      }
      if (collection.getTypeConstructorFullName().equals("java.util.Map")) {
        typeCheckResult.setResult(collection.getArgument(1));
      } else {
        typeCheckResult.setResult(collection.getArgument(0));
      }
      return;
    }

    LogHelper.error(
        node,
        "0xA3001",
        "The type of the expression of the OCLArrayQualification has to be SymTypeArray");
  }

  @Override
  public void traverse(ASTOCLAtPreQualification node) {
    SymTypeExpression exprResult;
    if (node.getExpression() != null) {
      node.getExpression().accept(getTraverser());
    }
    if (!typeCheckResult.isPresentResult()) {
      LogHelper.error(
          node,
          "0xA3001",
          "The type of the expression of the OCLAtPreQualification could not be calculated");
      return;
    }
    exprResult = typeCheckResult.getResult();
    typeCheckResult.reset();
    typeCheckResult.setResult(exprResult);
  }

  @Override
  public void traverse(ASTOCLTransitiveQualification node) {
    SymTypeExpression exprResult;
    if (node.getExpression() != null) {
      node.getExpression().accept(getTraverser());
    }
    if (!typeCheckResult.isPresentResult()) {
      LogHelper.error(
          node,
          "0xA3001",
          "The type of the expression of the OCLTransitiveQualification could not be calculated");
      return;
    }
    exprResult = typeCheckResult.getResult();
    typeCheckResult.reset();
    typeCheckResult.setResult(exprResult);
  }

  private void checkAndSetBooleanTypes(ASTExpression left, ASTExpression right, String astType) {
    SymTypeExpression leftResult;
    SymTypeExpression rightResult;

    if (left != null) {
      left.accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      leftResult = typeCheckResult.getResult();
      typeCheckResult.reset();
    } else {
      Log.error(
          "0xA3200 The type of the left expression of the " + astType + " could not be calculated");
      return;
    }

    if (!TypeCheck.isBoolean(leftResult)) {
      typeCheckResult.reset();
      Log.error("0xA3201 The type of the left expression of the " + astType + " has to be boolean");
      return;
    }

    if (right != null) {
      right.accept(getTraverser());
    }
    if (typeCheckResult.isPresentResult()) {
      rightResult = typeCheckResult.getResult();
      typeCheckResult.reset();
    } else {
      Log.error(
          "0xA3202 The type of the right expression of the "
              + astType
              + " could not be calculated");
      return;
    }

    if (!TypeCheck.isBoolean(rightResult)) {
      typeCheckResult.reset();
      Log.error(
          "0xA3203 The type of the right expression of the " + astType + " has to be boolean");
      return;
    }

    // return type is always boolean
    typeCheckResult.setResult(createBoolean());
  }

  public static SymTypeExpression createBoolean() {
    return SymTypeExpressionFactory.createPrimitive("boolean");
  }

  protected SymTypeExpression getCorrectResultArrayExpression(
      IExpressionsBasisScope scope, SymTypeExpression arrayTypeResult, SymTypeArray arrayResult) {
    SymTypeExpression wholeResult;
    if (arrayResult.getDim() > 1) {
      // case 1: A[][] bar -> bar[3] returns the type A[] -> decrease the dimension of the array by
      // 1
      wholeResult =
          SymTypeExpressionFactory.createTypeArray(
              arrayTypeResult.getTypeInfo(),
              arrayResult.getDim() - 1,
              SymTypeExpressionFactory.createPrimitive("int"));
    } else {
      // case 2: A[] bar -> bar[3] returns the type A
      // determine whether the result has to be a constant, generic or object
      if (arrayResult.getTypeInfo().getTypeParameterList().isEmpty()) {
        // if the return type is a primitive
        if (SymTypePrimitive.boxMap.containsKey(arrayResult.getTypeInfo().getName())) {
          wholeResult =
              SymTypeExpressionFactory.createPrimitive(arrayResult.getTypeInfo().getName());
        } else {
          // if the return type is an object
          wholeResult = SymTypeExpressionFactory.createTypeObject(arrayResult.getTypeInfo());
        }
      } else {
        // the return type must be a generic
        List<SymTypeExpression> typeArgs = Lists.newArrayList();
        for (TypeVarSymbol s : arrayResult.getTypeInfo().getTypeParameterList()) {
          typeArgs.add(SymTypeExpressionFactory.createTypeVariable(s));
        }
        wholeResult = SymTypeExpressionFactory.createGenerics(arrayResult.getTypeInfo(), typeArgs);
        wholeResult =
            replaceTypeVariables(
                wholeResult,
                typeArgs,
                ((SymTypeOfGenerics) arrayResult.getArgument()).getArgumentList());
      }
    }
    return wholeResult;
  }

  protected SymTypeExpression replaceTypeVariables(
      SymTypeExpression wholeResult,
      List<SymTypeExpression> typeArgs,
      List<SymTypeExpression> argumentList) {
    Map<SymTypeExpression, SymTypeExpression> map = Maps.newHashMap();
    if (typeArgs.size() != argumentList.size()) {
      LogHelper.error("0xA0297", "different amount of type variables and type arguments");
    } else {
      for (int i = 0; i < typeArgs.size(); i++) {
        map.put(typeArgs.get(i), argumentList.get(i));
      }

      List<SymTypeExpression> oldArgs = ((SymTypeOfGenerics) wholeResult).getArgumentList();
      List<SymTypeExpression> newArgs = Lists.newArrayList();
      for (int i = 0; i < oldArgs.size(); i++) {
        if (map.containsKey(oldArgs.get(i))) {
          newArgs.add(map.get(oldArgs.get(i)));
        } else {
          newArgs.add(oldArgs.get(i));
        }
      }
      ((SymTypeOfGenerics) wholeResult).setArgumentList(newArgs);
    }
    return wholeResult;
  }

  @Override
  public OCLExpressionsTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(OCLExpressionsTraverser traverser) {
    this.traverser = traverser;
  }
}
