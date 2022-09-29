// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types.check;

import de.monticore.expressions.commonexpressions._ast.ASTCallExpression;
import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.expressions.commonexpressions._ast.ASTInfixExpression;
import de.monticore.expressions.prettyprint.CommonExpressionsFullPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbolSurrogate;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.monticore.ocl.types.check.OCLTypeCheck.compatible;
import static de.monticore.ocl.types.check.OCLTypeCheck.isBoolean;

public class DeriveSymTypeOfCommonExpressions
  extends de.monticore.types.check.DeriveSymTypeOfCommonExpressions {

  /**
   * OCL doesn't now OOTypes. Therefore we just accept function symbols parsed
   * from a class diagrams as static methods in type checks
   */
  @Override protected List<FunctionSymbol> filterModifiersFunctions(
    List<FunctionSymbol> fittingMethods) {
    // Method symbols
    List<FunctionSymbol> result = super.filterModifiersFunctions(fittingMethods);
    // Function symbols (cannot be cast - we'll just accept it)
    result.addAll(fittingMethods.stream().filter(m -> !(m instanceof MethodSymbol))
      .collect(Collectors.toList()));
    return result;
  }

  /**
   * All below methods in this class are identical to the methods in
   * de.monticore.types.check.DeriveSymTypeOfCommonExpressions.
   * This class is used to ensure that OCLTypeCheck methods are
   * used instead of the normal TypeCheck methods.
   */

  @Override
  public void traverse(ASTFieldAccessExpression expr) {
    CommonExpressionsFullPrettyPrinter printer = new CommonExpressionsFullPrettyPrinter(
      new IndentPrinter());
    SymTypeExpression innerResult;
    expr.getExpression().accept(getTraverser());
    if (!typeCheckResult.getResult().isObscureType()) {
      //store the type of the inner expression in a variable
      innerResult = typeCheckResult.getResult();
      //look for this type in our scope
      TypeSymbol innerResultType = innerResult.getTypeInfo();
      //search for a method, field or type in the scope of the type of the inner expression
      List<VariableSymbol> fieldSymbols = innerResult
        .getFieldList(expr.getName(), typeCheckResult.isType());
      Optional<TypeSymbol> typeSymbolOpt = innerResultType.getSpannedScope()
        .resolveType(expr.getName());
      if (!fieldSymbols.isEmpty()) {
        //cannot be a method, test variable first
        //durch AST-Umbau kann ASTFieldAccessExpression keine Methode sein
        //if the last result is a type then filter for static field symbols
        if (typeCheckResult.isType()) {
          fieldSymbols = filterStaticFieldSymbols(fieldSymbols);
        }
        if (fieldSymbols.size() != 1) {
          if (!checkIfCorrect(fieldSymbols)) {
            typeCheckResult.reset();
            logError("0xA0237", expr.get_SourcePositionStart());
          }
        }
        if (!fieldSymbols.isEmpty()) {
          VariableSymbol var = fieldSymbols.get(0);
          SymTypeExpression type = var.getType();
          typeCheckResult.setField();
          typeCheckResult.setResult(type);
        }
      }
      else if (typeSymbolOpt.isPresent()) {
        //no variable found, test type
        TypeSymbol typeSymbol = typeSymbolOpt.get();
        boolean match = true;
        //if the last result is a type and the type is not static then it is not accessible
        if (typeCheckResult.isType()) {
          if (!(typeSymbol instanceof OOTypeSymbol) || !(((OOTypeSymbol) typeSymbol)
            .isIsStatic())) {
            match = false;
          }
        }
        if (match) {
          SymTypeExpression wholeResult = SymTypeExpressionFactory
            .createTypeExpression(typeSymbol);
          typeCheckResult.setType();
          typeCheckResult.setResult(wholeResult);
        }
        else {
          typeCheckResult.reset();
          logError("0xA0303", expr.get_SourcePositionStart());
        }
      }
      else {
        if (!typeCheckResult.getResult().isObscureType()) {
          innerResult = typeCheckResult.getResult();
          //resolve methods with name of the inner expression
          List<FunctionSymbol> fittingMethods = innerResult.getMethodList(expr.getName(), typeCheckResult.isType());
          //if the last result is static then filter for static methods
          if(typeCheckResult.isType()){
            fittingMethods = filterModifiersFunctions(fittingMethods);
          }
          //there can only be one method with the correct arguments and return type
          if (!fittingMethods.isEmpty()) {
            if (fittingMethods.size() > 1) {
              SymTypeExpression returnType = fittingMethods.get(0).getType();
              for (FunctionSymbol method : fittingMethods) {
                if (!returnType.deepEquals(method.getType())) {
                  logError("0xA0238", expr.get_SourcePositionStart());
                }
              }
            }
            SymTypeExpression result = fittingMethods.get(0).getType();
            typeCheckResult.setMethod();
            typeCheckResult.setResult(result);
          } else {
            typeCheckResult.reset();
            logError("0xA0239", expr.get_SourcePositionStart());
          }
        } else {
          Collection<FunctionSymbol> methodcollection = getScope(expr.getEnclosingScope()).resolveFunctionMany(expr.getName());
          List<FunctionSymbol> fittingMethods = new ArrayList<>(methodcollection);
          //there can only be one method with the correct arguments and return type
          if (fittingMethods.size() == 1) {
            Optional<SymTypeExpression> wholeResult = Optional.of(fittingMethods.get(0).getType());
            typeCheckResult.setMethod();
            typeCheckResult.setResult(wholeResult.get());
          } else {
            typeCheckResult.reset();
            logError("0xA0240", expr.get_SourcePositionStart());
          }
        }
      }
    }
    else {
      //inner type has no result --> try to resolve a type
      String toResolve = printer.prettyprint(expr);
      Optional<TypeSymbol> typeSymbolOpt = getScope(expr.getEnclosingScope())
        .resolveType(toResolve);
      if (typeSymbolOpt.isPresent()) {
        TypeSymbol typeSymbol = typeSymbolOpt.get();
        SymTypeExpression type = SymTypeExpressionFactory
          .createTypeExpression(typeSymbol);
        typeCheckResult.setType();
        typeCheckResult.setResult(type);
      }
      else {
        //the inner type has no result and there is no type found
        typeCheckResult.reset();
        Log.info("package suspected", "DeriveSymTypeOfCommonExpressions");
      }
    }
  }

  protected List<VariableSymbol> filterStaticFieldSymbols(List<VariableSymbol> fieldSymbols) {
    return fieldSymbols.stream().filter(f -> f instanceof FieldSymbol)
      .filter(f -> ((FieldSymbol) f).isIsStatic()).collect(Collectors.toList());
  }

  protected boolean checkIfCorrect(List<VariableSymbol> fieldSymbols) {
    if (fieldSymbols.size() == 0) {
      return false;
    }
    TypeSymbol type = fieldSymbols.get(0).getType().getTypeInfo();
    if (type instanceof TypeSymbolSurrogate) {
      type = ((TypeSymbolSurrogate) type).lazyLoadDelegate();
    }
    for (VariableSymbol field : fieldSymbols) {
      TypeSymbol typeOfField = field.getType().getTypeInfo();
      if (typeOfField instanceof TypeSymbolSurrogate) {
        typeOfField = ((TypeSymbolSurrogate) typeOfField).lazyLoadDelegate();
      }
      if (!type.equals(typeOfField)) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected SymTypeExpression calculateConditionalExpressionType( SymTypeExpression conditionResult,
      SymTypeExpression trueResult,
      SymTypeExpression falseResult) {
    SymTypeExpression wholeResult = SymTypeExpressionFactory.createObscureType();
    //condition has to be boolean
    if (isBoolean(conditionResult)) {
      //check if "then" and "else" are either from the same type or are in sub-supertype relation
      if (compatible(trueResult, falseResult)) {
        wholeResult = trueResult;
      }
      else if (compatible(falseResult, trueResult)) {
        wholeResult = falseResult;
      }
      else {
        // first argument can be null since it should not be relevant to the type calculation
        wholeResult = getBinaryNumericPromotion(trueResult, falseResult);
      }
    }
    return wholeResult;
  }

  @Override
  protected SymTypeExpression calculateTypeLogical(ASTInfixExpression expr, SymTypeExpression rightResult, SymTypeExpression leftResult) {
    //Option one: they are both numeric types
    if (isNumericType(leftResult) && isNumericType(rightResult)
      || isBoolean(leftResult) && isBoolean(rightResult)) {
      return SymTypeExpressionFactory.createPrimitive("boolean");
    }
    //Option two: none of them is a primitive type and they are either the same type or in a super/sub type relation
    if (!leftResult.isPrimitive() && !rightResult.isPrimitive() &&
      (compatible(leftResult, rightResult) || compatible(rightResult, leftResult))
    ) {
      return SymTypeExpressionFactory.createPrimitive("boolean");
    }
    //should never happen, no valid result, error will be handled in traverse
    return SymTypeExpressionFactory.createObscureType();
  }

  protected List<FunctionSymbol> getFittingMethods
    (List<FunctionSymbol> methodlist, ASTCallExpression expr) {
    List<FunctionSymbol> fittingMethods = new ArrayList<>();
    for (FunctionSymbol method : methodlist) {
      //for every method found check if the arguments are correct
      if (expr.getArguments().getExpressionList().size() == method.getParameterList().size()) {
        boolean success = true;
        for (int i = 0; i < method.getParameterList().size(); i++) {
          expr.getArguments().getExpression(i).accept(getTraverser());
          //test if every single argument is correct
          if (!method.getParameterList().get(i).getType()
            .deepEquals(typeCheckResult.getResult()) &&
            !compatible(method.getParameterList().get(i).getType(),
              typeCheckResult.getResult())) {
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
