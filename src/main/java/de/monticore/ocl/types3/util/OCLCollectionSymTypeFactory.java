// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types.mccollectiontypes.types3.util.MCCollectionSymTypeFactory;

public class OCLCollectionSymTypeFactory extends MCCollectionSymTypeFactory {

  public static SymTypeOfGenerics createOCLCollection(SymTypeExpression innerType) {
    return createCollectionType("Collection", "java.util.Collection", innerType);
  }

}
