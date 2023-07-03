// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types.check;

import com.google.common.collect.Lists;
import de.monticore.types.check.*;
import de.se_rwth.commons.logging.Log;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class OCLTypeCheck {
  
  // TODO MSm better variant will be provided by FDr
  protected static final List<String> collections =
      Collections.unmodifiableList(
          Lists.newArrayList(
              "java.util.List",
              "java.util.Set",
              "java.util.Collection",
              "java.util.Map",
              "List",
              "Set",
              "Collection",
              "Map"));
  
  // TODO MSm replace with a better variant when OCLSymTypeCompatibilityCalculator is available
  protected static TypeRelations typeRelations = new TypeRelations();
  
  /**
   * Test whether 2 types are compatible by using TypeCheck class and extending it by checking
   * whether FullQualifiedNames are different.
   *
   * @param left expression that should be assigned a value
   * @param right expression that should be assigned to left
   * @return true iff right is compatible to left
   * @deprecated replace with the future OCLSymTypeCompatibilityCalculator
   */
  @Deprecated
  public static boolean compatible(SymTypeExpression left, SymTypeExpression right) {
    if (!left.isPrimitive() && right.isNullType()) {
      return true;
    }
    // check whether TypeCheck class deems types compatible
    boolean comp = typeRelations.compatible(left, right);

    // check whether last Part of FullQualifiedName is equal
    String leftName = left.print();
    String rightName = right.print();
    String[] leftNameArray = leftName.split("\\.");
    String[] rightNameArray = rightName.split("\\.");
    if (leftNameArray.length > 1) {
      leftName = leftNameArray[leftNameArray.length - 1];
    }
    if (rightNameArray.length > 1) {
      rightName = rightNameArray[rightNameArray.length - 1];
    }
    if (leftName.equals(rightName)) {
      comp = true;
    }

    return comp;
  }
  
  /**
   * @deprecated replace with the future OCLSymTypeCompatibilityCalculator
   */
  @Deprecated
  public static boolean isSubtypeOf(SymTypeExpression subType, SymTypeExpression superType) {
    // Object is superType of all other types
    if (superType.getTypeInfo().getName().equals("Object")) {
      return true;
    }
    
    // Otherwise use default TypeCheck method
    else {
      return typeRelations.isSubtypeOf(subType, superType);
    }
  }
  
  /**
   * @deprecated use the {@link OptionalOperatorsTypeVisitor#unwrapOptional(SymTypeExpression)} instead
   */
  @Deprecated
  public static SymTypeExpression unwrapOptional(SymTypeExpression optional) {
    // check that argument is of Type Optional
    if (!optional.isGenericType() || !optional.getTypeInfo().getName().equals("Optional")) {
      Log.error("function optionalCompatible requires an Optional SymType but was given " +
          optional.print());
      return SymTypeExpressionFactory.createObscureType();
    }
    else if (!((SymTypeOfGenerics) optional).getArgumentList().isEmpty()) {
      // return type of optional
      return ((SymTypeOfGenerics) optional).getArgument(0);
    }
    else {
      return SymTypeExpressionFactory.createObscureType();
    }
  }

  public static SymTypeExpression unwrapSet(SymTypeExpression set) {
    // check that argument is of collection type
    var invalid = collections.stream()
        .noneMatch(c -> set.isGenericType() && set.getTypeInfo().getName().equals(c));
    if (invalid) {
      // not a set, return type of object (maybe change later?)
      if (set.isObjectType()) {
        return set;
      }
      Log.error("function unwrapSet requires a Collection SymType but was given " + set.print());
    }
    
    // get SymType used in Collection
    return ((SymTypeOfGenerics) set).getArgument(0);
  }
}