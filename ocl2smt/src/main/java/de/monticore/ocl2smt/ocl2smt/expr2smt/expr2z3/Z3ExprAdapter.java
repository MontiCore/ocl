package de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3;

import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import java.util.function.Function;

public class Z3ExprAdapter implements ExprAdapter<Expr<?>, Sort> {
  private final Expr<?> expr;
  protected final Z3TypeAdapter type;
  private Function<Z3ExprAdapter, Z3ExprAdapter> wrapper = null;

  private Z3ExprAdapter genConstraint = null;

  @Override
  public Expr<?> getExpr() {
    return expr;
  }

  public void setWrapper(Function<Z3ExprAdapter, Z3ExprAdapter> wrapper) {
    this.wrapper = wrapper;
  }

  public Function<Z3ExprAdapter, Z3ExprAdapter> getWrapper() {
    return wrapper;
  }

  public boolean isPresentWrapper() {
    return wrapper != null;
  }

  public boolean isPresentGenConstr() {
    return genConstraint != null;
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

  public boolean isOptExpr() {
    return type.isOptional();
  }

  public boolean isIntExpr() {
    return type.isInt();
  }

  public boolean isDoubleExpr() {
    return type.isDouble();
  }

  @Override
  public String toString() {
    return expr.toString();
  }

  public boolean isCharExpr() {
    return type.isChar();
  }

  public void addGenConstraint(Z3ExprAdapter constraint) {
    this.genConstraint = constraint;
  }

  public Z3ExprAdapter getGenConstraint() {
    return genConstraint;
  }
}
