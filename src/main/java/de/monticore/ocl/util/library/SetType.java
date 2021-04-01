// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util.library;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols.OOSymbolsMill;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfGenerics;
import sun.jvm.hotspot.debugger.cdbg.Sym;

import static de.monticore.ocl.util.library.TypeUtil.*;

/**
 * Adds symbols for OCL/P sets
 */
public class SetType {
  TypeSymbol setSymbol;

  TypeVarSymbol typeVarSymbol;

  public void addSetType() {
    SymTypeOfGenerics superType = SymTypeExpressionFactory
      .createGenerics(getCollectionType(),
        SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    setSymbol = OCLMill.typeSymbolBuilder()
      .setName("Set")
      .setEnclosingScope(OCLMill.globalScope())
      .setSpannedScope(OCLMill.scope())
      .build();
    setSymbol.getSpannedScope().setName("Set");
    typeVarSymbol = OCLMill.typeVarSymbolBuilder().setName("X").build();
    setSymbol.addTypeVarSymbol(typeVarSymbol);

    OCLMill.globalScope().add(setSymbol);
    OCLMill.globalScope().addSubScope(setSymbol.getSpannedScope());
  }

  public void addMethodsAndFields() {
    addFunctionAdd();
    addFunctionAddAll();
    addFunctionContains();
    addFunctionContainsAll();
    addFunctionCount();
    addFieldIsEmpty();
    addFieldSize();
    addFieldAsList();
  }

  /* ============================================================ */
  /* ========================= HELPERS ========================== */
  /* ============================================================ */

  protected FunctionSymbol createMethod(String name) {
    return OCLMill.functionSymbolBuilder()
      .setName(name)
      .setEnclosingScope(setSymbol.getSpannedScope())
      .setSpannedScope(OCLMill.scope())
      .build();
  }

  protected SymTypeExpression getSetOfXSymType() {
    return SymTypeExpressionFactory
      .createGenerics(setSymbol, SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
  }

  protected SymTypeExpression getCollectionOfXSymType() {
    return SymTypeExpressionFactory
      .createGenerics(getCollectionType(), SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
  }

  /* ============================================================ */
  /* ========================== METHODS ========================= */
  /* ============================================================ */

  protected void addFunctionAdd() {
    FunctionSymbol function = createMethod("add");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setReturnType(getSetOfXSymType());
    setSymbol.getSpannedScope().add(function);
  }

  private void addFunctionAddAll() {
    FunctionSymbol function = createMethod("addAll");
    addParam(function, "c", getCollectionOfXSymType());
    function.setReturnType(getSetOfXSymType());
    setSymbol.getSpannedScope().add(function);
  }

  private void addFunctionContains() {
    FunctionSymbol function = createMethod("contains");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setReturnType(getSetOfXSymType());
    setSymbol.getSpannedScope().add(function);
  }

  private void addFunctionCount() {
  }

  private void addFunctionContainsAll() {
  }

  /* ============================================================ */
  /* ========================== FIELDS ========================== */
  /* ============================================================ */

  protected void addFieldIsEmpty() {
    VariableSymbol field = OOSymbolsMill.variableSymbolBuilder()
      .setName("isEmpty")
      .setEnclosingScope(setSymbol.getSpannedScope())
      .setType(getBoolSymType())
      .build();

    setSymbol.getSpannedScope().add(field);
  }

  protected void addFieldSize() {
    VariableSymbol sizeField = OOSymbolsMill.variableSymbolBuilder()
      .setName("size")
      .setEnclosingScope(setSymbol.getSpannedScope())
      .setType(getIntSymType())
      .build();

    setSymbol.getSpannedScope().add(sizeField);
  }

  protected void addFieldAsList() {
    SymTypeExpression returnType = SymTypeExpressionFactory
      .createGenerics(getListType(), SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));

    VariableSymbol field = OOSymbolsMill.variableSymbolBuilder()
      .setName("asList")
      .setEnclosingScope(setSymbol.getSpannedScope())
      .setType(returnType)
      .build();

    setSymbol.getSpannedScope().add(field);
  }

}
