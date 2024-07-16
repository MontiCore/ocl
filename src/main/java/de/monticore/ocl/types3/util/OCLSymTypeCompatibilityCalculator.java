// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.ocl.types3.OCLSymTypeRelations;
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
      OCLSymTypeRelations.isOCLCollection(subType)
          && OCLSymTypeRelations.isOCLCollection(superType)
          &&
          // Set is-a Collection
          (!OCLSymTypeRelations.isSet(superType) || OCLSymTypeRelations.isSet(subType))
          &&
          // List is-a Collection
          (!OCLSymTypeRelations.isList(superType) || OCLSymTypeRelations.isList(subType))) {
        result =
            internal_constrainSubTypeOfPreNormalized(
                OCLSymTypeRelations.getCollectionElementType(subType),
                OCLSymTypeRelations.getCollectionElementType(superType));
      }
    }
    return result;
  }
}
