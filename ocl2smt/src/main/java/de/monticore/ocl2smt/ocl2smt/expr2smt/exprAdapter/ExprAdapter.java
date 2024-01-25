package de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter;

import de.monticore.ocl2smt.ocl2smt.expr.ExpressionKind;

public interface ExprAdapter<T> {
  T getExpr();

  ExpressionKind getExprKind();
}
