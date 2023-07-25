// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3;

import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.util.SymTypeCompatibilityCalculator;

public class OCLSymTypeCompatibilityCalculator
    extends SymTypeCompatibilityCalculator {

  protected IOCLSymTypeRelations oclSymTypeRelations;

  public OCLSymTypeCompatibilityCalculator(IOCLSymTypeRelations symTypeRelations) {
    super(symTypeRelations);
    // default values
    oclSymTypeRelations = symTypeRelations;
  }

  @Override
  protected IOCLSymTypeRelations getSymTypeRelations() {
    return this.oclSymTypeRelations;
  }

  @Override
  protected boolean objectIsSubTypeOf(
      SymTypeExpression subType,
      SymTypeExpression superType,
      boolean subTypeIsSoft
  ) {
    boolean result;
    if (super.objectIsSubTypeOf(subType, superType, subTypeIsSoft)) {
      result = true;
    }
    // additionally, allow inheritance between (OCL) collection types
    // s. Modelling with UML 3.3.7
    else if (
      // OCL collections
        getSymTypeRelations().isOCLCollection(subType) &&
            getSymTypeRelations().isOCLCollection(superType) &&
            // Set is-a Collection
            (!getSymTypeRelations().isSet(superType) ||
                getSymTypeRelations().isSet(subType)) &&
            // List is-a Collection
            (!getSymTypeRelations().isList(superType) ||
                getSymTypeRelations().isList(subType))
    ) {
      result = internal_isSubTypeOfPreNormalized(
          getSymTypeRelations().getCollectionElementType(subType),
          getSymTypeRelations().getCollectionElementType(superType),
          subTypeIsSoft);
    }
    // extension point
    else {
      result = false;
    }
    return result;
  }

}
