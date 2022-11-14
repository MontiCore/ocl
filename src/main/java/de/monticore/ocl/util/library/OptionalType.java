// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util.library;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfGenerics;

import static de.monticore.ocl.util.library.TypeUtil.addParam;
import static de.monticore.ocl.util.library.TypeUtil.getBoolSymType;
import static de.monticore.ocl.util.library.TypeUtil.getCollectionType;
import static de.monticore.ocl.util.library.TypeUtil.getIntSymType;
import static de.monticore.ocl.util.library.TypeUtil.getListType;

/**
 * Adds symbols for OCL/P sets
 */
public class OptionalType {
  protected TypeSymbol optionalSymbol;

  protected TypeVarSymbol typeVarSymbol;

  public void addOptionalType() {
    typeVarSymbol = OCLMill.typeVarSymbolBuilder().setName("X").build();

    SymTypeOfGenerics superType = SymTypeExpressionFactory
      .createGenerics(getCollectionType(),
        SymTypeExpressionFactory.createTypeVariable(typeVarSymbol));

    optionalSymbol = OCLMill.typeSymbolBuilder()
      .setName("Optional")
      .setEnclosingScope(OCLMill.globalScope())
      .setSpannedScope(OCLMill.scope())
      .addSuperTypes(superType)
      .build();
    optionalSymbol.getSpannedScope().setName("Optional");
    optionalSymbol.addTypeVarSymbol(typeVarSymbol);

    OCLMill.globalScope().add(optionalSymbol);
    OCLMill.globalScope().addSubScope(optionalSymbol.getSpannedScope());
  }

}
