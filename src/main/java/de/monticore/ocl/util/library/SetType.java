// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util.library;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfGenerics;

import static de.monticore.ocl.util.library.TypeUtil.*;

/**
 * Adds symbols for OCL/P sets
 */
public class SetType {
  protected TypeSymbol setSymbol;

  protected TypeVarSymbol typeVarSymbol;

  public void addSetType() {
    typeVarSymbol = OCLMill.typeVarSymbolBuilder().setName("X").build();

    SymTypeOfGenerics superType = SymTypeExpressionFactory
      .createGenerics(getCollectionType(),
        SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));

    setSymbol = OCLMill.typeSymbolBuilder()
      .setName("Set")
      .setEnclosingScope(OCLMill.globalScope())
      .setSpannedScope(OCLMill.scope())
      .addSuperTypes(superType)
      .build();
    setSymbol.getSpannedScope().setName("Set");
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
    addFunctionIsEmpty();
    addFunctionRemove();
    addFunctionRemoveAll();
    addFunctionRetainAll();
    addFunctionSymmetricDifference();
    addFunctionSize();
    addFunctionFlatten();
    addFunctionAsList();
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

  protected SymTypeExpression getListOfXSymType() {
    return SymTypeExpressionFactory
      .createGenerics(getListType(), SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
  }

  /* ============================================================ */
  /* ========================== METHODS ========================= */
  /* ============================================================ */

  protected void addFunctionAdd() {
    FunctionSymbol function = createMethod("add");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getSetOfXSymType());
    setSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionAddAll() {
    FunctionSymbol function = createMethod("addAll");
    addParam(function, "c", getCollectionOfXSymType());
    function.setType(getSetOfXSymType());
    setSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionContains() {
    FunctionSymbol function = createMethod("contains");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getBoolSymType());
    setSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionContainsAll() {
    FunctionSymbol function = createMethod("containsAll");
    addParam(function, "c", getCollectionOfXSymType());
    function.setType(getBoolSymType());
    setSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionCount() {
    FunctionSymbol function = createMethod("count");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getIntSymType());
    setSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionIsEmpty() {
    FunctionSymbol function = createMethod("isEmpty");
    function.setType(getBoolSymType());
    setSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionRemove() {
    FunctionSymbol function = createMethod("remove");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getSetOfXSymType());
    setSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionRemoveAll() {
    FunctionSymbol function = createMethod("removeAll");
    addParam(function, "c", getCollectionOfXSymType());
    function.setType(getSetOfXSymType());
    setSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionRetainAll() {
    FunctionSymbol function = createMethod("retainAll");
    addParam(function, "c", getCollectionOfXSymType());
    function.setType(getSetOfXSymType());
    setSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionSymmetricDifference() {
    FunctionSymbol function = createMethod("symmetricDifference");
    addParam(function, "s", getSetOfXSymType());
    function.setType(getSetOfXSymType());
    setSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionSize() {
    FunctionSymbol function = createMethod("size");
    function.setType(getIntSymType());
    setSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionFlatten() {
    FunctionSymbol function = createMethod("flatten");
    function.setType(SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    setSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionAsList(){
    FunctionSymbol function = createMethod("asList");
    function.setType(getListOfXSymType());
    setSymbol.getSpannedScope().add(function);
  }
}
