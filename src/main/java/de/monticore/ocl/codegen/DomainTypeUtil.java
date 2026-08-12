package de.monticore.ocl.codegen;

import de.monticore.types.check.SymTypeExpression;

import java.util.ArrayList;
import java.util.List;

public class DomainTypeUtil {
  
  private static DomainTypeUtil instance;
  
  private List<String> domainModels = new ArrayList<>();
  
  private DomainTypeUtil() {}
  
  public static DomainTypeUtil getInstance() {
    if (instance == null) {
      instance = new DomainTypeUtil();
    }
    return instance;
  }
  
  public void init(List<String> domainModels) {
    this.domainModels = domainModels;
  }
  
  public boolean isDomainType(SymTypeExpression symTypeExpression) {
    if (symTypeExpression == null) {
      return false;
    }
    
    boolean isDomain = false;
    if (symTypeExpression.isObjectType()) {
      String fullName = symTypeExpression.asObjectType().printFullName();
      for (String model : domainModels) {
        if (fullName.startsWith(model)) {
          isDomain = true;
          break;
        }
      }
    }
    
    return isDomain || (symTypeExpression.isGenericType() && isDomainType(
        symTypeExpression.asGenericType().getArgument(0)));
  }
}
