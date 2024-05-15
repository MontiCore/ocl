package de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3;

import com.microsoft.z3.Expr;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Z3ExprAdapter implements ExprAdapter<Expr<?>> {
  private final Expr<?> expr;

  protected final Z3TypeAdapter type;
  protected Z3TypeAdapter typeCast;
  private Function<Z3ExprAdapter, Z3ExprAdapter> wrapper = null;
  private final List<Z3ExprAdapter> genConstraint = new ArrayList<>();

  public Z3ExprAdapter(Expr<?> expr, Z3TypeAdapter type) {
    this.expr = expr;
    this.type = type;
  }

  @Override
  public Expr<?> getExpr() {
    return expr;
  }

  @Override
  public Z3TypeAdapter getType() {
    return type;
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

  public boolean isCharExpr() {
    return type.isChar();
  }

  public void setWrapper(Function<Z3ExprAdapter, Z3ExprAdapter> wrapper) {
    this.wrapper = wrapper;
  }

  public Function<Z3ExprAdapter, Z3ExprAdapter> getWrapper() {
    return wrapper != null ? wrapper : expr -> expr;
  }

  public void addGenConstraint(List<Z3ExprAdapter> constraint) {
    this.genConstraint.addAll(constraint);
  }

  public List<Z3ExprAdapter> getGenConstraint() {
    return genConstraint;
  }

  @Override
  public String toString() {
    return expr.toString();
  }

  public void setTypeCast(Z3TypeAdapter typeCast) {
    this.typeCast = typeCast;
  }

  public boolean isPresentTypeCast() {
    return typeCast != null;
  }

  public Z3TypeAdapter getTypeCast() {
    return typeCast;
  }
}
