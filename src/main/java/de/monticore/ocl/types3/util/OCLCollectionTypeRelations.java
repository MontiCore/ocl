// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsGlobalScope;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types.mccollectiontypes.types3.util.MCCollectionSymTypeFactory;
import de.monticore.types.mccollectiontypes.types3.util.MCCollectionTypeRelations;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

/**
 * adds support for Collection
 */
public class OCLCollectionTypeRelations extends MCCollectionTypeRelations
implements IOCLCollectionTypeRelations{

  protected static final String LOG_NAME = "OCLCollectionTypeRelations";

  /**
   * specifically checks for List, Set, and Collection,
   * as specified in Modellierung mit UML chapter 3-3.
   */
  public boolean isOCLCollection(SymTypeExpression type) {
    return isSet(type) || isList(type) || isOCLCollectionNoSubType(type);
  }

  public boolean isCollection(SymTypeExpression type) {
    return isList(type) ||
        isSet(type) ||
        isOptional(type) ||
        isMap(type) ||
        isOCLCollectionNoSubType(type);
  }

  /**
   * flattens collection types,
   * s. Modellierung mit UML 3.3.6.
   * If it cannot be flattened, this is id.
   */
  public SymTypeOfGenerics flatten(SymTypeOfGenerics toFlatten) {
    SymTypeOfGenerics flattened;
    if (isOCLCollection(toFlatten) &&
        isOCLCollection(getCollectionElementType(toFlatten))) {
      SymTypeOfGenerics innerCollectionType =
          (SymTypeOfGenerics) getCollectionElementType(toFlatten);
      SymTypeExpression innerElementType =
          getCollectionElementType(innerCollectionType);
      // List > Collection > Set
      if (isList(toFlatten) || isList(innerCollectionType)) {
        flattened = MCCollectionSymTypeFactory.createList(innerElementType);
      }
      else if (isOCLCollectionNoSubType(toFlatten) ||
          isOCLCollectionNoSubType(innerCollectionType)) {
        flattened = createCollection(innerElementType);
      }
      else if (isSet(toFlatten) && isSet(innerCollectionType)) {
        flattened = MCCollectionSymTypeFactory.createSet(innerElementType);
      }
      else {
        Log.error("0x0C1FC internal error:"
            + "encountered unexpected collection type");
        flattened = toFlatten;
      }
    }
    // if we cannot flatten, this is id
    else {
      Log.info("called flatten on type " + toFlatten.printFullName()
          + " which cannot be flattenend", LOG_NAME);
      flattened = toFlatten;
    }
    return flattened;
  }

  // Helper

  protected SymTypeOfGenerics createCollection(SymTypeExpression elementType) {
    IBasicSymbolsGlobalScope gs = BasicSymbolsMill.globalScope();
    Optional<TypeSymbol> typeSymbolOpt = gs.resolveType("Collection")
        .or(() -> gs.resolveType("java.util.Collection"));
    if (typeSymbolOpt.isPresent()) {
      return SymTypeExpressionFactory.createGenerics(
          typeSymbolOpt.get(), elementType);
    }
    else {
      Log.error("0xFD327 could not find symbol \"Collection\" "
          + "to create type Collection<" + elementType.printFullName() + ">");
      return null;
    }
  }

  protected boolean isOCLCollectionNoSubType(SymTypeExpression type) {
    return isSpecificCollection(type, "Collection", "java.util.Collection", 1);
  }

}
