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

import static de.monticore.ocl.util.library.TypeUtil.*;

/**
 * Adds symbols for OCL/P lists
 */
public class ListType {
  protected TypeSymbol listSymbol;

  protected TypeVarSymbol typeVarSymbol;

  public void addListType() {
    typeVarSymbol = OCLMill.typeVarSymbolBuilder().setName("X").build();

    SymTypeOfGenerics superType = SymTypeExpressionFactory
      .createGenerics(getCollectionType(),
        SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));

    listSymbol = OCLMill.typeSymbolBuilder()
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
    addFunctionPrepend();
    addFunctionAdd();
    addFunctionAdd2();
    addFieldFirst();
    addFieldLast();
    addFieldRest();
    addFieldIsEmpty();
    addFieldSize();
    addFieldAsSet();
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
    return SymTypeExpressionFactory
      .createGenerics(listSymbol, SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
  }

  /* ============================================================ */
  /* ========================== METHODS ========================= */
  /* ============================================================ */

  protected void addFunctionAdd() {
    FunctionSymbol function = createMethod("add");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setReturnType(getListOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionAdd2() {
    FunctionSymbol function = createMethod("add");
    addParam(function, "index", getIntSymType());
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setReturnType(getListOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }

  protected void addFunctionPrepend() {
    FunctionSymbol function = createMethod("add");
    addParam(function, "o", SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    function.setReturnType(getListOfXSymType());
    listSymbol.getSpannedScope().add(function);
  }

  /* ============================================================ */
  /* ========================== FIELDS ========================== */
  /* ============================================================ */

  protected void addFieldFirst() {
    VariableSymbol field = OOSymbolsMill.variableSymbolBuilder()
      .setName("first")
      .setEnclosingScope(listSymbol.getSpannedScope())
      .setType(SymTypeExpressionFactory.createTypeVariable(typeVarSymbol))
      .build();

    listSymbol.getSpannedScope().add(field);
  }

  protected void addFieldLast() {
    VariableSymbol field = OOSymbolsMill.variableSymbolBuilder()
      .setName("last")
      .setEnclosingScope(listSymbol.getSpannedScope())
      .setType(SymTypeExpressionFactory.createTypeVariable(typeVarSymbol))
      .build();

    listSymbol.getSpannedScope().add(field);
  }

  protected void addFieldRest() {
    VariableSymbol field = OOSymbolsMill.variableSymbolBuilder()
      .setName("rest")
      .setEnclosingScope(listSymbol.getSpannedScope())
      .setType(getListOfXSymType())
      .build();

    listSymbol.getSpannedScope().add(field);
  }

  protected void addFieldIsEmpty() {
    VariableSymbol field = OOSymbolsMill.variableSymbolBuilder()
      .setName("isEmpty")
      .setEnclosingScope(listSymbol.getSpannedScope())
      .setType(getBoolSymType())
      .build();

    listSymbol.getSpannedScope().add(field);
  }

  protected void addFieldSize() {
    VariableSymbol sizeField = OOSymbolsMill.variableSymbolBuilder()
      .setName("size")
      .setEnclosingScope(listSymbol.getSpannedScope())
      .setType(getIntSymType())
      .build();

    listSymbol.getSpannedScope().add(sizeField);
  }

  protected void addFieldAsSet() {
    SymTypeExpression returnType = SymTypeExpressionFactory
      .createGenerics(getSetType(), SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));

    VariableSymbol field = OOSymbolsMill.variableSymbolBuilder()
      .setName("asSet")
      .setEnclosingScope(listSymbol.getSpannedScope())
      .setType(returnType)
      .build();

    listSymbol.getSpannedScope().add(field);
  }

}
