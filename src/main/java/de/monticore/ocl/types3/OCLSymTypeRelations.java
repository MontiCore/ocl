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
import de.monticore.types.mccollectiontypes.types3.MCCollectionSymTypeRelations;

public class OCLSymTypeRelations extends MCCollectionSymTypeRelations {

  protected static IOCLCollectionTypeRelations oclCollectionTypeRelations;

  public static void init() {
    // default values
    MCCollectionSymTypeRelations.init();
    compatibilityDelegate = new OCLSymTypeCompatibilityCalculator();
    boxingVisitor = new OCLSymTypeBoxingVisitor();
    unboxingVisitor = new OCLSymTypeUnboxingVisitor();
    superTypeCalculator = new OCLNominalSuperTypeCalculator();
    OCLCollectionTypeRelations oclCTR = new OCLCollectionTypeRelations();
    oclCollectionTypeRelations = oclCTR;
    mcCollectionTypeRelations = oclCTR;
  }

  public static boolean isOCLCollection(SymTypeExpression type) {
    return oclCollectionTypeRelations.isOCLCollection(type);
  }

  public static SymTypeOfGenerics flatten(SymTypeOfGenerics toFlatten) {
    return oclCollectionTypeRelations.flatten(toFlatten);
  }
}
