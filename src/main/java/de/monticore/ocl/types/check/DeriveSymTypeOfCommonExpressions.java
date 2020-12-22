package de.monticore.ocl.types.check;

import de.monticore.expressions.commonexpressions._ast.ASTCallExpression;
import de.monticore.expressions.commonexpressions._ast.ASTConditionalExpression;
import de.monticore.expressions.commonexpressions._ast.ASTInfixExpression;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.monticore.ocl.types.check.OCLTypeCheck.*;

public class DeriveSymTypeOfCommonExpressions extends de.monticore.types.check.DeriveSymTypeOfCommonExpressions {

  /**
   * All methods in this class are identical to the methods in
   * de.monticore.types.check.DeriveSymTypeOfCommonExpressions.
   * This class is used to ensure that OCLTypeCheck methods are
   * used instead of the normal TypeCheck methods.
   */

  @Override
  protected Optional<SymTypeExpression> calculateConditionalExpressionType(ASTConditionalExpression expr,
                                                                           SymTypeExpression conditionResult,
                                                                           SymTypeExpression trueResult,
                                                                           SymTypeExpression falseResult) {
    Optional<SymTypeExpression> wholeResult = Optional.empty();
    //condition has to be boolean
    if (isBoolean(conditionResult)) {
      //check if "then" and "else" are either from the same type or are in sub-supertype relation
      if (compatible(trueResult, falseResult)) {
        wholeResult = Optional.of(trueResult);
      } else if (compatible(falseResult, trueResult)) {
        wholeResult = Optional.of(falseResult);
      } else {
        // first argument can be null since it should not be relevant to the type calculation
        wholeResult = getBinaryNumericPromotion(trueResult, falseResult);
      }
    }
    return wholeResult;
  }

  @Override
  protected Optional<SymTypeExpression> calculateTypeLogical(ASTInfixExpression expr, SymTypeExpression rightResult, SymTypeExpression leftResult) {
    //Option one: they are both numeric types
    if (isNumericType(leftResult) && isNumericType(rightResult)
            || isBoolean(leftResult) && isBoolean(rightResult)) {
      return Optional.of(SymTypeExpressionFactory.createTypeConstant("boolean"));
    }
    //Option two: none of them is a primitive type and they are either the same type or in a super/sub type relation
    if (!leftResult.isTypeConstant() && !rightResult.isTypeConstant() &&
            (compatible(leftResult, rightResult) || compatible(rightResult, leftResult))
    ) {
      return Optional.of(SymTypeExpressionFactory.createTypeConstant("boolean"));
    }
    //should never happen, no valid result, error will be handled in traverse
    return Optional.empty();
  }

  private List<FunctionSymbol> getFittingMethods(List<FunctionSymbol> methodlist, ASTCallExpression expr){
    List<FunctionSymbol> fittingMethods = new ArrayList<>();
    for (FunctionSymbol method : methodlist) {
      //for every method found check if the arguments are correct
      if (expr.getArguments().getExpressionList().size() == method.getParameterList().size()) {
        boolean success = true;
        for (int i = 0; i < method.getParameterList().size(); i++) {
          expr.getArguments().getExpression(i).accept(getRealThis());
          //test if every single argument is correct
          if (!method.getParameterList().get(i).getType().deepEquals(typeCheckResult.getCurrentResult()) &&
                  !compatible(method.getParameterList().get(i).getType(), typeCheckResult.getCurrentResult())) {
            success = false;
          }
        }
        if (success) {
          //method has the correct arguments and return type
          fittingMethods.add(method);
        }
      }
    }
    return fittingMethods;
  }
}
