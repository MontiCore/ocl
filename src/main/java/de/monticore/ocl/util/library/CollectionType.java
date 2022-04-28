// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util.library;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;

import static de.monticore.ocl.util.library.TypeUtil.*;

/**
 * Adds symbols for OCL/P collections
 */
public class CollectionType {
  TypeSymbol collectionSymbol;

  TypeVarSymbol typeVarSymbol;

  public void addCollectionType() {
    collectionSymbol = OCLMill.typeSymbolBuilder()
      .setName("Collection")
      .setEnclosingScope(OCLMill.globalScope())
      .setSpannedScope(OCLMill.scope())
      .build();
    collectionSymbol.getSpannedScope().setName("Collection");
    typeVarSymbol = OCLMill.typeVarSymbolBuilder().setName("X").build();
    collectionSymbol.addTypeVarSymbol(typeVarSymbol);

    OCLMill.globalScope().add(collectionSymbol);
    OCLMill.globalScope().addSubScope(collectionSymbol.getSpannedScope());
  }

  public void addMethodsAndFields() {
    addFunctionAdd();
    addFunctionAddAll();
    addFunctionContains();
    addFunctionContainsAll();
    addFunctionIsEmpty();
    addFunctionCount();
    addFunctionRemove();
    addFunctionRemoveAll();
    addFunctionRetainAll();
    addFunctionSize();
    //TODO: flatten
    addFunctionAsSet();
    addFunctionAsList();
  }

  /* ============================================================ */
  /* ========================= HELPERS ========================== */
  /* ============================================================ */

  protected FunctionSymbol createMethod(String name) {
    return OCLMill.functionSymbolBuilder()
      .setName(name)
      .setEnclosingScope(collectionSymbol.getSpannedScope())
      .setSpannedScope(OCLMill.scope())
      .build();
  }

  protected SymTypeExpression getCollectionOfXSymType() {
    return SymTypeExpressionFactory
      .createGenerics(collectionSymbol, SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
  }

  /* ============================================================ */
  /* ========================== METHODS ========================= */
  /* ============================================================ */

  protected void addFunctionAdd() {
    FunctionSymbol function = createMethod("add");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getCollectionOfXSymType());
    collectionSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionAddAll() {
    FunctionSymbol function = createMethod("addAll");
    addParam(function, "c", getCollectionOfXSymType());
    function.setType(getCollectionOfXSymType());
    collectionSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionContains() {
    FunctionSymbol function = createMethod("contains");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getBoolSymType());
    collectionSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionContainsAll() {
    FunctionSymbol function = createMethod("containsAll");
    addParam(function, "c", getCollectionOfXSymType());
    function.setType(getBoolSymType());
    collectionSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionIsEmpty() {
    FunctionSymbol function = createMethod("isEmpty");
    function.setType(getBoolSymType());
    collectionSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionCount() {
    FunctionSymbol function = createMethod("count");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getIntSymType());
    collectionSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionRemove() {
    FunctionSymbol function = createMethod("remove");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(getCollectionOfXSymType());
    collectionSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionRemoveAll() {
    FunctionSymbol function = createMethod("removeAll");
    addParam(function, "c", getCollectionOfXSymType());
    function.setType(getCollectionOfXSymType());
    collectionSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionRetainAll() {
    FunctionSymbol function = createMethod("retainAll");
    addParam(function, "c", getCollectionOfXSymType());
    function.setType(getCollectionOfXSymType());
    collectionSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionSize() {
    FunctionSymbol function = createMethod("size");
    function.setType(getIntSymType());
    collectionSymbol.getSpannedScope().add(function);
  }

  //TODO: flatten

  protected void addFunctionAsSet() {
    FunctionSymbol function = createMethod("asSet");
    SymTypeExpression returnType = SymTypeExpressionFactory
      .createGenerics(getSetType(), SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(returnType);
    collectionSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionAsList() {
    FunctionSymbol function = createMethod("asList");
    SymTypeExpression returnType = SymTypeExpressionFactory
      .createGenerics(getListType(), SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setType(returnType);
    collectionSymbol.getSpannedScope().add(function);
  }
}
