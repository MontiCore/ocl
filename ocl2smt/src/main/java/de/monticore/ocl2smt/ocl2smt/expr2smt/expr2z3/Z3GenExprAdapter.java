package de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3;

import de.monticore.ocl2smt.ocl2smt.expr2smt.ExprKind;
import java.util.function.Function;

public class Z3GenExprAdapter extends Z3ExprAdapter {
  private final Function<Z3ExprAdapter, Z3ExprAdapter> elementFilter;
  private final Z3ExprAdapter element;

  public Z3GenExprAdapter(
      Function<Z3ExprAdapter, Z3ExprAdapter> elementFilter,
      Z3ExprAdapter element,
      String elementType,
      ExprKind kind) {
    super(null, new Z3TypeAdapter(elementType, element.getType().getSort(), kind));

    this.elementFilter = elementFilter;
    this.element = element;
  }

  public Z3ExprAdapter getElement() {
    return element;
  }

  public Z3ExprAdapter isIn(Z3ExprAdapter element) {
    return elementFilter.apply(element);
  }
}
