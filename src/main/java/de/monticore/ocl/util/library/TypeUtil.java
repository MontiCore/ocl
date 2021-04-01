// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util.library;

import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols.OOSymbolsMill;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;

public class TypeUtil {
  protected static SymTypeExpression getIntSymType() {
    return SymTypeExpressionFactory.createTypeConstant("int");
  }

  protected static SymTypeExpression getBoolSymType() {
    return SymTypeExpressionFactory.createTypeConstant("boolean");
  }

  protected static TypeSymbol getListType() {
    return SymTypeExpressionFactory.createTypeConstant("List").getTypeInfo();
  }

  protected static TypeSymbol getSetType() {
    return SymTypeExpressionFactory.createTypeConstant("Set").getTypeInfo();
  }

  protected static TypeSymbol getCollectionType() {
    return SymTypeExpressionFactory.createTypeConstant("Collection").getTypeInfo();
  }

  protected static void addParam(FunctionSymbol function, String paramName,
    SymTypeExpression paramType) {
    VariableSymbol oParam = OOSymbolsMill.variableSymbolBuilder()
      .setName(paramName)
      .setEnclosingScope(function.getSpannedScope())
      .setType(paramType)
      .build();

    //add parameter to method
    function.getSpannedScope().add(oParam);
  }
}
