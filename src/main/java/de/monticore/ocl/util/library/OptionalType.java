// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util.library;

import static de.monticore.ocl.util.library.TypeUtil.getBoolSymType;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.symboltable.modifiers.BasicAccessModifier;
import de.monticore.types.check.SymTypeExpressionFactory;

/** Adds symbols for OCL/P sets */
public class OptionalType {
  protected TypeSymbol optionalSymbol;

  protected TypeVarSymbol typeVarSymbol;

  public void addOptionalType() {
    typeVarSymbol = OCLMill.typeVarSymbolBuilder().setSpannedScope(OCLMill.scope()).setName("X").build();

    optionalSymbol =
        OCLMill.typeSymbolBuilder()
            .setName("Optional")
            .setEnclosingScope(OCLMill.globalScope())
            .setSpannedScope(OCLMill.scope())
            .build();
    optionalSymbol.getSpannedScope().setName("Optional");
    optionalSymbol.addTypeVarSymbol(typeVarSymbol);

    OCLMill.globalScope().add(optionalSymbol);
    OCLMill.globalScope().addSubScope(optionalSymbol.getSpannedScope());
  }

  public void addMethodsAndFields() {
    addFunctionIsPresent();
    addFunctionGet();
    addFunctionIsEmpty();
  }

  protected void addFunctionGet() {
    FunctionSymbol function = createMethod("get");
    function.setType(SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    optionalSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionIsPresent() {
    FunctionSymbol function = createMethod("isPresent");
    function.setType(getBoolSymType());
    optionalSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionIsEmpty() {
    FunctionSymbol function = createMethod("isEmpty");
    function.setType(getBoolSymType());
    optionalSymbol.getSpannedScope().add(function);
  }

  protected FunctionSymbol createMethod(String name) {
    return OCLMill.functionSymbolBuilder()
        .setName(name)
        .setEnclosingScope(optionalSymbol.getSpannedScope())
        .setSpannedScope(OCLMill.scope())
        .setAccessModifier(BasicAccessModifier.PUBLIC)
        .build();
  }
}
