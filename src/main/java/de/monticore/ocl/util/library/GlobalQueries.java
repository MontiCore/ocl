// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util.library;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._symboltable.IOCLGlobalScope;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;

import static de.monticore.ocl.util.library.TypeUtil.*;

/**
 * Adds symbols for static queries which are available in the OCL/P global scope
 */
public class GlobalQueries {
  
  IOCLGlobalScope globalScope;

  public GlobalQueries() {
    globalScope = OCLMill.globalScope();
  }

  public void addMethodsAndFields() {
    //int methods
    addFunctionSumInt();
    addFunctionMaxInt();
    addFunctionMinInt();
    addFunctionAverageInt();
    addFunctionSortInt();

    //long methods
    //TODO: long methods
    /*addFunctionSumLong();
    addFunctionMaxLong();
    addFunctionMinLong();
    addFunctionAverageLong();
    addFunctionSortLong();*/

    addFunctionEven();
    addFunctionOdd();
  }

  /* ============================================================ */
  /* ========================= HELPERS ========================== */
  /* ============================================================ */

  protected FunctionSymbol createMethod(String name) {
    return OCLMill.functionSymbolBuilder()
            .setName(name)
            .setEnclosingScope(globalScope)
            .setSpannedScope(OCLMill.scope())
            .build();
  }

  protected SymTypeExpression getCollectionOfIntSymType() {
    return SymTypeExpressionFactory.createGenerics(getCollectionType(), getIntSymType());
  }

  protected SymTypeExpression getListOfIntSymType() {
    return SymTypeExpressionFactory.createGenerics(getListType(), getIntSymType());
  }

  protected SymTypeExpression getCollectionOfLongSymType() {
    return SymTypeExpressionFactory.createGenerics(getCollectionType(), getLongSymType());
  }

  protected SymTypeExpression getListOfLongSymType() {
    return SymTypeExpressionFactory.createGenerics(getListType(), getLongSymType());
  }

  /* ============================================================ */
  /* ========================== METHODS ========================= */
  /* ============================================================ */

  protected void addFunctionSumInt() {
    FunctionSymbol function = createMethod("sum");
    addParam(function, "c", getCollectionOfIntSymType());
    function.setType(getIntSymType());
    globalScope.add(function);
  }

  protected void addFunctionMaxInt() {
    FunctionSymbol function = createMethod("max");
    addParam(function, "c", getCollectionOfIntSymType());
    function.setType(getIntSymType());
    globalScope.add(function);
  }

  protected void addFunctionMinInt() {
    FunctionSymbol function = createMethod("min");
    addParam(function, "c", getCollectionOfIntSymType());
    function.setType(getIntSymType());
    globalScope.add(function);
  }

  protected void addFunctionAverageInt() {
    FunctionSymbol function = createMethod("average");
    addParam(function, "c", getCollectionOfIntSymType());
    function.setType(getIntSymType());
    globalScope.add(function);
  }

  protected void addFunctionSortInt() {
    FunctionSymbol function = createMethod("sort");
    addParam(function, "c", getCollectionOfIntSymType());
    function.setType(getListOfIntSymType());
    globalScope.add(function);
  }

  protected void addFunctionSumLong() {
    FunctionSymbol function = createMethod("sum");
    addParam(function, "c", getCollectionOfLongSymType());
    function.setType(getLongSymType());
    globalScope.add(function);
  }

  protected void addFunctionMaxLong() {
    FunctionSymbol function = createMethod("max");
    addParam(function, "c", getCollectionOfLongSymType());
    function.setType(getLongSymType());
    globalScope.add(function);
  }

  protected void addFunctionMinLong() {
    FunctionSymbol function = createMethod("min");
    addParam(function, "c", getCollectionOfLongSymType());
    function.setType(getLongSymType());
    globalScope.add(function);
  }

  protected void addFunctionAverageLong() {
    FunctionSymbol function = createMethod("average");
    addParam(function, "c", getCollectionOfLongSymType());
    function.setType(getLongSymType());
    globalScope.add(function);
  }

  protected void addFunctionSortLong() {
    FunctionSymbol function = createMethod("sort");
    addParam(function, "c", getCollectionOfLongSymType());
    function.setType(getListOfLongSymType());
    globalScope.add(function);
  }

  protected void addFunctionEven() {
    FunctionSymbol function = createMethod("even");
    addParam(function, "number", getIntSymType());
    function.setType(getBoolSymType());
    globalScope.add(function);
  }

  protected void addFunctionOdd() {
    FunctionSymbol function = createMethod("odd");
    addParam(function, "number", getIntSymType());
    function.setType(getBoolSymType());
    globalScope.add(function);
  }
}
