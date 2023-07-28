// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util.library;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;

/** Adds symbols for OCL/P sets */
public class OptionalType {
  protected TypeSymbol optionalSymbol;

  protected TypeVarSymbol typeVarSymbol;

  public void addOptionalType() {
    typeVarSymbol = OCLMill.typeVarSymbolBuilder().setName("X").build();

    optionalSymbol =
        OCLMill.typeSymbolBuilder()
            .setName("Optional")
            .setEnclosingScope(OCLMill.globalScope())
            .setSpannedScope(OCLMill.scope())
            .build();
    optionalSymbol.getSpannedScope().setName("Optional");
    optionalSymbol.addTypeVarSymbol(typeVarSymbol);

    OCLMill.globalScope().add(optionalSymbol);
    OCLMill.globalScope().addSubScope(optionalSymbol.getSpannedScope());
  }
}
