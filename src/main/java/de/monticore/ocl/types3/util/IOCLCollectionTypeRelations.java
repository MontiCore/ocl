// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types.mccollectiontypes.types3.IMCCollectionTypeRelations;

public interface IOCLCollectionTypeRelations
    extends IMCCollectionTypeRelations {

  /**
   * specifically checks for List, Set, and Collection,
   * as specified in Modellierung mit UML chapter 3-3.
   */
  boolean isOCLCollection(SymTypeExpression type);

  /**
   * flattens collection types,
   * s. Modellierung mit UML 3.3.6.
   * If it cannot be flattened, this is id.
   */
  SymTypeOfGenerics flatten(SymTypeOfGenerics toFlatten);

}
