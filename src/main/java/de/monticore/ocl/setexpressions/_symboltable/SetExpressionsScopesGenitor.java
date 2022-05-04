// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.setexpressions._symboltable;

import de.monticore.ocl.types.check.OCLDeriver;
import de.se_rwth.commons.logging.Log;

public class SetExpressionsScopesGenitor extends SetExpressionsScopesGenitorTOP {
  protected OCLDeriver deriver;

  public SetExpressionsScopesGenitor(){
    super();
  }

  public void setDeriver(OCLDeriver deriver) {
    if (deriver != null) {
      this.deriver = deriver;
    }
    else {
      Log.error("0xA3201 The typesVisitor has to be set");
    }
  }




}
