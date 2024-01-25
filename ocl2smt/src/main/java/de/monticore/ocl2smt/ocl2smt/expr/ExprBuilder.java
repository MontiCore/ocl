package de.monticore.ocl2smt.ocl2smt.expr;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import de.monticore.literals.mccommonliterals._ast.*;

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
