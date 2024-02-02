package de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter;

import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;

public interface ExprAdapter<E, T> {
  E getExpr();

  TypeAdapter<T> getExprType();
}
