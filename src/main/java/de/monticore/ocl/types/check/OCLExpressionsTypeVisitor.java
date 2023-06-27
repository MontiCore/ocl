/*
 *  (c) https://github.com/MontiCore/monticore
 */

package de.monticore.ocl.types.check;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.ocl.util.LogHelper;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types3.AbstractTypeVisitor;
import de.monticore.types3.SymTypeRelations;
import de.se_rwth.commons.logging.Log;

import static de.monticore.types.check.SymTypeExpressionFactory.createObscureType;
import static de.monticore.types.check.SymTypeExpressionFactory.createPrimitive;

public class OCLExpressionsTypeVisitor extends AbstractTypeVisitor
    implements OCLExpressionsVisitor2, OCLExpressionsHandler {
  
  protected OCLExpressionsTraverser traverser;
  protected SymTypeRelations typeRelations;
  
  protected OCLExpressionsTypeVisitor(SymTypeRelations typeRelations) {
    this.typeRelations = typeRelations;
  }
  
  @Override
  public OCLExpressionsTraverser getTraverser() {
    return traverser;
  }
  
  @Override
  public void setTraverser(OCLExpressionsTraverser traverser) {
    this.traverser = traverser;
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
    else if (OCLTypeCheck.compatible(typeResult, exprResult)) {
      // check whether typecast is possible
      result = typeResult;
    }
    else {
      result = createObscureType();
      LogHelper.error(expr, "0xA3082",
          "The type of the expression of the OCLTypeCastExpression can't be cast to given type");
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
  
}
