// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3;

import de.monticore.ocl.types3.util.OCLNominalSuperTypeCalculator;
import de.monticore.ocl.types3.util.OCLSymTypeBoxingVisitor;
import de.monticore.ocl.types3.util.OCLSymTypeCompatibilityCalculator;
import de.monticore.ocl.types3.util.OCLSymTypeLubCalculator;
import de.monticore.ocl.types3.util.OCLSymTypeUnboxingVisitor;
import de.monticore.types3.SymTypeRelations;
import de.monticore.types3.util.BuiltInTypeRelations;
import de.monticore.types3.util.SymTypeNormalizeVisitor;
import de.monticore.types3.util.SymTypeRelationsDefaultDelegatee;

public abstract class OCLSymTypeRelations extends SymTypeRelations {

  public static void init() {
    SymTypeRelations.setDelegate(new OCLSymTypeRelationsDelegatee());
  }

  // selecting the concrete implementations
  protected static class OCLSymTypeRelationsDelegatee extends SymTypeRelationsDefaultDelegatee {
    public OCLSymTypeRelationsDelegatee() {
      compatibilityDelegate = new OCLSymTypeCompatibilityCalculator();
      superTypeCalculator = new OCLNominalSuperTypeCalculator();
      boxingVisitor = new OCLSymTypeBoxingVisitor();
      unboxingVisitor = new OCLSymTypeUnboxingVisitor();
      normalizeVisitor = new SymTypeNormalizeVisitor();
      lubDelegate = new OCLSymTypeLubCalculator();
      builtInRelationsDelegate = new BuiltInTypeRelations();
    }
  }
}
