package de.monticore.ocl2smt.ocl2smt.expr2smt.exprFactory;

import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.OCLExprConverter;
import de.monticore.ocl2smt.util.OCLType;

import java.util.List;
import java.util.function.Function;

public interface ExprFactory<T extends ExprAdapter<?>> {
  T mkBool(boolean expr);

  T mkString(String expr);

  T mkInt(int expr);

  T mkChar(char expr);

  T mkDouble(double expr);

  T mkNot(T expr);

  T mkAnd(T expr1, T expr);

  T mkOr(T expr1, T expr);

  T mkEq(T expr1, T expr);

  T mkImplies(T expr1, T expr);

  T mkNeq(T expr1, T expr);

  T mkLt(T expr1, T expr);

  T mkLeq(T expr1, T expr);

  T mkGt(T expr1, T expr);

  T mkGe(T expr1, T expr);

  T mkSub(T expr1, T expr);

  T mkPlus(T expr1, T expr);

  T mkMul(T expr1, T expr);

  T mkDiv(T expr1, T expr);

  T mkMod(T expr1, T expr);

  T mkPlusPrefix(T expr1);

  T mkMinusPrefix(T expr1);

  T mkIte(T cond, T expr1, T expr2);

  T mkReplace(T s, T s1, T s2);

  T mkContains(T s1, T s2);

  T mkPrefixOf(T s1, T s2);

  T mkSuffixOf(T s1, T s2);

  public abstract T mkSet(
      Function<T, T> setFunction, OCLType type, OCLExprConverter exprConv);

  T containsAll(T set1, T set2);

  T mkIsEmpty(T set);

  T mkSetUnion(T set1, T set);

  T mkSetIntersect(T set1, T set);

  T mkSetMinus(T set1, T set);

     public T mkForall(List<T> vars, T body);

     public T mkExists(List<T> vars, T body) ;
}
