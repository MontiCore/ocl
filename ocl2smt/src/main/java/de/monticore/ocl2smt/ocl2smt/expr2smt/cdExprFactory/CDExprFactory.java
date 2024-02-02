package de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory;

import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;

public interface CDExprFactory<E extends ExprAdapter<?, T>,T> {
  E getLink(E obj, String link);

  E getLinkedObjects(E obj, String role); // todo check if FieldSymbol can be resolve form role name

}
