/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter;

import de.monticore.ocl2smt.ocl2smt.expr2smt.ExpressionKind;

public interface TypeAdapter<T> {

  ExpressionKind getKind();

  boolean isInt();

  boolean isDouble();

  boolean isChar();

  boolean isString();

  boolean isSet();

  boolean isBool();

  boolean isObject();

  String getName();

  T getType();

  boolean isNative();
}
