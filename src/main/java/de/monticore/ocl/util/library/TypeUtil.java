// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util.library;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

public class TypeUtil {
  protected static SymTypeExpression getIntSymType() {
    Optional<TypeSymbol> intSymbol = OCLMill.globalScope().resolveType("int");
    if (!intSymbol.isPresent()) {
      Log.error("0xOCL400 int is not defined as type");
    }
    return SymTypeExpressionFactory.createTypeExpression(intSymbol.get());
  }

  protected static SymTypeExpression getBoolSymType() {
    Optional<TypeSymbol> boolSymbol = OCLMill.globalScope().resolveType("boolean");
    if (!boolSymbol.isPresent()) {
      Log.error("0xOCL401 boolean is not defined as type");
    }
    return SymTypeExpressionFactory.createTypeExpression(boolSymbol.get());
  }

  protected static TypeSymbol getListType() {
    Optional<TypeSymbol> listSymbol = OCLMill.globalScope().resolveType("List");
    if (!listSymbol.isPresent()) {
      Log.error("0xOCL402 List is not defined as type");
    }
    return listSymbol.get();
  }

  protected static TypeSymbol getSetType() {
    Optional<TypeSymbol> setSymbol = OCLMill.globalScope().resolveType("Set");
    if (!setSymbol.isPresent()) {
      Log.error("0xOCL402 Set is not defined as type");
    }
    return setSymbol.get();
  }
}
