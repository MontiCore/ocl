// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.setexpressions._symboltable;

import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.se_rwth.commons.logging.Log;

public class SetExpressionsScopesGenitor extends SetExpressionsScopesGenitorTOP {
  protected DeriveSymTypeOfOCLCombineExpressions typeVisitor;

  public SetExpressionsScopesGenitor(){
    super();
  }

  public void setTypeVisitor(DeriveSymTypeOfOCLCombineExpressions typesCalculator) {
    if (typesCalculator != null) {
      this.typeVisitor = typesCalculator;
    }
    else {
      Log.error("0xA3201 The typesVisitor has to be set");
    }
  }




}
