// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbolDeSer;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbolDeSer;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbolDeSer;

public class OCLGlobalScope extends OCLGlobalScopeTOP {
  @Override public OCLGlobalScope getRealThis() {
    return this;
  }

  @Override
  public void init() {
    super.init();
    /*
    this.putSymbolDeSer("de.monticore.cdbasis._symboltable.CDTypeSymbol", new TypeSymbolDeSer());
    this.putSymbolDeSer("de.monticore.cd4codebasis._symboltable.CDMethodSignatureSymbol",
      new FunctionSymbolDeSer());
    this.putSymbolDeSer("de.monticore.symbols.oosymbols._symboltable.FieldSymbol",
      new VariableSymbolDeSer());
    */
    add(OCLMill.typeSymbolBuilder()
      .setName(BasicSymbolsMill.NULL)
      .setEnclosingScope(this)
      .setFullName(BasicSymbolsMill.NULL)
      .build());
  }

}
