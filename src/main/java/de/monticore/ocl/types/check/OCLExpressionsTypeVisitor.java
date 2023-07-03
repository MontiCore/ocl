/*
 *  (c) https://github.com/MontiCore/monticore
 */

package de.monticore.ocl.types.check;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.types.check.SymTypeArray;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types3.AbstractTypeVisitor;
import de.monticore.types3.SymTypeRelations;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

import static de.monticore.types.check.SymTypeExpressionFactory.createObscureType;

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
    else if (getTypeRel().isNumericType(typeResult) && getTypeRel().isNumericType(exprResult)) {
      // allow to cast numbers down, e.g., (int) 5.0 or (byte) 5
      result = typeResult;
    }
    else if (getTypeRel().isSubTypeOf(exprResult, typeResult)) {
      // check whether typecast is possible
      result = typeResult;
    }
    else {
      result = createObscureType();
      
      Log.error(
          String.format("0xFD204 The expression of type '%s' can't be cast to the given type '%s'.",
              exprResult.printFullName(), typeResult.printFullName()),
          expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd());
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
    else if (getTypeRel().isCompatible(thenResult, elseResult)) {
      result = thenResult;
    }
    else if (getTypeRel().isSubTypeOf(thenResult, elseResult)) {
      result = elseResult;
    }
    else {
      result = createObscureType();
      
      Log.error(String.format(
              "0xFD205 The type '%s' of the else expression doesn't match the type '%s' of the then expression.",
              elseResult.printFullName(), thenResult.printFullName()), expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd());
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
    else if (getTypeRel().isCompatible(thenResult, elseResult)) {
      // Type of else is subtype of/or same type as then -> return then-type
      result = thenResult;
    }
    else if (getTypeRel().isSubTypeOf(thenResult, elseResult)) {
      // Type of then is subtype of else -> return else-type
      result = elseResult;
    }
    else {
      result = createObscureType();
      
      Log.error(String.format(
              "0xFD206 The type '%s' of the else expression doesn't match the type '%s' of the then expression.",
              elseResult.printFullName(), thenResult.printFullName()), expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd());
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTImpliesExpression expr) {
    getType4Ast().setTypeOfExpression(expr,
        calculateConditionalBooleanOperation(expr.getLeft(), expr.getRight(), "implies")
    );
  }
  
  @Override
  public void endVisit(ASTEquivalentExpression expr) {
    getType4Ast().setTypeOfExpression(expr,
        calculateConditionalBooleanOperation(expr.getLeft(), expr.getRight(), "equivalent")
    );
  }
  
  //TODO MSm merge with CommonExpressionsTypeVisitor.calculateConditionalBooleanOp before finishing updating the OCL type check.
  protected SymTypeExpression calculateConditionalBooleanOperation(ASTExpression left,
      ASTExpression right, String operator) {
    var leftResult = getType4Ast().getPartialTypeOfExpr(left);
    var rightResult = getType4Ast().getPartialTypeOfExpr(right);
    
    if (leftResult.isObscureType() || rightResult.isObscureType()) {
      // if any inner obscure then error already logged
      return createObscureType();
    }
    else if (getTypeRel().isBoolean(leftResult) && getTypeRel().isBoolean(rightResult)) {
      return SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
    }
    else {
      // operator not applicable
      Log.error(String.format(
              "0xFD207 The operator '%s' is not applicable to the expressions of type '%s' and '%s' but only to expressions of type boolean.",
              operator, leftResult.printFullName(), rightResult.printFullName()),
          left.get_SourcePositionStart(),
          right.get_SourcePositionEnd());
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
      // TODO FDr Extend the SymTypeExpressionsFactory using the static delegator pattern and add
      //  convenience methods for creating primitive types.
      // TODO MSm Use Convenience Methods as soon as they are available.
      result = SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
    }
    else {
      result = createObscureType();
      
      Log.error(String.format(
              "0xFD208 The type of the expression in the ForallExpression is '%s' but has to be boolean.",
              exprResult.printFullName()), expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd());
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
      result = SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
    }
    else {
      result = createObscureType();
      
      Log.error(String.format(
              "The type of the expression in the ExistsExpression is '%s' but has to be boolean.",
              exprResult.printFullName()), expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd());
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
    else if (exprResult.isGenericType()) {
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
    
    // TODO incorporate before merge: evaluateInDeclarationType
    SymTypeExpression result;
    if (initResult.isObscureType() || valueResult.isObscureType()) {
      result = createObscureType();
    }
    else if (!getTypeRel().isCompatible(initResult, valueResult)) {
      result = initResult;
      Log.error(String.format(
          "0xFD210 The type of the value of the OCLIterateExpression '%s' has to match the type of the init declaration '%s'",
          valueResult.printFullName(), initResult.printFullName()));
    }
    else {
      result = createObscureType();
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  protected SymTypeExpression evaluateInDeclarationType(ASTInDeclaration inDeclaration) {
    var typeResult = Optional.ofNullable(inDeclaration.getMCType());
    var expressionResult = Optional.ofNullable(inDeclaration.getExpression());
    
    SymTypeExpression result;
    if(typeResult.isPresent() && expressionResult.isPresent()) {
      // TODO MSm check before merge: MCType? (InDeclarationVariable || ",")+ ("in" Expression)
      result = createObscureType();
    } else if(expressionResult.isPresent()) {
      // TODO MSm check before merge: (InDeclarationVariable || ",")+ ("in" Expression)
      result = createObscureType();
    } else if(typeResult.isPresent()) {
      // TODO MSm check before merge: (InDeclarationVariable || ",")+
      result = createObscureType();
    } else {
      // should never occur
      result = createObscureType();
    }
    return result;
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
      result = SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
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
    else if (exprResult.isArrayType()) {
      result = evaluateArrayType(expr, (SymTypeArray) exprResult);
    }
    else if (exprResult.isGenericType()) {
      result = evaluateGenericType(expr, (SymTypeOfGenerics) exprResult);
    }
    else {
      result = createObscureType();
      
      Log.error(String.format(
              "0xFD219 The type of the expression of the OCLArrayQualification is '%s' but has to be an array or generic type.",
              exprResult.printFullName()),
          expr.get_SourcePositionStart(), expr.get_SourcePositionEnd());
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  protected SymTypeExpression evaluateArrayType(ASTOCLArrayQualification expr,
      SymTypeArray arrayResult) {
    SymTypeExpression result;
    if (arrayResult.getDim() == 1) {
      // case: A[] bar -> bar[3] returns the type A
      result = arrayResult.getArgument();
    }
    else if (arrayResult.getDim() > 1) {
      // case: A[][] bar -> bar[3] returns the type A[] -> decrease the dimension of the array by 1
      var intType = SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.INT);
      result = SymTypeExpressionFactory.createTypeArray(intType, arrayResult.getDim() - 1);
    }
    else {
      // case: dim < 1, should never occur
      result = createObscureType();
      
      Log.error("0xFD218 Array qualifications must have an array dimension of at least 1.",
          expr.get_SourcePositionStart(), expr.get_SourcePositionEnd());
    }
    return result;
  }
  
  protected SymTypeExpression evaluateGenericType(ASTOCLArrayQualification expr,
      SymTypeOfGenerics exprResult) {
    SymTypeExpression result;
    if (exprResult.getArgumentList().size() > 1
        && !exprResult.getTypeConstructorFullName().equals("java.util.Map")) {
      result = createObscureType();
      
      Log.error("0xFD211 Array qualifications can only be used with one type argument or a map.",
          expr.get_SourcePositionStart(), expr.get_SourcePositionEnd());
    }
    else if (exprResult.getTypeConstructorFullName().equals("java.util.Map")) {
      result = exprResult.getArgument(1);
    }
    else {
      result = exprResult.getArgument(0);
    }
    return result;
  }
  
  @Override
  public void endVisit(ASTOCLAtPreQualification expr) {
    var exprResult = getType4Ast().getPartialTypeOfExpr(expr.getExpression());
    
    SymTypeExpression result;
    if (exprResult.isObscureType()) {
      result = createObscureType();
    }
    else {
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