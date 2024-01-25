package de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory;

import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;

public interface CDExprFactory<T extends ExprAdapter<?>> {
  T getAttribute(T obj, FieldSymbol attribute);

  T getLinkedObjects(T obj, String attribute);
}
