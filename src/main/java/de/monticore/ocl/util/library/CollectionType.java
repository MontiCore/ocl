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

import static de.monticore.ocl.util.library.TypeUtil.*;

/**
 * Adds symbols for OCL/P sets
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
    addFieldIsEmpty();
    addFieldSize();
    addFieldAsList();
    addFieldAsSet();
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
    function.setReturnType(getCollectionOfXSymType());
    collectionSymbol.getSpannedScope().add(function);
  }

  /* ============================================================ */
  /* ========================== FIELDS ========================== */
  /* ============================================================ */

  protected void addFieldIsEmpty() {
    VariableSymbol field = OOSymbolsMill.variableSymbolBuilder()
      .setName("isEmpty")
      .setEnclosingScope(collectionSymbol.getSpannedScope())
      .setType(getBoolSymType())
      .build();

    collectionSymbol.getSpannedScope().add(field);
  }

  protected void addFieldSize() {
    VariableSymbol sizeField = OOSymbolsMill.variableSymbolBuilder()
      .setName("size")
      .setEnclosingScope(collectionSymbol.getSpannedScope())
      .setType(getIntSymType())
      .build();

    collectionSymbol.getSpannedScope().add(sizeField);
  }

  protected void addFieldAsList() {
    SymTypeExpression returnType = SymTypeExpressionFactory
      .createGenerics(getListType(), SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));

    VariableSymbol field = OOSymbolsMill.variableSymbolBuilder()
      .setName("asList")
      .setEnclosingScope(collectionSymbol.getSpannedScope())
      .setType(returnType)
      .build();

    collectionSymbol.getSpannedScope().add(field);
  }

  protected void addFieldAsSet() {
    SymTypeExpression returnType = SymTypeExpressionFactory
      .createGenerics(getSetType(), SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));

    VariableSymbol field = OOSymbolsMill.variableSymbolBuilder()
      .setName("asSet")
      .setEnclosingScope(collectionSymbol.getSpannedScope())
      .setType(returnType)
      .build();

    collectionSymbol.getSpannedScope().add(field);
  }

}
