package de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3;

import de.monticore.ocl2smt.ocl2smt.expr2smt.ExpressionKind;
import java.util.function.Function;

public class Z3GenExprAdapter extends Z3ExprAdapter {

  private final Function<Z3ExprAdapter, Z3ExprAdapter> function;

  private final Z3ExprAdapter element;

  public Z3GenExprAdapter(
      Function<Z3ExprAdapter, Z3ExprAdapter> function,
      Z3ExprAdapter element,
      String name,
      ExpressionKind kind) {
    super(null, new Z3TypeAdapter(name, element.getExprType().getSort(), kind));

    this.function = function;
    this.element = element;
  }

  public Z3ExprAdapter getElement() {
    return element;
  }

  public Z3ExprAdapter isIn(Z3ExprAdapter element) {
    // todo check types
    return function.apply(element);
  }

  @Override
  public String toString() {
    return super.toString();
  }
}
