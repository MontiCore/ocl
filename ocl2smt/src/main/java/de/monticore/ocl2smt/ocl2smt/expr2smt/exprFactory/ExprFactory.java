package de.monticore.ocl2smt.ocl2smt.expr2smt.exprFactory;

import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;
import java.util.function.Function;

public interface ExprFactory<E extends ExprAdapter<?, T>, T> {
  E mkBool(boolean expr);

  E mkString(String expr);

  E mkInt(int expr);

  E mkChar(char expr);

  E mkDouble(double expr);

  E mkNot(E expr);

  E mkAnd(E expr1, E expr);

  E mkOr(E expr1, E expr);

  E mkEq(E expr1, E expr);

  E mkImplies(E expr1, E expr);

  E mkNeq(E expr1, E expr);

  E mkLt(E expr1, E expr);

  E mkLeq(E expr1, E expr);

  E mkGt(E expr1, E expr);

  E mkGe(E expr1, E expr);

  E mkSub(E expr1, E expr);

  E mkPlus(E expr1, E expr);

  E mkConcat(E leftNode, E rightNode);

  E mkMul(E expr1, E expr);

  E mkDiv(E expr1, E expr);

  E mkMod(E expr1, E expr);

  E mkPlusPrefix(E expr1);

  E mkMinusPrefix(E expr1);

  E mkIte(E cond, E expr1, E expr2);

  E mkReplace(E s, E s1, E s2);

  E mkPrefixOf(E s1, E s2);

  E mkSuffixOf(E s1, E s2);

  E mkSet(Function<E, E> setFunction, E element);

  E containsAll(E set1, E set2);

  E mkIsEmpty(E set);

  E mkSetUnion(E set1, E set);

  E mkSetIntersect(E set1, E set);

  E mkSetMinus(E set1, E set);

  E mkContains(E callerExpr, E arg1);

  E mkConst(String name, TypeAdapter<T> type);
}
