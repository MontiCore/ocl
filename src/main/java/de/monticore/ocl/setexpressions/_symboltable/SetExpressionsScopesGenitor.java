// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.setexpressions._symboltable;

import de.monticore.types.check.IDerive;
import de.se_rwth.commons.logging.Log;

public class SetExpressionsScopesGenitor extends SetExpressionsScopesGenitorTOP {
  protected IDerive deriver;

  public SetExpressionsScopesGenitor(){
    super();
  }

  public void setDeriver(IDerive deriver) {
    if (deriver != null) {
      this.deriver = deriver;
    }
    else {
      Log.error("0xA3201 The typesVisitor has to be set");
    }
  }




}
