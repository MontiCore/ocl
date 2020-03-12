/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._symboltable;

import de.monticore.expressions.expressionsbasis._symboltable.ExpressionsBasisSymbolTableCreator;
import de.monticore.expressions.expressionsbasis._symboltable.IExpressionsBasisScope;
import de.monticore.expressions.oclexpressions._symboltable.OCLExpressionsSymbolTableCreator;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;

public class OCLSymbolTableCreatorDelegator
    extends OCLSymbolTableCreatorDelegatorTOP {
  public OCLSymbolTableCreatorDelegator(IOCLGlobalScope globalScope) {
    super(globalScope);
  }

  public void setTypeVisitor(DeriveSymTypeOfOCLCombineExpressions typesCalculator) {
    ((OCLExpressionsSymbolTableCreator) this.getOCLExpressionsVisitor().get()).setTypeVisitor(typesCalculator.getOCLExpressionsVisitorAsDerivedType());
  }

  public IExpressionsBasisScope createScope(boolean shadowing) {
    final IExpressionsBasisScope subScope = ((ExpressionsBasisSymbolTableCreator) this.getExpressionsBasisVisitor().get()).createScope(shadowing);
    return subScope;
  }

  public IOCLScope getGlobalScope() {
    return globalScope;
  }
}
