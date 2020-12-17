/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.types.check;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._symboltable.IExpressionsBasisScope;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.types.check.*;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Map;

public class DeriveSymTypeOfOCLExpressions extends DeriveSymTypeOfExpression implements OCLExpressionsVisitor {

  private OCLExpressionsVisitor realThis;

  public DeriveSymTypeOfOCLExpressions() {
    this.realThis = this;
  }

  @Override
  public void setRealThis(OCLExpressionsVisitor realThis) {
    this.realThis = realThis;
  }

  @Override
  public OCLExpressionsVisitor getRealThis() {
    return realThis;
  }

  @Override
  public void traverse(ASTTypeCastExpression node) {
    SymTypeExpression exprResult = null;
    SymTypeExpression typeResult = null;

    //check type of Expression
    if (node.getExpression() != null) {
      node.getExpression().accept(getRealThis());
    }
    if (typeCheckResult.isPresentCurrentResult()) {
      exprResult = typeCheckResult.getCurrentResult();
      typeCheckResult.reset();
    }
    else {
      Log.error("0xA3080 The type of the expression of the OCLTypeCastExpression could not be calculated");
      return;
    }

    //check type of type to cast expression to
    if (node.getMCType() != null) {
      node.getMCType().accept(getRealThis());
    }
    if (typeCheckResult.isPresentCurrentResult()) {
      typeResult = typeCheckResult.getCurrentResult();
      typeCheckResult.reset();
    }
    else {
      Log.error("0xA3081 The type of the MCType of the OCLTypeCastExpression could not be calculated");
      return;
    }

    //check whether typecast is possible
    if (!OCLTypeCheck.compatible(typeResult, exprResult)) {
      typeCheckResult.reset();
      Log.error("0xA3082 The type of the expression of the OCLTypeCastExpression can't be cast to given type");
      return;
    }
    else {
      //set result to typecasted expression
      typeCheckResult.setCurrentResult(typeResult.deepClone());
    }
  }

  @Override
  public void traverse(ASTTypeIfExpression node) {
    SymTypeExpression thenResult = null;
    SymTypeExpression elseResult = null;

    //resolve MCType to SymTypeExpression
    if (node.getMCType() != null) {
      node.getMCType().accept(getRealThis());
    }
    if (typeCheckResult.isPresentCurrentResult()) {
      typeCheckResult.reset();
    }
    else {
      Log.error("0xA3012 The type of the MCType of the OCLInstanceOfExpression of the OCLTypeIfExpr could not be calculated");
      return;
    }

    if (node.getThenExpression() != null) {
      node.getThenExpression().accept(getRealThis());
    }
    if (typeCheckResult.isPresentCurrentResult()) {
      thenResult = typeCheckResult.getCurrentResult();
      typeCheckResult.reset();
    }
    else {
      Log.error("0xA3013 The type of the then expression of the OCLTypeIfExpression could not be calculated");
      return;
    }

    if (node.getElseExpression() != null) {
      node.getElseExpression().accept(getRealThis());
    }
    if (typeCheckResult.isPresentCurrentResult()) {
      elseResult = typeCheckResult.getCurrentResult();
      typeCheckResult.reset();
    }
    else {
      Log.error("0xA3014 The type of the else expression of the OCLTypeIfExpr could not be calculated");
      return;
    }

    if (OCLTypeCheck.compatible(thenResult, elseResult)) {
      typeCheckResult.setCurrentResult(thenResult);
    }
    else if (OCLTypeCheck.isSubtypeOf(thenResult, elseResult)) {
      typeCheckResult.setCurrentResult(elseResult);
    }
    else {
      typeCheckResult.reset();
      Log.error("0xA3015 The type of the else expression of the OCLTypeIfExpr doesn't match the then expression");
      return;
    }
  }

  @Override
  public void traverse(ASTIfThenElseExpression node) {
    SymTypeExpression conditionResult = null;
    SymTypeExpression thenResult = null;
    SymTypeExpression elseResult = null;

    if (node.getCondition() != null) {
      node.getCondition().accept(getRealThis());
    }
    if (typeCheckResult.isPresentCurrentResult()) {
      conditionResult = typeCheckResult.getCurrentResult();
      typeCheckResult.reset();
    }
    else {
      Log.error("0xA3040 The type of the left expression of the OCLIfThenElseExpr could not be calculated");
      return;
    }

    // the condition has to be boolean
    if (!OCLTypeCheck.isBoolean(conditionResult)) {
      typeCheckResult.reset();
      Log.error("0xA3041 The type of the condition of the OCLIfThenElseExpr has to be boolean");
      return;
    }

    if (node.getThenExpression() != null) {
      node.getThenExpression().accept(getRealThis());
    }
    if (typeCheckResult.isPresentCurrentResult()) {
      thenResult = typeCheckResult.getCurrentResult();
      typeCheckResult.reset();
    }
    else {
      Log.error("0xA3042 The type of the then expression of the OCLIfThenElseExpr could not be calculated");
    }

    if (node.getElseExpression() != null) {
      node.getElseExpression().accept(getRealThis());
    }
    if (typeCheckResult.isPresentCurrentResult()) {
      elseResult = typeCheckResult.getCurrentResult();
      typeCheckResult.reset();
    }
    else {
      Log.error("0xA3043 The type of the else expression of the OCLIfThenElseExpr could not be calculated");
      return;
    }

    if (OCLTypeCheck.compatible(thenResult, elseResult)) {
      // Type of else is subtype of/or same type as then -> return then-type
      typeCheckResult.setCurrentResult(thenResult);
    }
    else if (OCLTypeCheck.isSubtypeOf(thenResult, elseResult)) {
      // Type of then is subtype of else -> return else-type
      typeCheckResult.setCurrentResult(elseResult);
    }
    else {
      typeCheckResult.reset();
      Log.error("0xA3044 The type of the else expression of the OCLIfThenElseExpr doesn't match the then expression");
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
    
    if(node.getExpression() != null){
      node.getExpression().accept(getRealThis());
    }
    if(typeCheckResult.isPresentCurrentResult()){
      exprResult = typeCheckResult.getCurrentResult();
      typeCheckResult.reset();
    }
    else{
      Log.error("0xA3211 The type of the expression in the ForallExpression could not be calculated");
    }
    if (!OCLTypeCheck.isBoolean(exprResult)) {
      typeCheckResult.reset();
      Log.error("0xA3212 The type of the expression in the ForallExpression has to be boolean");
      return;
    }
    else {
      typeCheckResult.setCurrentResult(createBoolean());
    }
  }

  @Override
  public void traverse(ASTExistsExpression node) {
    SymTypeExpression exprResult = null;

    if(node.getExpression() != null){
      node.getExpression().accept(getRealThis());
    }
    if(typeCheckResult.isPresentCurrentResult()){
      exprResult = typeCheckResult.getCurrentResult();
      typeCheckResult.reset();
    }
    else{
      Log.error("0xA3211 The type of the expression in the ExistsExpression could not be calculated");
    }
    if (!OCLTypeCheck.isBoolean(exprResult)) {
      typeCheckResult.reset();
      Log.error("0xA3212 The type of the expression in the ExistsExpression has to be boolean");
      return;
    }
    else {
      typeCheckResult.setCurrentResult(createBoolean());
    }
  }

  @Override
  public void traverse(ASTAnyExpression node) {
    SymTypeExpression exprResult = null;

    if (node.getExpression() != null) {
      node.getExpression().accept(getRealThis());
    }
    if (typeCheckResult.isPresentCurrentResult()) {
      exprResult = typeCheckResult.getCurrentResult();
      typeCheckResult.reset();
    }
    else {
      Log.error("0xA3050 The type of the expression of the OCLAnyExpr could not be calculated");
      return;
    }

    if (exprResult instanceof SymTypeOfGenerics) {
      typeCheckResult.setCurrentResult(exprResult);
    }
    else {
      typeCheckResult.setCurrentResult(exprResult);
    }
  }

  @Override
  public void traverse(ASTLetinExpression node) {
    SymTypeExpression exprResult = null;

    if (node.getOCLVariableDeclarationList() != null && !node.getOCLVariableDeclarationList().isEmpty()) {
      for (ASTOCLVariableDeclaration dec : node.getOCLVariableDeclarationList()) {
        dec.accept(getRealThis());
        if (!typeCheckResult.isPresentCurrentResult()) {
          Log.error("0xA3060 The type of the OCLVariableDeclaration of the LetinExpr could not be calculated");
          return;
        }
      }
    }

    if (node.getExpression() != null) {
      node.getExpression().accept(getRealThis());
    }
    if (typeCheckResult.isPresentCurrentResult()) {
      exprResult = typeCheckResult.getCurrentResult();
      typeCheckResult.reset();
    }
    else {
      Log.error("0xA3061 The type of the expression of the LetinExpr could not be calculated");
      return;
    }

    typeCheckResult.setCurrentResult(exprResult);
  }

  @Override
  public void traverse(ASTIterateExpression node) {
    SymTypeExpression valueResult = null;
    SymTypeExpression initResult = null;

    if (node.getInit() != null) {
      node.getInit().accept(getRealThis());
    }
    if (!typeCheckResult.isPresentCurrentResult()) {
      Log.error("0xA3071 The type of the init of the OCLIterateExpression could not be calculated");
      return;
    }
    else {
      initResult = typeCheckResult.getCurrentResult();
      typeCheckResult.reset();
    }

    if (node.getValue() != null) {
      node.getValue().accept(getRealThis());
    }
    if (!typeCheckResult.isPresentCurrentResult()) {
      Log.error("0xA3073 The type of the value of the OCLIterateExpression could not be calculated");
      return;
    }
    else {
      valueResult = typeCheckResult.getCurrentResult();
      typeCheckResult.reset();
    }

    if (!OCLTypeCheck.compatible(initResult, valueResult)) {
      typeCheckResult.reset();
      Log.error("0xA3074 The type of the value of the OCLIterateExpression (" + valueResult.print() +
              ") has to match the type of the init declaration (" + initResult.print() + ")");
      return;
    }
    else {
      typeCheckResult.setCurrentResult(initResult);
    }
  }

  @Override
  public void traverse(ASTInstanceOfExpression node) {
    if (node.getExpression() != null) {
      node.getExpression().accept(getRealThis());
    }
    if (typeCheckResult.isPresentCurrentResult()) {
      typeCheckResult.reset();
    }
    else {
      Log.error("0xA3000 The type of the left expression of the OCLInstanceOfExpression could not be calculated");
      return;
    }

    if (node.getMCType() != null) {
      node.getMCType().accept(getRealThis());
    }
    if (typeCheckResult.isPresentCurrentResult()) {
      typeCheckResult.reset();
    }
    else {
      Log.error("0xA3001 The type of the MCType of the OCLInstanceOfExpression could not be calculated");
      return;
    }

    final SymTypeExpression wholeResult = createBoolean();
    typeCheckResult.setCurrentResult(wholeResult);
  }

  @Override
  public void traverse(ASTOCLArrayQualification node){
    SymTypeExpression exprResult;
    if (node.getExpression() != null) {
      node.getExpression().accept(getRealThis());
    }
    if (!typeCheckResult.isPresentCurrentResult()) {
      Log.error("0xA3001 The type of the expression of the OCLArrayQualification could not be calculated");
      return;
    }
    exprResult = typeCheckResult.getCurrentResult();
    typeCheckResult.reset();
    for (ASTExpression e : node.getArgumentsList()){
      if (e != null) {
        e.accept(getRealThis());
      }
      if (!typeCheckResult.isPresentCurrentResult()) {
        Log.error("0xA3001 The type of a expression in the arguments of the OCLArrayQualification could not be calculated");
        typeCheckResult.reset();
        return;
      }
      if(!isIntegralType(typeCheckResult.getCurrentResult())){
        Log.error("0xA3001 The type of one of the arguments of the OCLArrayQualification is not integral");
        typeCheckResult.reset();
        return;
      }
      typeCheckResult.reset();
    }
    if(!(exprResult instanceof SymTypeArray)){
      Log.error("0xA3001 The type of the expression of the OCLArrayQualification has to be SymTypeArray");
      return;
    }
    exprResult =  getCorrectResultArrayExpression(node.getEnclosingScope(), exprResult, (SymTypeArray) exprResult);
    typeCheckResult.setCurrentResult(exprResult);
  }

  @Override
  public void traverse(ASTOCLAtPreQualification node){
    SymTypeExpression exprResult;
    if (node.getExpression() != null) {
      node.getExpression().accept(getRealThis());
    }
    if (!typeCheckResult.isPresentCurrentResult()) {
      Log.error("0xA3001 The type of the expression of the OCLAtPreQualification could not be calculated");
      return;
    }
    exprResult = typeCheckResult.getCurrentResult();
    typeCheckResult.reset();
    typeCheckResult.setCurrentResult(exprResult);
  }

  @Override
  public void traverse(ASTOCLTransitiveQualification node){
    SymTypeExpression exprResult;
    if (node.getExpression() != null) {
      node.getExpression().accept(getRealThis());
    }
    if (!typeCheckResult.isPresentCurrentResult()) {
      Log.error("0xA3001 The type of the expression of the OCLTransitiveQualification could not be calculated");
      return;
    }
    exprResult = typeCheckResult.getCurrentResult();
    typeCheckResult.reset();
    typeCheckResult.setCurrentResult(exprResult);
  }
  
  private void checkAndSetBooleanTypes(ASTExpression left, ASTExpression right, String astType) {
    SymTypeExpression leftResult;
    SymTypeExpression rightResult;

    if (left != null) {
      left.accept(getRealThis());
    }
    if (typeCheckResult.isPresentCurrentResult()) {
      leftResult = typeCheckResult.getCurrentResult();
      typeCheckResult.reset();
    }
    else {
      Log.error("0xA3200 The type of the left expression of the " + astType + " could not be calculated");
      return;
    }

    if (!OCLTypeCheck.isBoolean(leftResult)) {
      typeCheckResult.reset();
      Log.error("0xA3201 The type of the left expression of the " + astType + " has to be boolean");
      return;
    }

    if (right != null) {
      right.accept(getRealThis());
    }
    if (typeCheckResult.isPresentCurrentResult()) {
      rightResult = typeCheckResult.getCurrentResult();
      typeCheckResult.reset();
    }
    else {
      Log.error("0xA3202 The type of the right expression of the " + astType + " could not be calculated");
      return;
    }

    if (!OCLTypeCheck.isBoolean(rightResult)) {
      typeCheckResult.reset();
      Log.error("0xA3203 The type of the right expression of the " + astType + " has to be boolean");
      return;
    }

    // return type is always boolean
    typeCheckResult.setCurrentResult(createBoolean());
  }

  public static SymTypeExpression createBoolean() {
    return SymTypeExpressionFactory.createTypeConstant("boolean");
  }

  private SymTypeExpression getCorrectResultArrayExpression(IExpressionsBasisScope scope, SymTypeExpression arrayTypeResult, SymTypeArray arrayResult) {
    SymTypeExpression wholeResult;
    if(arrayResult.getDim()>1){
      //case 1: A[][] bar -> bar[3] returns the type A[] -> decrease the dimension of the array by 1
      wholeResult = SymTypeExpressionFactory.createTypeArray(arrayTypeResult.getTypeInfo().getName(),getScope(scope),
              arrayResult.getDim()-1,SymTypeExpressionFactory.createTypeConstant("int"));
    }else {
      //case 2: A[] bar -> bar[3] returns the type A
      //determine whether the result has to be a constant, generic or object
      if(arrayResult.getTypeInfo().getTypeParameterList().isEmpty()){
        //if the return type is a primitive
        if(SymTypeConstant.boxMap.containsKey(arrayResult.getTypeInfo().getName())){
          wholeResult = SymTypeExpressionFactory.createTypeConstant(arrayResult.getTypeInfo().getName());
        }else {
          //if the return type is an object
          wholeResult = SymTypeExpressionFactory.createTypeObject(arrayResult.getTypeInfo().getName(), getScope(scope));
        }
      }else {
        //the return type must be a generic
        List<SymTypeExpression> typeArgs = Lists.newArrayList();
        for(TypeVarSymbol s : arrayResult.getTypeInfo().getTypeParameterList()){
          typeArgs.add(SymTypeExpressionFactory.createTypeVariable(s.getName(),getScope(scope)));
        }
        wholeResult = SymTypeExpressionFactory.createGenerics(arrayResult.getTypeInfo().getName(), getScope(scope), typeArgs);
        wholeResult = replaceTypeVariables(wholeResult,typeArgs,((SymTypeOfGenerics)arrayResult.getArgument()).getArgumentList());
      }
    }
    return wholeResult;
  }

  private SymTypeExpression replaceTypeVariables(SymTypeExpression wholeResult, List<SymTypeExpression> typeArgs, List<SymTypeExpression> argumentList) {
    Map<SymTypeExpression,SymTypeExpression> map = Maps.newHashMap();
    if(typeArgs.size()!=argumentList.size()){
      Log.error("0xA0297 different amount of type variables and type arguments");
    }else{
      for(int i = 0;i<typeArgs.size();i++){
        map.put(typeArgs.get(i),argumentList.get(i));
      }

      List<SymTypeExpression> oldArgs = ((SymTypeOfGenerics) wholeResult).getArgumentList();
      List<SymTypeExpression> newArgs = Lists.newArrayList();
      for(int i = 0;i<oldArgs.size();i++){
        if(map.containsKey(oldArgs.get(i))){
          newArgs.add(map.get(oldArgs.get(i)));
        }else{
          newArgs.add(oldArgs.get(i));
        }
      }
      ((SymTypeOfGenerics) wholeResult).setArgumentList(newArgs);
    }
    return wholeResult;
  }


}
