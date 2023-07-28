// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.types3.util.SymTypeUnboxingVisitor;
import java.util.HashMap;
import java.util.Map;

public class OCLSymTypeUnboxingVisitor extends SymTypeUnboxingVisitor {

  @Override
  public Map<String, String> getGenericUnboxMap() {
    Map<String, String> genericBoxMap = new HashMap<>(super.getGenericUnboxMap());
    genericBoxMap.put("java.util.Collection", "Collection");
    return genericBoxMap;
  }
}
