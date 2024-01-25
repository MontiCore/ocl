package de.monticore.ocl2smt.ocl2smt.expr;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import de.monticore.literals.mccommonliterals._ast.*;

/**
 * (alle interfaces in eigenes package)
 *
 * interface: ExprFactory<T extends ExprAdapter>
 *   * public T mkAnd(T fst, T snd)
 *    ... und andere funktionen, so wie der z3 context
 *
 * interface: CDExprFactory<T extends ExprAdapter> implements ExprFactory<T extends ExprAdapter>
 *   * public T getAttribute(T obj, FieldSymbol attribute) // nutzt cd2smt
 *   ... und andere funktionen, spezifisch für CDs (... nutzt cd2smt)
 *
 * ??interface: OCLExprFactory<T extends ExprAdapter> implements CDExprFactory<T extends ExprAdapter>
 *   * public T mkOCLSet(...)
 *   ... und andere funktionen, spezifisch für OCLs (????? )

 *
 *   interface: ExprAdapter<T>
 *   * T getExpr()
 *   * getExprKind()
 *   NICHTS mit z3!!
 *
 *
 *
 *   und dann konkrete Z3 Klassen:
 *   public class Z3ExprAdapter extends ExprAdapter<Expression> // Expression = die normale von z3
 *     * hat einen konstruktor, wo eine Z3Expression reingeht (+type), und Z3ExprFactory kann den aufrufen
 * // vllt macht ein "Z3SetExprAdapter extends Z3ExprAdapter" Sinn.... vllt??
 *
 *   public class Z3ExprFactory extends ExprFactory<Z3ExprAdapter> // bekommt den context als konstruktor parameter
 */
public abstract class ExprBuilder {
  protected Context ctx;
  protected ExpressionKind kind;

  public abstract <Expr> Expr expr();

  public abstract <Sort> Sort sort();

  @Override
  public String toString() {
    return expr().toString();
  }

  public abstract ExprBuilder mkBool(boolean node);

  public abstract ExprBuilder mkBool(BoolExpr node); // todo : remove later

  public abstract <Expr> ExprBuilder mkExpr(ExpressionKind kind, Expr expr); // todo : remove later

  public abstract <SORT> ExprBuilder mkExpr(String name, SORT type);

  public abstract ExprBuilder mkString(String node);

  public abstract ExprBuilder mkInt(int node);

  public abstract ExprBuilder mkChar(char node);

  public abstract ExprBuilder mkDouble(double node);

  public abstract ExprBuilder mkNot(ExprBuilder node);

  public abstract ExprBuilder mkAnd(ExprBuilder leftNode, ExprBuilder rightNode);

  public abstract ExprBuilder mkOr(ExprBuilder leftNode, ExprBuilder rightNode);

  public abstract ExprBuilder mkEq(ExprBuilder leftNode, ExprBuilder rightNode);

  public abstract ExprBuilder mkImplies(ExprBuilder leftNode, ExprBuilder rightNode);

  public abstract ExprBuilder mkNeq(ExprBuilder leftNode, ExprBuilder rightNode);

  public abstract ExprBuilder mkLt(ExprBuilder leftNode, ExprBuilder rightNode);

  public abstract ExprBuilder mkLeq(ExprBuilder leftNode, ExprBuilder rightNode);

  public abstract ExprBuilder mkGt(ExprBuilder leftNode, ExprBuilder rightNode);

  public abstract ExprBuilder mkGe(ExprBuilder leftNode, ExprBuilder rightNode);

  public abstract ExprBuilder mkSub(ExprBuilder leftNode, ExprBuilder rightNode);

  public abstract ExprBuilder mkPlus(ExprBuilder leftNode, ExprBuilder rightNode);

  public abstract ExprBuilder mkMul(ExprBuilder leftNode, ExprBuilder rightNode);

  public abstract ExprBuilder mkDiv(ExprBuilder leftNode, ExprBuilder rightNode);

  public abstract ExprBuilder mkMod(ExprBuilder leftNode, ExprBuilder rightNode);

  public abstract ExprBuilder mkPlusPrefix(ExprBuilder leftNode);

  public abstract ExprBuilder mkMinusPrefix(ExprBuilder leftNode);

  public abstract ExprBuilder mkIte(ExprBuilder cond, ExprBuilder expr1, ExprBuilder expr2);

  public abstract ExprBuilder mkReplace(ExprBuilder s, ExprBuilder s1, ExprBuilder s2);

  public abstract ExprBuilder mkContains(ExprBuilder s1, ExprBuilder s2);

  public abstract ExprBuilder mkPrefixOf(ExprBuilder s1, ExprBuilder s2);

  public abstract ExprBuilder mkSuffixOf(ExprBuilder s1, ExprBuilder s2);

  boolean isArithExpr() {
    return kind == ExpressionKind.INTEGER || kind == ExpressionKind.DOUBLE;
  }

  public boolean isString() {
    return kind == ExpressionKind.STRING;
  }

  public boolean isUnInterpreted() {
    return kind == ExpressionKind.UNINTERPRETED;
  }

  boolean isPresent() {
    return expr() != null;
  }

  public boolean isBool() {
    return kind == ExpressionKind.BOOL;
  }

  public ExpressionKind getKind() {
    return kind;
  }

  public boolean hasNativeType() {
    return kind != ExpressionKind.NULL
        && kind != ExpressionKind.UNINTERPRETED
        && kind != ExpressionKind.SET;
  }

  public boolean isNull() {
    return kind == ExpressionKind.NULL;
  }
}
