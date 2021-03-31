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
 * Adds symbols for OCL/P lists
 */
public class ListType {
  protected TypeSymbol listSymbol;

  protected TypeVarSymbol typeVarSymbol;

  public void addListType() {
    listSymbol = OCLMill.typeSymbolBuilder()
      .setName("List")
      .setEnclosingScope(OCLMill.globalScope())
      .setSpannedScope(OCLMill.scope())
      .build();
    listSymbol.getSpannedScope().setName("List");
    typeVarSymbol = OCLMill.typeVarSymbolBuilder().setName("X").build();
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

  protected void addFunctionAdd() {
    FunctionSymbol prependFunc = OCLMill.functionSymbolBuilder()
      .setName("add")
      .setEnclosingScope(listSymbol.getSpannedScope())
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
      .createGenerics(listSymbol, SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    prependFunc.setReturnType(returnTypePrepend);

    listSymbol.getSpannedScope().add(prependFunc);
  }

  protected void addFunctionAdd2() {
    FunctionSymbol prependFunc = OCLMill.functionSymbolBuilder()
      .setName("add")
      .setEnclosingScope(listSymbol.getSpannedScope())
      .setSpannedScope(OCLMill.scope())
      .build();

    //parameter o of type X
    VariableSymbol indexParam = OOSymbolsMill.variableSymbolBuilder()
      .setName("index")
      .setEnclosingScope(prependFunc.getSpannedScope())
      //the type of the parameter is X
      .setType(getIntSymType())
      .build();

    //parameter o of type X
    VariableSymbol oParam = OOSymbolsMill.variableSymbolBuilder()
      .setName("o")
      .setEnclosingScope(prependFunc.getSpannedScope())
      //the type of the parameter is X
      .setType(SymTypeExpressionFactory.createTypeVariable(typeVarSymbol))
      .build();

    //add parameters to method prepend
    prependFunc.getSpannedScope().add(indexParam);
    prependFunc.getSpannedScope().add(oParam);

    //create and set return type of the method
    SymTypeExpression returnTypePrepend = SymTypeExpressionFactory
      .createGenerics(listSymbol, SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    prependFunc.setReturnType(returnTypePrepend);

    listSymbol.getSpannedScope().add(prependFunc);
  }

  protected void addFunctionPrepend() {
    FunctionSymbol prependFunc = OCLMill.functionSymbolBuilder()
      .setName("prepend")
      .setEnclosingScope(listSymbol.getSpannedScope())
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
      .createGenerics(listSymbol, SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));
    prependFunc.setReturnType(returnTypePrepend);

    listSymbol.getSpannedScope().add(prependFunc);
  }

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
    SymTypeExpression returnType = SymTypeExpressionFactory
      .createGenerics(listSymbol, SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));

    VariableSymbol field = OOSymbolsMill.variableSymbolBuilder()
      .setName("rest")
      .setEnclosingScope(listSymbol.getSpannedScope())
      .setType(returnType)
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
      //the type of the parameter is X
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
