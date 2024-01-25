/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.util;

import de.monticore.ocl2smt.ocl2smt.expr.ExpressionKind;
import java.util.HashMap;
import java.util.Map;

public class OCLType {
  public static Map<String, OCLType> typeNames = new HashMap<>();
  private final String name;

  private OCLType(String name) {
    this.name = name;
  }

  public static OCLType buildOCLType(String name) {
    if (typeNames.containsKey(name)) {
      return typeNames.get(name);
    }
    OCLType res = new OCLType(name);
    typeNames.put(name, res);
    return res;
  }

  public static OCLType buildOCLType(ExpressionKind kind) {
    switch (kind) {
      case INTEGER:
        return buildOCLType("int");
      case DOUBLE:
        return buildOCLType("double");
      case CHAR:
        return buildOCLType("char");

      default:
        // todo print error
    }
    return null;
  }

  public boolean equals(OCLType obj) {
    return this.name.equals(obj.getName());
  }

  public String getName() {
    return name;
  }

  public boolean isPrimitiv() {
    return TypeConverter.typeMap.containsKey(this);
  }
}
