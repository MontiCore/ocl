package de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3;

import com.microsoft.z3.Sort;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.ocl2smt.ocl2smt.expr2smt.ExpressionKind;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;

public class Z3TypeAdapter implements TypeAdapter<Sort> {
  private final String name;

  private final Sort sort;

  private final ExpressionKind kind;

  private final ASTCDType astcdType;

  Z3TypeAdapter(String name, Sort sort, ExpressionKind kind) {
    this.name = name;
    this.sort = sort;
    this.kind = kind;
    this.astcdType = null;
  }

  Z3TypeAdapter(ASTCDType astcdType, Sort sort, ExpressionKind kind) {
    this.astcdType = astcdType;
    this.name = astcdType.getName();
    this.sort = sort;
    this.kind = kind;
  }

  @Override
  public ExpressionKind getKind() {
    return kind;
  }

  @Override
  public boolean isInt() {
    return kind == ExpressionKind.INTEGER;
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public boolean isDouble() {
    return kind == ExpressionKind.DOUBLE;
  }

  @Override
  public boolean isChar() {
    return kind == ExpressionKind.CHAR;
  }

  @Override
  public boolean isString() {
    return kind == ExpressionKind.STRING;
  }

  @Override
  public boolean isSet() {
    return kind == ExpressionKind.SET;
  }

  @Override
  public boolean isBool() {
    return kind == ExpressionKind.BOOL;
  }

  @Override
  public boolean isObject() {
    return kind == ExpressionKind.OO;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Sort getType() {
    return sort;
  }

  @Override
  public boolean isNative() {
    return isBool() || isChar() || isDouble() || isInt() || isString();
  }

  @Override
  public String toString() {
    return name;
  }

  public ASTCDType getCDType() {
    return astcdType;
  }
}
