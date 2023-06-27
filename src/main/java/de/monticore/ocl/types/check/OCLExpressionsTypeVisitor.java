/*
 *  (c) https://github.com/MontiCore/monticore
 */

package de.monticore.ocl.types.check;

import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.ocl.util.LogHelper;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.AbstractTypeVisitor;

import static de.monticore.types.check.SymTypeExpressionFactory.createObscureType;

public class OCLExpressionsTypeVisitor extends AbstractTypeVisitor
    implements OCLExpressionsVisitor2, OCLExpressionsHandler {
  
  protected OCLExpressionsTraverser traverser;
  
  @Override
  public OCLExpressionsTraverser getTraverser() {
    return traverser;
  }
  
  @Override
  public void setTraverser(OCLExpressionsTraverser traverser) {
    this.traverser = traverser;
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
}
