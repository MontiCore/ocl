package de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory;

import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import java.util.List;

public interface CDExprFactory<E extends ExprAdapter<?, T>, T> {
  E getLink(E obj, String link);

  E getTransitiveLink(E obj, String link);

  E unwrapOptional(E opt);

  E mkForall(List<E> uninterpretedBool, E subRes);

  E mkExists(List<E> uninterpretedBool, E subRes);
}
