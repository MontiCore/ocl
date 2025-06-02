// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.ocl.types3.OCLCollectionSymTypeRelations;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.util.NominalSuperTypeCalculator;
import java.util.List;

public class OCLNominalSuperTypeCalculator extends NominalSuperTypeCalculator {

  /** adds OCL Collection specific subtyping, e.g., {@code Set<A> < Set<B> iff A < B} */
  @Override
  public List<SymTypeExpression> getNominalSuperTypes(SymTypeExpression thisType) {
    // note that this calculates the same supertype multiple times (non-issue):
    // given Set<A> with A < B, we calculate Collection<A> and Set<B>.
    // With Collection<A> AND Set<B>, we calculate Collection<B> in the next step
    List<SymTypeExpression> superTypes = super.getNominalSuperTypes(thisType);
    if (OCLCollectionSymTypeRelations.isOCLCollection(thisType)) {
      SymTypeExpression elementType =
          OCLCollectionSymTypeRelations.getCollectionElementType(thisType);
      if (isSupported(elementType)) {
        List<SymTypeExpression> superElementTypes = getNominalSuperTypes(elementType);
        // simply go other all options
        if (OCLCollectionSymTypeRelations.isList(thisType)) {
          superElementTypes.forEach(
              et -> superTypes.add(OCLCollectionSymTypeFactory.createList(et)));
        } else if (OCLCollectionSymTypeRelations.isSet(thisType)) {
          superElementTypes.forEach(
              et -> superTypes.add(OCLCollectionSymTypeFactory.createSet(et)));
        } else {
          superElementTypes.forEach(
              et -> superTypes.add(OCLCollectionSymTypeFactory.createOCLCollection(et)));
        }
      }
    }
    return superTypes;
  }
}
