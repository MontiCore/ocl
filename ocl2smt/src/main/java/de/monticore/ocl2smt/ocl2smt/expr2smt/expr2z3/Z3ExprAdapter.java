package de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3;

import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;

public class Z3ExprAdapter implements ExprAdapter<Expr<?>, Sort> {
  private final Expr<?> expr;
  protected final Z3TypeAdapter type;

  @Override
  public Expr<?> getExpr() {
    return expr;
  }

  @Override
  public Z3TypeAdapter getExprType() {
    return type;
  }

  public Z3ExprAdapter(Expr<?> expr, Z3TypeAdapter type) {
    this.expr = expr;
    this.type = type;
  }

  public boolean isStringExpr() {
    return type.isString();
  }

  public boolean isObjExpr() {
    return type.isObject();
  }

  public boolean isBoolExpr() {
    return type.isBool();
  }

  public boolean isSetExpr() {
    return type.isSet();
  }

  public boolean isIntExpr() {
    return type.isInt();
  }

  public boolean isDoubleExpr() {
    return type.isChar();
  }

  @Override
  public String toString() {
    return expr.toString();
  }
}
