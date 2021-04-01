// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types.check;

import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;

import java.util.Optional;

public class DeriveSymTypeOfExpression
  extends de.monticore.types.check.DeriveSymTypeOfExpression {

  @Override protected Optional<SymTypeExpression> calculateNameExpression(ASTNameExpression expr) {
    Optional<VariableSymbol> optVar = getScope(expr.getEnclosingScope())
      .resolveVariable(expr.getName());
    Optional<TypeVarSymbol> optTypeVar = getScope(expr.getEnclosingScope())
      .resolveTypeVar(expr.getName());
    Optional<TypeSymbol> optType = getScope(expr.getEnclosingScope()).resolveType(expr.getName());
    if (expr.getName().equals("null")) {
      SymTypeExpression res = SymTypeExpressionFactory.createTypeOfNull();
      return Optional.of(res);
    }
    else if (optVar.isPresent()) {
      //no method here, test variable first
      // durch AST-Umbau kann ASTNameExpression keine Methode sein
      VariableSymbol var = optVar.get();
      SymTypeExpression res = var.getType().deepClone();
      typeCheckResult.setField();
      return Optional.of(res);
    }
    else if (optTypeVar.isPresent()) {
      TypeVarSymbol typeVar = optTypeVar.get();
      SymTypeExpression res = SymTypeExpressionFactory.createTypeVariable(typeVar);
      typeCheckResult.setType();
      return Optional.of(res);
    }
    else if (optType.isPresent()) {
      //no variable found, test if name is type
      TypeSymbol type = optType.get();
      SymTypeExpression res = SymTypeExpressionFactory.createTypeExpression(type);
      typeCheckResult.setType();
      return Optional.of(res);
    }
    return Optional.empty();
  }
}
