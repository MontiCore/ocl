// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.types3.util.SymTypeBoxingVisitor;
import java.util.LinkedHashMap;
import java.util.Map;

public class OCLSymTypeBoxingVisitor extends SymTypeBoxingVisitor {

  @Override
  public Map<String, String> getGenericBoxMap() {
    Map<String, String> genericBoxMap = new LinkedHashMap<>(super.getGenericBoxMap());
    genericBoxMap.put("Collection", "java.util.Collection");
    return genericBoxMap;
  }
}
