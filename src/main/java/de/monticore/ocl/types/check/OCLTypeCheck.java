package de.monticore.ocl.types.check;

import de.monticore.types.check.*;
import de.se_rwth.commons.logging.Log;

public class OCLTypeCheck extends TypeCheck {
  public OCLTypeCheck(ISynthesize synthesizeSymType, ITypesCalculator iTypesCalculator) {
    super(synthesizeSymType, iTypesCalculator);
  }

  public OCLTypeCheck(ISynthesize synthesizeSymType) {
    super(synthesizeSymType);
  }

  public OCLTypeCheck(ITypesCalculator iTypesCalculator) {
    super(iTypesCalculator);
  }

  /**
   * Test whether 2 types are compatible by using TypeCheck class
   * and extending it by checking whether FullQualifiedNames are different.
   */
  public static boolean compatible(SymTypeExpression left, SymTypeExpression right) {
    //check whether TypeCheck class deems types compatible
    boolean comp = TypeCheck.compatible(left, right);

    //check whether last Part of FullQualifiedName is equal
    String leftName = left.print();
    String rightName = right.print();
    String[] leftNameArray = leftName.split("\\.");
    String[] rightNameArray = rightName.split("\\.");
    if(leftNameArray.length > 1){
      leftName = leftNameArray[leftNameArray.length - 1];
    }
    if(rightNameArray.length > 1){
      rightName = rightNameArray[rightNameArray.length - 1];
    }
    if(leftName.equals(rightName)){
      comp = true;
    }

    return comp;
  }

  public static boolean isSubtypeOf(SymTypeExpression subType, SymTypeExpression superType){
    //Object is superType of all other types
    if(superType.getTypeInfo().getName().equals("Object")){
      return true;
    }

    //Otherwise use default TypeCheck method
    else return TypeCheck.isSubtypeOf(subType, superType);
  }

  public static boolean optionalCompatible(SymTypeExpression optional, SymTypeExpression right){
    //check that first argument is of Type Optional
    if (!optional.isGenericType() || optional.print().equals("Optional")){
      Log.error("function optionalCompatible requires an Optional SymType " +
              "but was given " + optional.print());
      return false;
    }

    //check whether value in optional argument and second argument are compatible
    SymTypeExpression leftUnwrapped = ((SymTypeOfGenerics) optional).getArgument(0);
    return compatible(leftUnwrapped, right);
  }
}
