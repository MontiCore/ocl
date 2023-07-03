/*
 *  (c) https://github.com/MontiCore/monticore
 */

package de.monticore.ocl.types.check;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.ocl.util.LogHelper;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.types.check.*;
import de.monticore.types3.AbstractTypeVisitor;
import de.monticore.types3.SymTypeRelations;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Map;

import static de.monticore.types.check.SymTypeExpressionFactory.createObscureType;
import static de.monticore.types.check.SymTypeExpressionFactory.createPrimitive;

public class OCLExpressionsTypeVisitor extends AbstractTypeVisitor
    implements OCLExpressionsVisitor2 {
  
  protected SymTypeRelations typeRelations;
  
  public OCLExpressionsTypeVisitor() {
    this(new SymTypeRelations());
  }
  
  protected OCLExpressionsTypeVisitor(SymTypeRelations typeRelations) {
    this.typeRelations = typeRelations;
  }
  
  protected SymTypeRelations getTypeRel() {
    return typeRelations;
  }
  
  @Override
  public void endVisit(ASTTypeCastExpression expr) {
    var typeResult = getType4Ast().getPartialTypeOfTypeId(expr.getMCType());
    var exprResult = getType4Ast().getPartialTypeOfExpr(expr.getExpression());
    
    SymTypeExpression result;
    if (typeResult.isObscureType() || exprResult.isObscureType()) {
      // if any inner obscure then error already logged
      result = createObscureType();
    }
    else if (OCLTypeCheck.compatible(typeResult, exprResult)) { // TODO MSm support downcasts?
      // check whether typecast is possible
      result = typeResult;
    }
    else {
      result = createObscureType();
      LogHelper.error(expr, "0xA3082",
          "The type of the expression can't be cast to the given type '" +
              typeResult.getTypeInfo().getName());
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTTypeIfExpression expr) {
    var typeResult = getType4Ast().getPartialTypeOfTypeId(expr.getMCType());
    var thenResult = getType4Ast().getPartialTypeOfExpr(expr.getThenExpression().getExpression());
    var elseResult = getType4Ast().getPartialTypeOfExpr(expr.getElseExpression());
    
    SymTypeExpression result;
    if (typeResult.isObscureType() || thenResult.isObscureType() || elseResult.isObscureType()) {
      // if any inner obscure then error already logged
      result = createObscureType();
    }
    else if (OCLTypeCheck.compatible(thenResult, elseResult)) {
      result = thenResult;
    }
    else if (OCLTypeCheck.isSubtypeOf(thenResult, elseResult)) {
      result = elseResult;
    }
    else {
      result = createObscureType();
      LogHelper.error(expr, "0xA3015",
          "The type of the else expression of the OCLTypeIfExpr doesn't match the then expression");
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTIfThenElseExpression expr) {
    var conditionResult = getType4Ast().getPartialTypeOfExpr(expr.getCondition());
    var thenResult = getType4Ast().getPartialTypeOfExpr(expr.getThenExpression());
    var elseResult = getType4Ast().getPartialTypeOfExpr(expr.getElseExpression());
    
    SymTypeExpression result;
    if (conditionResult.isObscureType() || thenResult.isObscureType() ||
        elseResult.isObscureType()) {
      // if any inner obscure then error already logged
      result = createObscureType();
    }
    else if (OCLTypeCheck.compatible(thenResult, elseResult)) {
      // Type of else is subtype of/or same type as then -> return then-type
      result = thenResult;
    }
    else if (OCLTypeCheck.isSubtypeOf(thenResult, elseResult)) {
      // Type of then is subtype of else -> return else-type
      result = elseResult;
    }
    else {
      result = createObscureType();
      LogHelper.error(expr, "0xA3044",
          "The type of the else expression of the OCLIfThenElseExpr doesn't match the then expression");
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTImpliesExpression expr) {
    getType4Ast().setTypeOfExpression(expr,
        calculateConditionalBooleanOp(expr.getLeft(), expr.getRight(), "ImpliesExpression")
    );
  }
  
  @Override
  public void endVisit(ASTEquivalentExpression expr) {
    getType4Ast().setTypeOfExpression(expr,
        calculateConditionalBooleanOp(expr.getLeft(), expr.getRight(), "EquivalentExpression")
    );
  }
  
  //TODO MSm discuss common solution with CommonExpressionsTypeVisitor.calculateConditionalBooleanOp
  protected SymTypeExpression calculateConditionalBooleanOp(ASTExpression left, ASTExpression right,
      String operation) {
    var leftResult = getType4Ast().getPartialTypeOfExpr(left);
    var rightResult = getType4Ast().getPartialTypeOfExpr(right);
    
    if (leftResult.isObscureType() || rightResult.isObscureType()) {
      // if any inner obscure then error already logged
      return createObscureType();
    }
    else if (getTypeRel().isBoolean(leftResult) && getTypeRel().isBoolean(rightResult)) {
      return createPrimitive(BasicSymbolsMill.BOOLEAN);
    }
    else {
      // operator not applicable
      Log.error(
          "0xA3203 The type of the right expression of the " + operation + " has to be boolean");
      return createObscureType();
    }
  }
  
  @Override
  public void endVisit(ASTForallExpression expr) {
    var exprResult = getType4Ast().getPartialTypeOfExpr(expr.getExpression());
    
    SymTypeExpression result;
    if (exprResult.isObscureType()) {
      result = createObscureType();
    }
    else if (typeRelations.isBoolean(exprResult)) {
      // TODO MSm Is there a better method?
      result = SymTypeExpressionFactory.createPrimitive("boolean");
    }
    else {
      LogHelper.error(expr, "0xA3212",
          "The type of the expression in the ForallExpression has to be boolean");
      result = createObscureType();
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTExistsExpression expr) {
    var exprResult = getType4Ast().getPartialTypeOfExpr(expr.getExpression());
    
    SymTypeExpression result;
    if (exprResult.isObscureType()) {
      result = createObscureType();
    }
    else if (typeRelations.isBoolean(exprResult)) {
      // TODO MSm Is there a better method?
      result = SymTypeExpressionFactory.createPrimitive("boolean");
    }
    else {
      LogHelper.error(expr, "0xA3212",
          "The type of the expression in the ExistsExpression has to be boolean");
      result = createObscureType();
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTAnyExpression expr) {
    var exprResult = getType4Ast().getPartialTypeOfExpr(expr.getExpression());
    
    SymTypeExpression result;
    if (exprResult.isObscureType()) {
      result = createObscureType();
    }
    else if (exprResult instanceof SymTypeOfGenerics) {
      result = OCLTypeCheck.unwrapSet(exprResult);
    }
    else {
      result = exprResult;
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTLetinExpression expr) {
    var obscureDeclaration = expr.streamOCLVariableDeclarations().anyMatch(decl -> {
      return getType4Ast().getPartialTypeOfExpr(decl.getExpression()).isObscureType();
    });
    
    var exprResult = getType4Ast().getPartialTypeOfExpr(expr.getExpression());
    
    SymTypeExpression result;
    if (exprResult.isObscureType() || obscureDeclaration) {
      result = createObscureType();
    }
    else {
      result = exprResult;
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTIterateExpression expr) {
    var initResult = getType4Ast().getPartialTypeOfExpr(expr.getInit().getExpression());
    var valueResult = getType4Ast().getPartialTypeOfExpr(expr.getValue());
    
    // TODO MSm typecheck in Declaration?
    SymTypeExpression result;
    if (initResult.isObscureType() || valueResult.isObscureType()) {
      result = createObscureType();
    }
    else if (OCLTypeCheck.compatible(initResult, valueResult)) { //TODO MSm use subtype instead?
      result = initResult;
      Log.error("0xA3074 The type of the value of the OCLIterateExpression (" + valueResult.print() + ") has to match the type of the init declaration (" +
          initResult.print() + ")");
    }
    else {
      result = createObscureType();
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTInstanceOfExpression expr) {
    var exprResult = getType4Ast().getPartialTypeOfExpr(expr.getExpression());
    var typeResult = getType4Ast().getPartialTypeOfTypeId(expr.getMCType());
    
    SymTypeExpression result;
    if (exprResult.isObscureType() || typeResult.isObscureType()) {
      result = createObscureType();
    }
    else {
      result = SymTypeExpressionFactory.createPrimitive("boolean");
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTOCLArrayQualification expr) {
    var exprResult = getType4Ast().getPartialTypeOfExpr(expr.getExpression());
    var hasObscureOrNonIntegralArgument = expr.streamArguments().anyMatch(arg -> {
      var partialExpr = getType4Ast().getPartialTypeOfExpr(arg);
      return partialExpr.isObscureType() || !getTypeRel().isIntegralType(partialExpr);
    });
    
    SymTypeExpression result;
    if (exprResult.isObscureType() || hasObscureOrNonIntegralArgument) {
      result = createObscureType();
    }
    else if (exprResult instanceof SymTypeArray) {
      LogHelper.setCurrentNode(expr);
      exprResult = getCorrectResultArrayExpression(exprResult, (SymTypeArray) exprResult);
      result = exprResult;
    }
    else if (exprResult instanceof SymTypeOfGenerics) {
      SymTypeOfGenerics collection = (SymTypeOfGenerics) exprResult;
      if (collection.getArgumentList().size() > 1
          && !collection.getTypeConstructorFullName().equals("java.util.Map")) {
        LogHelper.error(
            expr,
            "0xA3002",
            "Array qualifications can only be used with one type argument or a map");
        result = createObscureType(); // TODO MSm wasn't obscure before
      }
      else if (collection.getTypeConstructorFullName().equals("java.util.Map")) {
        result = collection.getArgument(1);
      }
      else {
        result = collection.getArgument(0);
      }
    }
    else {
      result = createObscureType();
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  protected SymTypeExpression getCorrectResultArrayExpression(SymTypeExpression arrayTypeResult, SymTypeArray arrayResult) {
    SymTypeExpression wholeResult;
    if (arrayResult.getDim() > 1) {
      // case 1: A[][] bar -> bar[3] returns the type A[] -> decrease the dimension of the array by
      // 1
      wholeResult =
          SymTypeExpressionFactory.createTypeArray( // TODO MSm replace with what?
              arrayTypeResult.getTypeInfo(),
              arrayResult.getDim() - 1,
              SymTypeExpressionFactory.createPrimitive("int"));
    }
    else {
      // case 2: A[] bar -> bar[3] returns the type A
      // determine whether the result has to be a constant, generic or object
      if (arrayResult.getTypeInfo().getTypeParameterList().isEmpty()) {
        // if the return type is a primitive
        // TODO MSm replace with what? getTypeRel().unbox()
        if (SymTypePrimitive.boxMap.containsKey(arrayResult.getTypeInfo().getName())) {
          wholeResult =
              SymTypeExpressionFactory.createPrimitive(arrayResult.getTypeInfo().getName());
        }
        else {
          // if the return type is an object
          wholeResult = SymTypeExpressionFactory.createTypeObject(arrayResult.getTypeInfo());
        }
      }
      else {
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
    }
    else {
      for (int i = 0; i < typeArgs.size(); i++) {
        map.put(typeArgs.get(i), argumentList.get(i));
      }
      
      List<SymTypeExpression> oldArgs = ((SymTypeOfGenerics) wholeResult).getArgumentList();
      List<SymTypeExpression> newArgs = Lists.newArrayList();
      for (SymTypeExpression oldArg : oldArgs) {
        newArgs.add(map.getOrDefault(oldArg, oldArg));
      }
      ((SymTypeOfGenerics) wholeResult).setArgumentList(newArgs);
    }
    return wholeResult;
  }
  
  @Override
  public void endVisit(ASTOCLAtPreQualification expr) {
    var exprResult = getType4Ast().getPartialTypeOfExpr(expr.getExpression());
    
    SymTypeExpression result;
    if(exprResult.isObscureType()) {
      result = createObscureType();
    } else {
      result = exprResult;
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTOCLTransitiveQualification expr) {
    var exprResult = getType4Ast().getPartialTypeOfExpr(expr.getExpression());
    
    SymTypeExpression result;
    if(exprResult.isObscureType()) {
      result = createObscureType();
    } else {
      result = exprResult;
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
}