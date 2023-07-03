// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util.library;

import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;

// TODO MSm deprecate as soon as SymTypeExpressionsFactory has convenience methods for this.
public class TypeUtil {
  protected static SymTypeExpression getIntSymType() {
    return SymTypeExpressionFactory.createPrimitive("int");
  }

  protected static SymTypeExpression getLongSymType() {
    return SymTypeExpressionFactory.createPrimitive("long");
  }

  protected static SymTypeExpression getBoolSymType() {
    return SymTypeExpressionFactory.createPrimitive("boolean");
  }

  protected static TypeSymbol getCollectionType() {
    return SymTypeExpressionFactory.createPrimitive("Collection").getTypeInfo();
  }

  protected static TypeSymbol getListType() {
    return SymTypeExpressionFactory.createPrimitive("List").getTypeInfo();
  }

  protected static TypeSymbol getSetType() {
    return SymTypeExpressionFactory.createPrimitive("Set").getTypeInfo();
  }

  protected static void addParam(
      FunctionSymbol function, String paramName, SymTypeExpression paramType) {
    VariableSymbol oParam =
        BasicSymbolsMill.variableSymbolBuilder()
            .setName(paramName)
            .setEnclosingScope(function.getSpannedScope())
            .setType(paramType)
            .build();

    // add parameter to method
    function.getSpannedScope().add(oParam);
  }
}