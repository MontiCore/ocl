// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.ocl.types3.OCLCollectionSymTypeRelations;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.generics.bounds.Bound;
import de.monticore.types3.util.SymTypeCompatibilityCalculator;
import java.util.List;

public class OCLSymTypeCompatibilityCalculator extends SymTypeCompatibilityCalculator {

  @Override
  protected List<Bound> objectConstrainSubTypeOf(
      SymTypeExpression subType, SymTypeExpression superType) {
    List<Bound> result;
    result = super.objectConstrainSubTypeOf(subType, superType);
    // additionally, allow inheritance between (OCL) collection types
    // s. Modelling with UML 3.3.7
    if (result.stream().anyMatch(Bound::isUnsatisfiableBound)) {
      if (
      // OCL collections
      OCLCollectionSymTypeRelations.isOCLCollection(subType)
          && OCLCollectionSymTypeRelations.isOCLCollection(superType)
          &&
          // Set is-a Collection
          (!OCLCollectionSymTypeRelations.isSet(superType)
              || OCLCollectionSymTypeRelations.isSet(subType))
          &&
          // List is-a Collection
          (!OCLCollectionSymTypeRelations.isList(superType)
              || OCLCollectionSymTypeRelations.isList(subType))) {
        result =
            internal_constrainSubTypeOfPreNormalized(
                OCLCollectionSymTypeRelations.getCollectionElementType(subType),
                OCLCollectionSymTypeRelations.getCollectionElementType(superType));
      }
    }
    return result;
  }
}
