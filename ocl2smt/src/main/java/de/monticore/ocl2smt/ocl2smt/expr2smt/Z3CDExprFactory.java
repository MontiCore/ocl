package de.monticore.ocl2smt.ocl2smt.expr2smt;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory.CDExprFactory;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import java.util.List;

public class Z3CDExprFactory implements CDExprFactory<Z3ExprAdapter> {
  CD2SMTGenerator cd2SMTGenerator;

  public Z3CDExprFactory(CD2SMTGenerator cd2SMTGenerator) {
    this.cd2SMTGenerator = cd2SMTGenerator;
  }

  @Override
  public Z3ExprAdapter getAttribute(Z3ExprAdapter obj, FieldSymbol attribute) {
    return null;
  }

  @Override
  public Z3ExprAdapter getLinkedObjects(Z3ExprAdapter obj, String roleName) {
    return null;
  }

  @Override
  public Z3ExprAdapter mkForall(List<Z3ExprAdapter> expr, Z3ExprAdapter z3ExprAdapter) {
    return null;
  }

  @Override
  public Z3ExprAdapter mkExists(List<Z3ExprAdapter> expr, Z3ExprAdapter z3ExprAdapter) {
    return null;
  }

  /*   protected T convertSimpleFieldAccessSet(T obj, String role) {
      OCLType type1 = getType(obj);
      ASTCDAssociation association = OCLHelper.getAssociation(type1, role, getCD());
      OCLType type2 = OCLHelper.getOtherType(association, type1, role, getCD());

      Function<T, T> auction_per_set =
              per ->
                      factory
                              .mkBool(
                                      OCLHelper.evaluateLink(
                                              association, obj, role, per, cd2smtGenerator, this::getType));

      return factory.mkSet(auction_per_set, type2, this);
  }*/
}
