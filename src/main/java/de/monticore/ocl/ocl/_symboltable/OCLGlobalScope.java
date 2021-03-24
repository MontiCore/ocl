// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;

public class OCLGlobalScope extends OCLGlobalScopeTOP {
  @Override public OCLGlobalScope getRealThis() {
    return this;
  }

  @Override
  public void init() {
    super.init();
    add(OCLMill.typeSymbolBuilder()
      .setName(BasicSymbolsMill.NULL)
      .setEnclosingScope(this)
      .setFullName(BasicSymbolsMill.NULL)
      .build());
  }

}
