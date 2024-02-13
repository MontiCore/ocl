package de.monticore.ocl2smt.ocl2smt.expr2smt.exprFactory;

import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;
import java.util.function.Function;

/**
 * this interface introduce basic operations on SMT expressions.
 */
public interface ExprFactory<E extends ExprAdapter<?>> {
  E mkBool(boolean value);

  E mkString(String value);

  E mkInt(int value);

  E mkChar(char value);

  E mkDouble(double value);

  E mkConst(String name, TypeAdapter type);

  E mkNot(E expr);

  E mkAnd(E leftExpr, E rightExpr);

  E mkOr(E leftExpr, E rightExpr);

  E mkEq(E leftExpr, E rightExpr);

  E mkImplies(E leftExpr, E rightExpr);

  E mkNeq(E leftExpr, E rightExpr);

  E mkLt(E leftExpr, E rightExpr);

  E mkLeq(E leftExpr, E rightExpr);

  E mkGt(E leftExpr, E rightExpr);

  E mkGe(E leftExpr, E rightExpr);

  E mkSub(E leftExpr, E rightExpr);

  E mkPlus(E leftExpr, E rightExpr);

  E mkMul(E leftExpr, E rightExpr);

  E mkDiv(E leftExpr, E rightExpr);

  E mkMod(E leftExpr, E rightExpr);

  E mkPlusPrefix(E expr);

  E mkMinusPrefix(E expr);

  E mkIte(E cond, E leftExpr, E rightExpr);

  E mkReplace(E string, E src, E dest);

  E mkPrefixOf(E prefix, E string);

  E mkSuffixOf(E suffix, E string);

  E mkSet(Function<E, E> setFunction, E elem);

  E containsAll(E set1, E set2);

  E mkIsEmpty(E set);

  E mkSetUnion(E set1, E set2);

  E mkSetIntersect(E set1, E set2);

  E mkSetMinus(E set1, E set2);

  E mkContains(E collection, E element);
}
