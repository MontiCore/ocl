package de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory;

import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprFactory.ExprFactory;
import java.util.List;

public interface CDExprFactory<E extends ExprAdapter<?>> extends ExprFactory<E> {
  E getLink(E obj, String role);

  E getLinkTransitive(E obj, String role);

  E unWrap(E opt);

  E mkForall(List<E> params, E body);

  E mkExists(List<E> params, E body);
}
