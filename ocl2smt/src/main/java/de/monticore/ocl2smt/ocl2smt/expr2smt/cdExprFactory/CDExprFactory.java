package de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory;

import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import java.util.List;

public interface CDExprFactory<T extends ExprAdapter<?>> {
  T getAttribute(T obj, FieldSymbol attribute);

  T getLinkedObjects(T obj, String attribute);

  public T mkForall(List<T> expr, T z3ExprAdapter);

  public T mkExists(List<T> expr, T z3ExprAdapter);
}
