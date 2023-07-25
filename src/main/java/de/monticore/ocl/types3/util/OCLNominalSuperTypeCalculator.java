// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.ocl.types3.IOCLSymTypeRelations;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.util.NominalSuperTypeCalculator;

import java.util.List;

public class OCLNominalSuperTypeCalculator extends NominalSuperTypeCalculator {

  IOCLSymTypeRelations oclSymTypeRelations;

  public OCLNominalSuperTypeCalculator(IOCLSymTypeRelations symTypeRelations) {
    this.oclSymTypeRelations = symTypeRelations;
  }

  protected IOCLSymTypeRelations getOclSymTypeRel() {
    return oclSymTypeRelations;
  }

  /**
   * adds OCL Collection specific subtyping,
   * e.g., Set<A> < Set<B> iff A < B
   */
  @Override
  public List<SymTypeExpression> getNominalSuperTypes(SymTypeExpression thisType) {
    // note that this calculates the same supertype multiple times (non-issue):
    // given Set<A> with A < B, we calculate Collection<A> and Set<B>.
    // With Collection<A> AND Set<B>, we calculate Collection<B> in the next step
    List<SymTypeExpression> superTypes =
        super.getNominalSuperTypes(thisType);
    if (getOclSymTypeRel().isOCLCollection(thisType)) {
      SymTypeExpression elementType =
          getOclSymTypeRel().getCollectionElementType(thisType);
      if (isSupported(elementType)) {
        List<SymTypeExpression> superElementTypes =
            getNominalSuperTypes(elementType);
        // simply go other all options
        if (getOclSymTypeRel().isList(thisType)) {
          superElementTypes.forEach(
              et -> superTypes.add(OCLCollectionSymTypeFactory.createList(et)));
        }
        else if (getOclSymTypeRel().isSet(thisType)) {
          superElementTypes.forEach(et ->
              superTypes.add(OCLCollectionSymTypeFactory.createSet(et)));
        }
        else {
          superElementTypes.forEach(et ->
              superTypes.add(OCLCollectionSymTypeFactory.createOCLCollection(et)));
        }
      }
    }
    return superTypes;
  }

}
