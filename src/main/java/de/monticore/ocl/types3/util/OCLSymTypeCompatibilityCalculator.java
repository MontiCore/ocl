// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.util.SymTypeCompatibilityCalculator;

public class OCLSymTypeCompatibilityCalculator extends SymTypeCompatibilityCalculator {

  @Override
  protected boolean objectIsSubTypeOf(
      SymTypeExpression subType, SymTypeExpression superType, boolean subTypeIsSoft) {
    boolean result;
    if (super.objectIsSubTypeOf(subType, superType, subTypeIsSoft)) {
      result = true;
    }
    // additionally, allow inheritance between (OCL) collection types
    // s. Modelling with UML 3.3.7
    else if (
    // OCL collections
    OCLSymTypeRelations.isOCLCollection(subType)
        && OCLSymTypeRelations.isOCLCollection(superType)
        &&
        // Set is-a Collection
        (!OCLSymTypeRelations.isSet(superType) || OCLSymTypeRelations.isSet(subType))
        &&
        // List is-a Collection
        (!OCLSymTypeRelations.isList(superType) || OCLSymTypeRelations.isList(subType))) {
      result =
          internal_isSubTypeOfPreNormalized(
              OCLSymTypeRelations.getCollectionElementType(subType),
              OCLSymTypeRelations.getCollectionElementType(superType),
              subTypeIsSoft);
    }
    // extension point
    else {
      result = false;
    }
    return result;
  }
}
