// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3;

import de.monticore.ocl.types3.util.IOCLCollectionTypeRelations;
import de.monticore.ocl.types3.util.OCLCollectionTypeRelations;
import de.monticore.ocl.types3.util.OCLNominalSuperTypeCalculator;
import de.monticore.ocl.types3.util.OCLSymTypeBoxingVisitor;
import de.monticore.ocl.types3.util.OCLSymTypeCompatibilityCalculator;
import de.monticore.ocl.types3.util.OCLSymTypeUnboxingVisitor;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types3.SymTypeRelations;

public class OCLSymTypeRelations extends SymTypeRelations {

  protected static IOCLCollectionTypeRelations oclCollectionTypeRelations;

  public static void init() {
    // default values
    SymTypeRelations.init();
    compatibilityDelegate = new OCLSymTypeCompatibilityCalculator();
    boxingVisitor = new OCLSymTypeBoxingVisitor();
    unboxingVisitor = new OCLSymTypeUnboxingVisitor();
    superTypeCalculator = new OCLNominalSuperTypeCalculator();
    oclCollectionTypeRelations = new OCLCollectionTypeRelations();
  }

  public static boolean isSet(SymTypeExpression symTypeExpression) {
    return oclCollectionTypeRelations.isSet(symTypeExpression);
  }

  public static boolean isList(SymTypeExpression symTypeExpression) {
    return oclCollectionTypeRelations.isList(symTypeExpression);
  }

  public static boolean isOptional(SymTypeExpression symTypeExpression) {
    return oclCollectionTypeRelations.isOptional(symTypeExpression);
  }

  public static boolean isMap(SymTypeExpression symTypeExpression) {
    return oclCollectionTypeRelations.isMap(symTypeExpression);
  }

  public static SymTypeExpression getCollectionElementType(SymTypeExpression symTypeExpression) {
    return oclCollectionTypeRelations.getCollectionElementType(symTypeExpression);
  }

  public static SymTypeExpression getMapKeyType(SymTypeExpression symTypeExpression) {
    return oclCollectionTypeRelations.getMapKeyType(symTypeExpression);
  }

  public static boolean isOCLCollection(SymTypeExpression type) {
    return oclCollectionTypeRelations.isOCLCollection(type);
  }

  public static SymTypeOfGenerics flatten(SymTypeOfGenerics toFlatten) {
    return oclCollectionTypeRelations.flatten(toFlatten);
  }
}
