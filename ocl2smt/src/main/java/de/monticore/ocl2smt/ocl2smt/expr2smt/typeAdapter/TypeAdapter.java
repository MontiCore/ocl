/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter;

import de.monticore.ocl2smt.ocl2smt.expr2smt.ExprKind;
// todo add documentation

public interface TypeAdapter {

  ExprKind getKind();

  String getName();

  boolean isInt();

  boolean isDouble();

  boolean isChar();

  boolean isString();

  boolean isSet();

  boolean isBool();

  boolean isObject();

  boolean isNative();

  boolean isOptional();
}
