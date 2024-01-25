package de.monticore.ocl2smt.ocl2smt.expr2smt;

import com.microsoft.z3.Expr;
import de.monticore.ocl2smt.ocl2smt.expr.ExpressionKind;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;

public class Z3ExprAdapter implements ExprAdapter<Expr<?>> {
  protected Expr<?> expr;
  protected ExpressionKind kind;

  @Override
  public Expr<?> getExpr() {
    return expr;
  }

  @Override
  public ExpressionKind getExprKind() {
    return kind;
  }

  public void setExpr(Expr<?> expr) {
    this.expr = expr;
  }

  public void setKind(ExpressionKind kind) {
    this.kind = kind;
  }

  boolean isArithExpr() {
    return kind == ExpressionKind.INTEGER || kind == ExpressionKind.DOUBLE;
  }

  public boolean isString() {
    return kind == ExpressionKind.STRING;
  }

  public boolean isUnInterpreted() {
    return kind == ExpressionKind.UNINTERPRETED;
  }

  public boolean isBool() {
    return kind == ExpressionKind.BOOL;
  }

  public boolean isSet() {
    return kind == ExpressionKind.SET;
  }
}
