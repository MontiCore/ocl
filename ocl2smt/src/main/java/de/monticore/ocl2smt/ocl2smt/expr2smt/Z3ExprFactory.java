package de.monticore.ocl2smt.ocl2smt.expr2smt;

import com.microsoft.z3.*;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprFactory.ExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;
import de.se_rwth.commons.logging.Log;
import java.util.List;
import java.util.function.Function;

public class Z3ExprFactory implements ExprFactory<Z3ExprAdapter, Sort> {
  private final Context ctx;
  private final Z3TypeFactory tFactory;

  private final Z3CDExprFactory cdFactory;
  private final CD2SMTGenerator cd2SMTGenerator;

  private final String wrongParam =
      "Method %s(...) get parameter with wrong type '%s' expected was %s";

  public Z3ExprFactory(
      Z3CDExprFactory cdFactory, Z3TypeFactory factory, CD2SMTGenerator cd2SMTGenerator) {
    this.ctx = cd2SMTGenerator.getContext();
    this.cd2SMTGenerator = cd2SMTGenerator;
    this.tFactory = factory;
    this.cdFactory = cdFactory;
  }

  @Override
  public Z3ExprAdapter mkBool(boolean node) {
    return new Z3ExprAdapter(ctx.mkBool(node), tFactory.mkBoolType());
  }

  @Override
  public Z3ExprAdapter mkString(String node) {
    return new Z3ExprAdapter(ctx.mkString(node), tFactory.mkStringType());
  }

  @Override
  public Z3ExprAdapter mkInt(int node) {
    return new Z3ExprAdapter(ctx.mkInt(node), tFactory.mkInType());
  }

  @Override
  public Z3ExprAdapter mkChar(char node) {
    return new Z3ExprAdapter(ctx.mkInt(node), tFactory.mkCharTYpe());
  }

  @Override
  public Z3ExprAdapter mkDouble(double node) {
    return new Z3ExprAdapter(ctx.mkFP(node, ctx.mkFPSortDouble()), tFactory.mkDoubleType());
  }

  @Override
  public Z3ExprAdapter mkNot(Z3ExprAdapter node) {
    checkBool("mkNot", node);
    return new Z3ExprAdapter(ctx.mkNot((BoolExpr) node.getExpr()), tFactory.mkBoolType());
  }

  @Override
  public Z3ExprAdapter mkAnd(Z3ExprAdapter left, Z3ExprAdapter right) {
    checkBool("mkAnd", left);
    checkBool("mkAnd", right);
    return new Z3ExprAdapter(
        ctx.mkAnd((BoolExpr) left.getExpr(), (BoolExpr) right.getExpr()), tFactory.mkBoolType());
  }

  @Override
  public Z3ExprAdapter mkOr(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkBool("mkOr", leftNode);
    checkBool("mkOr", rightNode);

    BoolExpr left = (BoolExpr) leftNode.getExpr();
    BoolExpr right = (BoolExpr) rightNode.getExpr();
    return new Z3ExprAdapter(ctx.mkOr(left, right), tFactory.mkBoolType());
  }

  @Override
  public Z3ExprAdapter mkImplies(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkBool("mkImplies", leftNode);
    checkBool("mkImplies", rightNode);

    BoolExpr left = (BoolExpr) leftNode.getExpr();
    BoolExpr right = (BoolExpr) rightNode.getExpr();
    return new Z3ExprAdapter(ctx.mkImplies(left, right), tFactory.mkBoolType());
  }

  @Override
  public Z3ExprAdapter mkLt(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkArith("mkLt", leftNode);
    checkArith("mkLt", rightNode);

    Expr<?> value;
    if (leftNode.isIntExpr() && rightNode.isIntExpr()) {
      value = ctx.mkLt((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
    } else {
      value = ctx.mkFPLt((FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
    }
    return new Z3ExprAdapter(value, tFactory.mkBoolType());
  }

  @Override
  public Z3ExprAdapter mkLeq(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkArith("mkLeq", leftNode);
    checkArith("mkLeq", rightNode);

    Expr<?> expr;
    if (leftNode.isIntExpr() && rightNode.isIntExpr()) {
      expr = ctx.mkLe((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
    } else {
      expr = ctx.mkFPLEq((FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
    }
    return new Z3ExprAdapter(expr, tFactory.mkBoolType());
  }

  @Override
  public Z3ExprAdapter mkGt(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkArith("mkGt", leftNode);
    checkArith("mkGt", rightNode);

    Expr<?> expr;
    if (leftNode.isIntExpr() && rightNode.isIntExpr()) {
      expr = ctx.mkGt((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
    } else {
      expr = ctx.mkFPGt((FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
    }
    return new Z3ExprAdapter(expr, tFactory.mkBoolType());
  }

  @Override
  public Z3ExprAdapter mkEq(Z3ExprAdapter left, Z3ExprAdapter right) {
    if (!left.isSetExpr() && !right.isSetExpr()) {
      return new Z3ExprAdapter(ctx.mkEq(left.getExpr(), right.getExpr()), tFactory.mkBoolType());
    }

    Z3ExprAdapter leftElement = ((Z3SetExprAdapter) left).getElement();
    Z3ExprAdapter body = mkEq(mkContains(leftElement, right), mkContains(right, right));
    return cdFactory.mkForall(List.of(leftElement), body);
  }

  @Override
  public Z3ExprAdapter mkGe(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkArith("mkGe", leftNode);
    checkArith("mkGe", rightNode);

    Expr<?> expr;
    if (leftNode.isIntExpr() || rightNode.isIntExpr()) {
      expr = ctx.mkGe((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
    } else {
      expr = ctx.mkFPGEq((FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
    }
    return new Z3ExprAdapter(expr, tFactory.mkBoolType());
  }

  @Override
  public Z3ExprAdapter mkSub(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkArith("mkSub", leftNode);
    checkArith("mkSub", rightNode);

    Expr<?> expr;
    Z3TypeAdapter type;
    if (leftNode.isIntExpr() || rightNode.isDoubleExpr()) {
      expr = ctx.mkSub((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
      type = tFactory.mkInType();
    } else {
      expr = ctx.mkFPSub(ctx.mkFPRNA(), (FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
      type = tFactory.mkDoubleType();
    }

    return new Z3ExprAdapter(expr, type);
  }

  @Override
  public Z3ExprAdapter mkPlus(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkArith("mkPlus", leftNode);
    checkArith("mkPlus", rightNode);

    Expr<?> expr;
    Z3TypeAdapter type;
    if (leftNode.isIntExpr() || rightNode.isIntExpr()) {
      expr = ctx.mkAdd((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
      type = tFactory.mkInType();
    } else {
      expr = ctx.mkFPAdd(ctx.mkFPRNA(), (FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
      type = tFactory.mkDoubleType();
    }
    return new Z3ExprAdapter(expr, type);
  }

  @Override
  public Z3ExprAdapter mkConcat(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkString("mkSub", leftNode);
    checkString("mkSub", rightNode);

    Expr<SeqSort<CharSort>> left = (Expr<SeqSort<CharSort>>) leftNode.getExpr();
    Expr<SeqSort<CharSort>> right = (Expr<SeqSort<CharSort>>) rightNode.getExpr();
    return new Z3ExprAdapter(ctx.mkConcat(left, right), tFactory.mkStringType());
  }

  @Override
  public Z3ExprAdapter mkMul(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkArith("mkSub", leftNode);
    checkArith("mkSub", rightNode);

    Expr<?> expr;
    Z3TypeAdapter type;
    if (leftNode.isIntExpr() || rightNode.isIntExpr()) {
      expr = ctx.mkMul((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
      type = tFactory.mkInType();
    } else {
      expr = ctx.mkFPMul(ctx.mkFPRNA(), (FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
      type = tFactory.mkDoubleType();
    }
    return new Z3ExprAdapter(expr, type);
  }

  @Override
  public Z3ExprAdapter mkDiv(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkArith("mkSub", leftNode);
    checkArith("mkSub", rightNode);

    Expr<?> expr;
    Z3TypeAdapter type;
    if (leftNode.isIntExpr() || rightNode.isIntExpr()) {
      expr = ctx.mkDiv((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
      type = tFactory.mkInType();
    } else {
      expr = ctx.mkFPDiv(ctx.mkFPRNA(), (FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
      type = tFactory.mkDoubleType();
    }
    return new Z3ExprAdapter(expr, type);
  }

  @Override
  public Z3ExprAdapter mkMod(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkInt("mkSub", leftNode);
    checkArith("mkSub", rightNode);

    Expr<?> expr = ctx.mkMod((IntExpr) leftNode.getExpr(), (IntExpr) rightNode.getExpr());
    Z3TypeAdapter type = tFactory.mkInType();
    return new Z3ExprAdapter(expr, type);
  }

  @Override
  public Z3ExprAdapter mkPlusPrefix(Z3ExprAdapter node) {
    return node;
  }

  @Override
  public Z3ExprAdapter mkMinusPrefix(Z3ExprAdapter node) {
    checkArith("mkMinusPrefix", node);

    Expr<?> expr;
    Z3TypeAdapter type;
    if (node.isIntExpr()) {
      expr = ctx.mkMul(ctx.mkInt(-1), (ArithExpr<?>) node.getExpr());
      type = tFactory.mkInType();
    } else {
      expr =
          ctx.mkFPMul(ctx.mkFPRNA(), ctx.mkFP(-1, ctx.mkFPSortDouble()), (FPExpr) node.getExpr());
      type = tFactory.mkDoubleType();
    }
    return new Z3ExprAdapter(expr, type);
  }

  @Override
  public Z3ExprAdapter mkIte(Z3ExprAdapter cond, Z3ExprAdapter expr1, Z3ExprAdapter expr2) {
    checkBool("mkIte", cond);

    Expr<?> expr = ctx.mkITE((BoolExpr) cond.getExpr(), expr1.getExpr(), expr2.getExpr());
    return new Z3ExprAdapter(expr, tFactory.mkBoolType());
  }

  @Override
  public Z3ExprAdapter mkReplace(Z3ExprAdapter s, Z3ExprAdapter src, Z3ExprAdapter dst) {
    checkString("mkReplace", s);
    checkString("mkReplace", src);
    checkString("mkReplace", dst);

    Expr<?> expr =
        ctx.mkReplace(
            (Expr<SeqSort<Sort>>) s.getExpr(),
            (Expr<SeqSort<Sort>>) src.getExpr(),
            (Expr<SeqSort<Sort>>) dst.getExpr());

    return new Z3ExprAdapter(expr, tFactory.mkStringType());
  }

  @Override
  public Z3ExprAdapter mkPrefixOf(Z3ExprAdapter s1, Z3ExprAdapter s2) {
    checkString("mkPrefixOf", s1);
    checkString("mkPrefixOf", s2);

    Expr<SeqSort<Sort>> expr1 = (Expr<SeqSort<Sort>>) s1.getExpr();
    Expr<SeqSort<Sort>> expr2 = (Expr<SeqSort<Sort>>) s2.getExpr();
    return new Z3ExprAdapter(ctx.mkPrefixOf(expr1, expr2), tFactory.mkStringType());
  }

  @Override
  public Z3ExprAdapter mkSuffixOf(Z3ExprAdapter s1, Z3ExprAdapter s2) {
    checkString("mkSuffixOf", s1);
    checkString("mkSuffixOf", s2);

    Expr<SeqSort<Sort>> expr1 = (Expr<SeqSort<Sort>>) s1.getExpr();
    Expr<SeqSort<Sort>> expr2 = (Expr<SeqSort<Sort>>) s2.getExpr();
    return new Z3ExprAdapter(ctx.mkSuffixOf(expr1, expr2), tFactory.mkStringType());
  }

  @Override
  public Z3ExprAdapter mkSet(
      Function<Z3ExprAdapter, Z3ExprAdapter> setFunction, Z3ExprAdapter expr) {
    return new Z3SetExprAdapter(setFunction, expr);
  }

  @Override
  public Z3ExprAdapter containsAll(Z3ExprAdapter exp1, Z3ExprAdapter exp2) {
    checkSet("containsAll", exp1);
    checkSet("containsAll", exp1);

    Z3SetExprAdapter set1 = (Z3SetExprAdapter) exp1;
    Z3SetExprAdapter set2 = (Z3SetExprAdapter) exp2;
    Z3ExprAdapter expr = set1.getElement();
    Z3ExprAdapter body = mkImplies(mkContains(set2, expr), mkContains(set1, expr));
    return cdFactory.mkForall(List.of(expr), body);
  }

  @Override
  public Z3ExprAdapter mkIsEmpty(Z3ExprAdapter expr) {
    checkSet("mkIsEmpty", expr);

    Z3SetExprAdapter set = ((Z3SetExprAdapter) expr);
    Z3ExprAdapter con = set.getElement();
    return cdFactory.mkForall(List.of(expr), mkNot(mkContains(set, con)));
  }

  @Override
  public Z3ExprAdapter mkSetUnion(Z3ExprAdapter expr1, Z3ExprAdapter expr2) {
    checkSet("mkSetUnion", expr1);
    checkSet("mkSetUnion", expr2);

    Z3SetExprAdapter set1 = (Z3SetExprAdapter) expr1;
    Z3SetExprAdapter set2 = (Z3SetExprAdapter) expr2;
    Function<Z3ExprAdapter, Z3ExprAdapter> setFunction =
        obj -> mkOr(mkContains(set1, obj), mkContains(set2, obj));
    return mkSet(setFunction, set1.getElement());
  }

  @Override
  public Z3ExprAdapter mkSetIntersect(Z3ExprAdapter expr1, Z3ExprAdapter expr2) {
    checkSet("mkSetIntersect", expr1);
    checkSet("mkSetIntersect", expr2);

    Z3SetExprAdapter set1 = (Z3SetExprAdapter) expr1;
    Z3SetExprAdapter set2 = (Z3SetExprAdapter) expr2;
    Function<Z3ExprAdapter, Z3ExprAdapter> setFunction =
        obj -> mkAnd(mkContains(set1, obj), mkContains(set2, obj));
    return mkSet(setFunction, set1.getElement());
  }

  @Override
  public Z3ExprAdapter mkSetMinus(Z3ExprAdapter expr1, Z3ExprAdapter expr2) {
    checkSet("mkSetMinus", expr1);
    checkSet("mkSetMinus", expr2);

    Z3SetExprAdapter set1 = (Z3SetExprAdapter) expr1;
    Z3SetExprAdapter set2 = (Z3SetExprAdapter) expr2;
    Function<Z3ExprAdapter, Z3ExprAdapter> setFunction =
        obj -> mkOr(mkContains(set1, obj), mkContains(set2, obj));
    return mkSet(setFunction, set1.getElement());
  }

  @Override
  public Z3ExprAdapter mkContains(Z3ExprAdapter expr1, Z3ExprAdapter arg1) {
    if (expr1.isStringExpr()) {
      Expr<SeqSort<Sort>> str1 = (Expr<SeqSort<Sort>>) expr1.getExpr();
      Expr<SeqSort<Sort>> str2 = (Expr<SeqSort<Sort>>) arg1.getExpr();
      return new Z3ExprAdapter(ctx.mkContains(str1, str2), tFactory.mkBoolType());
    }
    checkSet("mkContains", expr1);
    return ((Z3SetExprAdapter) expr1).isIn(arg1);
  }

  @Override
  public Z3ExprAdapter mkConst(String name, TypeAdapter<Sort> type) {
    return new Z3ExprAdapter(ctx.mkConst(name, type.getType()), (Z3TypeAdapter) type);
  }

  @Override
  public Z3ExprAdapter mkNeq(Z3ExprAdapter left, Z3ExprAdapter right) {
    return mkNot(mkEq(left, right));
  }

  private void checkBool(String method, Z3ExprAdapter node) {
    if (!node.isBoolExpr()) {
      Log.error(String.format(wrongParam, method, node.getExprType().getName(), "bool"));
    }
  }

  private void checkArith(String method, Z3ExprAdapter node) {
    if (!node.isIntExpr() && !node.isDoubleExpr()) {
      Log.error(String.format(wrongParam, method, node.getExprType(), "int or double"));
    }
  }

  private void checkString(String method, Z3ExprAdapter node) {
    if (!node.isStringExpr()) {
      Log.error(String.format(wrongParam, method, node.getExprType(), "String"));
    }
  }

  private void checkInt(String method, Z3ExprAdapter node) {
    if (!node.isIntExpr()) {
      Log.error(String.format(wrongParam, method, node.getExprType(), "int"));
    }
  }

  private void checkSet(String method, Z3ExprAdapter node) {
    if (!node.isSetExpr()) {
      Log.error(String.format(wrongParam, method, node.getExprType(), "bool"));
    }
  }
}
