// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util.library;

import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symboltable.modifiers.AccessModifier;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;

/** @deprecated to be replaced with SymTable */
@Deprecated
public class TypeUtil {

  @Deprecated
  protected static SymTypeExpression getIntSymType() {
    return SymTypeExpressionFactory.createPrimitive("int");
  }

  @Deprecated
  protected static SymTypeExpression getLongSymType() {
    return SymTypeExpressionFactory.createPrimitive("long");
  }

  @Deprecated
  protected static SymTypeExpression getBoolSymType() {
    return SymTypeExpressionFactory.createPrimitive("boolean");
  }

  @Deprecated
  protected static TypeSymbol getCollectionType() {
    return SymTypeExpressionFactory.createPrimitive("Collection").getTypeInfo();
  }

  @Deprecated
  protected static TypeSymbol getListType() {
    return SymTypeExpressionFactory.createPrimitive("List").getTypeInfo();
  }

  @Deprecated
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
            .setAccessModifier(AccessModifier.ALL_INCLUSION)
            .build();

    // add parameter to method
    function.getSpannedScope().add(oParam);
  }
}
