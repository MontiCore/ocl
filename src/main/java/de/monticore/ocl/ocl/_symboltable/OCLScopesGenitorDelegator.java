/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.ocl._symboltable;

import de.monticore.ocl.oclexpressions._symboltable.OCLExpressionsScopesGenitor;
import de.monticore.ocl.setexpressions._symboltable.SetExpressionsScopesGenitor;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;

public class OCLScopesGenitorDelegator extends OCLScopesGenitorDelegatorTOP {

  public OCLScopesGenitorDelegator(){
    super();
    this.setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());
  }

  public  OCLScopesGenitorDelegator(IOCLGlobalScope globalScope)  {
    super(globalScope);
    this.setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());
  }

  public void setTypeVisitor(DeriveSymTypeOfOCLCombineExpressions typesCalculator){
    OCLScopesGenitor oclHandler = (OCLScopesGenitor) traverser.getOCLHandler().get();
    OCLExpressionsScopesGenitor oclExpressionsHandler = (OCLExpressionsScopesGenitor) traverser.getOCLExpressionsHandler().get();
    SetExpressionsScopesGenitor setExpressionsHandler = (SetExpressionsScopesGenitor) traverser.getSetExpressionsHandler().get();
    oclHandler.setTypeVisitor(typesCalculator);
    oclExpressionsHandler.setTypeVisitor(typesCalculator);
    setExpressionsHandler.setTypeVisitor(typesCalculator);
  }



}
