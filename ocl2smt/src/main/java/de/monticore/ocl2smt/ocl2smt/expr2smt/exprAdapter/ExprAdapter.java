package de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter;

import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;

/***
 * this interface is used to abstract SMT expression form the concrete library used.
 * @param <E> concrete type of the expression.
 */
public interface ExprAdapter<E> {

  /***
   * @return the concrete expression.
   */
  E getExpr();

  /***
   * @return the adapted type of the expression.
   */
  TypeAdapter getType();
}
