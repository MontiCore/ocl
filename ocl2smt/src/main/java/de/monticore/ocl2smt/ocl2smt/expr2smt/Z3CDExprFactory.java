package de.monticore.ocl2smt.ocl2smt.expr2smt;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory.CDExprFactory;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;

public class Z3CDExprFactory implements CDExprFactory<Z3ExprAdapter> {
    CD2SMTGenerator cd2SMTGenerator ;

    public Z3CDExprFactory(CD2SMTGenerator cd2SMTGenerator){
        this.cd2SMTGenerator = cd2SMTGenerator ;
    }

  @Override
  public Z3ExprAdapter getAttribute(Z3ExprAdapter obj, FieldSymbol attribute) {
    return null;
  }

  @Override
  public Z3ExprAdapter getLinkedObjects(Z3ExprAdapter obj, String roleName) {
    return null;
  }
}
