package de.monticore.expressions.testoclexpressions._symboltable;

import de.monticore.expressions.expressionsbasis._symboltable.ExpressionsBasisSymbolTableCreator;
import de.monticore.expressions.expressionsbasis._symboltable.IExpressionsBasisScope;
import de.monticore.expressions.oclexpressions._symboltable.OCLExpressionsSymbolTableCreator;
import de.monticore.types.check.DeriveSymTypeOfOCLCombineExpressions;

public class TestOCLExpressionsSymbolTableCreatorDelegator
    extends TestOCLExpressionsSymbolTableCreatorDelegatorTOP {
  public TestOCLExpressionsSymbolTableCreatorDelegator(ITestOCLExpressionsGlobalScope globalScope) {
    super(globalScope);
  }

  public void setTypeVisitor(DeriveSymTypeOfOCLCombineExpressions typesCalculator) {
    ((OCLExpressionsSymbolTableCreator) this.getOCLExpressionsVisitor().get()).setTypeVisitor(typesCalculator.getOCLExpressionsVisitorAsDerivedType());
  }

  public IExpressionsBasisScope createScope(boolean shadowing) {
    final IExpressionsBasisScope subScope = ((ExpressionsBasisSymbolTableCreator) this.getExpressionsBasisVisitor().get()).createScope(shadowing);
    return subScope;
  }

  public ITestOCLExpressionsScope getGlobalScope() {
    return globalScope;
  }
}
