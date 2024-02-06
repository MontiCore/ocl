package de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3;

import com.microsoft.z3.Sort;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.ocl2smt.ocl2smt.expr2smt.ExprKind;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;

public class Z3TypeAdapter implements TypeAdapter<Sort> {
  private final String name;
  private final Sort sort;
  private final ExprKind kind;
  private final ASTCDType astcdType;

  Z3TypeAdapter(String name, Sort sort, ExprKind kind) {
    this.name = name;
    this.sort = sort;
    this.kind = kind;
    this.astcdType = null;
  }

  Z3TypeAdapter(ASTCDType astcdType, Sort sort, ExprKind kind) {
    this.astcdType = astcdType;
    this.name = astcdType.getName();
    this.sort = sort;
    this.kind = kind;
  }

  @Override
  public ExprKind getKind() {
    return kind;
  }

  @Override
  public boolean isInt() {
    return kind == ExprKind.INTEGER;
  }

  @Override
  public boolean isDouble() {
    return kind == ExprKind.DOUBLE;
  }

  @Override
  public boolean isChar() {
    return kind == ExprKind.CHAR;
  }

  @Override
  public boolean isString() {
    return kind == ExprKind.STRING;
  }

  @Override
  public boolean isSet() {
    return kind == ExprKind.SET;
  }

  @Override
  public boolean isBool() {
    return kind == ExprKind.BOOL;
  }

  @Override
  public boolean isObject() {
    return kind == ExprKind.OO;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Sort getSort() {
    return sort;
  }

  @Override
  public boolean isNative() {
    return isBool() || isChar() || isDouble() || isInt() || isString();
  }

  @Override
  public boolean isOptional() {
    return kind == ExprKind.OPTIONAL;
  }

  @Override
  public String toString() {
    return name;
  }

  public ASTCDType getCDType() {
    return astcdType;
  }
}
