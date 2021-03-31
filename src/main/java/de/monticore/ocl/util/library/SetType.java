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
public class SetType {
  TypeSymbol setSymbol;

  TypeVarSymbol typeVarSymbol;

  public void addSetType() {
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
    addFieldIsEmpty();
    addFieldSize();
    addFieldAsList();
  }

  protected void addFunctionAdd() {
    FunctionSymbol prependFunc = OCLMill.functionSymbolBuilder()
      .setName("add")
      .setEnclosingScope(setSymbol.getSpannedScope())
      .setSpannedScope(OCLMill.scope())
      .build();

    //parameter o of type X
    VariableSymbol oParam = OOSymbolsMill.variableSymbolBuilder()
      .setName("o")
      .setEnclosingScope(prependFunc.getSpannedScope())
      //the type of the parameter is X
      .setType(SymTypeExpressionFactory.createTypeVariable(typeVarSymbol))
      .build();

    //add parameter o to method prepend
    prependFunc.getSpannedScope().add(oParam);

    //create and set return type of the method
    SymTypeExpression returnTypePrepend = SymTypeExpressionFactory
      .createGenerics(setSymbol, SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    prependFunc.setReturnType(returnTypePrepend);

    setSymbol.getSpannedScope().add(prependFunc);
  }

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
      //the type of the parameter is X
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
