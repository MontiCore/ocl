// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3;

import de.monticore.ocl.types3.util.IOCLCollectionTypeRelations;
import de.monticore.ocl.types3.util.OCLCollectionTypeRelations;
import de.monticore.ocl.types3.util.OCLNominalSuperTypeCalculator;
import de.monticore.ocl.types3.util.OCLSymTypeBoxingVisitor;
import de.monticore.ocl.types3.util.OCLSymTypeUnboxingVisitor;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types3.util.FunctionRelations;
import de.monticore.types3.util.SymTypeRelations;

public class OCLSymTypeRelations extends SymTypeRelations implements IOCLSymTypeRelations {

  IOCLCollectionTypeRelations oclCollectionTypeRelations;

  public OCLSymTypeRelations() {
    // default values
    super();
    this.compatibilityDelegate = new OCLSymTypeCompatibilityCalculator(this);
    this.boxingVisitor = new OCLSymTypeBoxingVisitor();
    this.unboxingVisitor = new OCLSymTypeUnboxingVisitor();
    this.superTypeCalculator = new OCLNominalSuperTypeCalculator(this);
    this.oclCollectionTypeRelations = new OCLCollectionTypeRelations();
    this.functionRelationsDelegate = new FunctionRelations(this);
  }

  protected IOCLCollectionTypeRelations getOCLCollTypeRel() {
    return oclCollectionTypeRelations;
  }

  @Override
  public boolean isList(SymTypeExpression symTypeExpression) {
    return getOCLCollTypeRel().isList(symTypeExpression);
  }

  @Override
  public boolean isSet(SymTypeExpression symTypeExpression) {
    return getOCLCollTypeRel().isSet(symTypeExpression);
  }

  @Override
  public boolean isOptional(SymTypeExpression symTypeExpression) {
    return getOCLCollTypeRel().isOptional(symTypeExpression);
  }

  @Override
  public boolean isMap(SymTypeExpression symTypeExpression) {
    return getOCLCollTypeRel().isMap(symTypeExpression);
  }

  @Override
  public SymTypeExpression getCollectionElementType(SymTypeExpression symTypeExpression) {
    return getOCLCollTypeRel().getCollectionElementType(symTypeExpression);
  }

  @Override
  public SymTypeExpression getMapKeyType(SymTypeExpression symTypeExpression) {
    return getOCLCollTypeRel().getMapKeyType(symTypeExpression);
  }

  @Override
  public boolean isOCLCollection(SymTypeExpression type) {
    return getOCLCollTypeRel().isOCLCollection(type);
  }

  @Override
  public SymTypeOfGenerics flatten(SymTypeOfGenerics toFlatten) {
    return getOCLCollTypeRel().flatten(toFlatten);
  }
}
