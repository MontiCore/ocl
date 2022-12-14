// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util.library;

import static de.monticore.ocl.util.library.TypeUtil.*;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfGenerics;

/** Adds symbols for OCL/P lists */
public class ListType {
  protected TypeSymbol listSymbol;

  protected TypeVarSymbol typeVarSymbol;

  public void addListType() {
    typeVarSymbol = OCLMill.typeVarSymbolBuilder().setName("X").build();

    SymTypeOfGenerics superType =
        SymTypeExpressionFactory.createGenerics(
            getCollectionType(), SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));

    listSymbol =
        OCLMill.typeSymbolBuilder()
            .setName("List")
            .setEnclosingScope(OCLMill.globalScope())
            .setSpannedScope(OCLMill.scope())
            .addSuperTypes(superType)
            .build();
    listSymbol.getSpannedScope().setName("List");
    listSymbol.addTypeVarSymbol(typeVarSymbol);

    OCLMill.globalScope().add(listSymbol);
    OCLMill.globalScope().addSubScope(listSymbol.getSpannedScope());
  }

  public void addMethodsAndFields() {
    addFunctionAdd();
    addFunctionAdd2();
    addFunctionPrepend();
    addFunctionAddAll();
    addFunctionAddAll2();
    addFunctionContains();
    addFunctionContainsAll();
    addFunctionGet();
    addFunctionFirst();
    addFunctionLast();
    addFunctionRest();
    addFunctionIndexOf();
    addFunctionLastIndexOf();
    addFunctionIsEmpty();
    addFunctionCount();
    addFunctionRemove();
    addFunctionRemoveAtIndex();
    addFunctionRemoveAll();
    addFunctionRetainAll();
    addFunctionSet();
    addFunctionSize();
    addFunctionSubList();
    addFunctionFlatten();
    addFunctionAsSet();
  }

  /* ============================================================ */
  /* ========================= HELPERS ========================== */
  /* ============================================================ */

  protected FunctionSymbol createMethod(String name) {
    return OCLMill.functionSymbolBuilder()
        .setName(name)
        .setEnclosingScope(listSymbol.getSpannedScope())
        .setSpannedScope(OCLMill.scope())
        .build();
  }

  protected SymTypeExpression getListOfXSymType() {
    return SymTypeExpressionFactory.createGenerics(
        listSymbol, SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
  }

  protected SymTypeExpression getSetOfXSymType() {
    return SymTypeExpressionFactory.createGenerics(
        getSetType(), SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
  }

  protected SymTypeExpression getCollectionOfXSymType() {
    return SymTypeExpressionFactory.createGenerics(
        getCollectionType(), SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
  }

  /* ============================================================ */
  /* ========================== METHODS ========================= */
  /* ============================================================ */

  protected void addFunctionAdd() {
    FunctionSymbol function = createMethod("add");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getListOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionAdd2() {
    FunctionSymbol function = createMethod("add");
    addParam(function, "index", getIntSymType());
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getListOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionPrepend() {
    FunctionSymbol function = createMethod("prepend");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getListOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionAddAll() {
    FunctionSymbol function = createMethod("addAll");
    addParam(function, "c", getCollectionOfXSymType());
    function.setType(getListOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionAddAll2() {
    FunctionSymbol function = createMethod("addAll");
    addParam(function, "index", getIntSymType());
    addParam(function, "c", getCollectionOfXSymType());
    function.setType(getListOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionContains() {
    FunctionSymbol function = createMethod("contains");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getBoolSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionContainsAll() {
    FunctionSymbol function = createMethod("containsAll");
    addParam(function, "c", getCollectionOfXSymType());
    function.setType(getBoolSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionGet() {
    FunctionSymbol function = createMethod("get");
    addParam(function, "index", getIntSymType());
    function.setType(SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionFirst() {
    FunctionSymbol function = createMethod("first");
    function.setType(SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionLast() {
    FunctionSymbol function = createMethod("last");
    function.setType(SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionRest() {
    FunctionSymbol function = createMethod("rest");
    function.setType(getListOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionIndexOf() {
    FunctionSymbol function = createMethod("indexOf");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getIntSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionLastIndexOf() {
    FunctionSymbol function = createMethod("lastIndexOf");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getIntSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionIsEmpty() {
    FunctionSymbol function = createMethod("isEmpty");
    function.setType(getBoolSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionCount() {
    FunctionSymbol function = createMethod("count");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getIntSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionRemove() {
    FunctionSymbol function = createMethod("remove");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getListOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionRemoveAtIndex() {
    FunctionSymbol function = createMethod("removeAtIndex");
    addParam(function, "index", getIntSymType());
    function.setType(getListOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionRemoveAll() {
    FunctionSymbol function = createMethod("removeAll");
    addParam(function, "c", getCollectionOfXSymType());
    function.setType(getListOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionRetainAll() {
    FunctionSymbol function = createMethod("retainAll");
    addParam(function, "c", getCollectionOfXSymType());
    function.setType(getListOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionSet() {
    FunctionSymbol function = createMethod("set");
    addParam(function, "index", getIntSymType());
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getListOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionSize() {
    FunctionSymbol function = createMethod("size");
    function.setType(getIntSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionSubList() {
    FunctionSymbol function = createMethod("subList");
    addParam(function, "fromIndex", getIntSymType());
    addParam(function, "toIndex", getIntSymType());
    function.setType(getListOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionFlatten() {
    // TODO: besser implementieren
    FunctionSymbol function = createMethod("flatten");
    TypeVarSymbol typeVarSymbolY = OCLMill.typeVarSymbolBuilder().setName("Y").build();
    SymTypeExpression returnType =
        SymTypeExpressionFactory.createGenerics(
            listSymbol, SymTypeExpressionFactory.createTypeVariable(typeVarSymbolY));
    function.setType(returnType);
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionAsSet() {
    FunctionSymbol function = createMethod("asSet");
    function.setType(getSetOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }
}
