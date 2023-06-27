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
    // check type of type to cast expression to
    SymTypeExpression typeResult = getType4Ast().getPartialTypeOfTypeId(expr.getMCType());
    
    // check type of Expression
    SymTypeExpression exprResult = getType4Ast().getPartialTypeOfExpr(expr.getExpression());
    
    SymTypeExpression result;
    if (typeResult.isObscureType() || exprResult.isObscureType()) {
      // if any inner obscure then error already logged
      result = createObscureType();
    }
    else {
      result = typeResult;
    }
    
    // check whether typecast is possible
    if (OCLTypeCheck.compatible(typeResult, exprResult)) {
      getType4Ast().setTypeOfExpression(expr, result);
    }
    else {
      LogHelper.error(expr, "0xA3082",
          "The type of the expression of the OCLTypeCastExpression can't be cast to given type");
    }
  }
}
