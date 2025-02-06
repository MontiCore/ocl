// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.ocl.types3.OCLCollectionSymTypeRelations;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.util.SymTypeLubCalculator;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class OCLSymTypeLubCalculator extends SymTypeLubCalculator {

  /** handles the OCL Collection types */
  @Override
  public Optional<SymTypeExpression> leastUpperBound(Collection<SymTypeExpression> types) {
    Optional<SymTypeExpression> lub;
    if (types.stream().allMatch(OCLCollectionSymTypeRelations::isOCLCollection)) {
      Collection<SymTypeExpression> elementTypes =
          types.stream()
              .map(OCLCollectionSymTypeRelations::getCollectionElementType)
              .collect(Collectors.toSet());
      // lub of element types
      Optional<SymTypeExpression> elementLub = leastUpperBound(elementTypes);
      if (elementLub.isEmpty()) {
        lub = Optional.empty();
      }
      // search for correct collection type
      else if (types.stream().allMatch(OCLCollectionSymTypeRelations::isList)) {
        lub = Optional.of(OCLCollectionSymTypeFactory.createList(elementLub.get()));
      } else if (types.stream().allMatch(OCLCollectionSymTypeRelations::isSet)) {
        lub = Optional.of(OCLCollectionSymTypeFactory.createSet(elementLub.get()));
      } else {
        lub = Optional.of(OCLCollectionSymTypeFactory.createOCLCollection(elementLub.get()));
      }
    } else {
      lub = super.leastUpperBound(types);
    }
    return lub;
  }
}
