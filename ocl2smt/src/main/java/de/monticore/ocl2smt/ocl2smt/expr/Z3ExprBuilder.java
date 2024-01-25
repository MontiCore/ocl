package de.monticore.ocl2smt.ocl2smt.expr;

import com.microsoft.z3.*;
import de.monticore.literals.mccommonliterals._ast.*;
import de.se_rwth.commons.logging.Log;

public class Z3ExprBuilder extends ExprBuilder {
  protected Expr<?> expr = null;


  protected Sort sort;

  public Z3ExprBuilder(Context ctx) {
    this.kind = ExpressionKind.NULL;
    this.ctx = ctx;
  }

  @Override
  public <T> T expr() {
    if (expr != null) {
      return (T) expr;
    }
    Log.error("false Expr");
    assert false;
    return null;
  }

  @Override
  public <Sort> Sort sort() {
    return (Sort) sort;
  }

  @Override
  public ExprBuilder mkBool(boolean node) {
    this.expr = ctx.mkBool(node);
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  public ExprBuilder mkBool(BoolExpr node) {
    this.expr = node;
    kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  public ExprBuilder mkString(String node) {
    this.expr = ctx.mkString(node);
    this.kind = ExpressionKind.STRING;
    return this;
  }

  @Override
  public ExprBuilder mkInt(int node) {
    this.expr = ctx.mkInt(node);
    this.kind = ExpressionKind.INTEGER;
    return this;
  }

  @Override
  public ExprBuilder mkChar(char node) {
    this.expr = ctx.mkInt(node);
    this.kind = ExpressionKind.CHAR;
    return this;
  }

  @Override
  public ExprBuilder mkDouble(double node) {
    this.expr = ctx.mkFP(node, ctx.mkFPSortDouble());
    this.kind = ExpressionKind.DOUBLE;
    return this;
  }

  @Override
  public <SORT> ExprBuilder mkExpr(String name, SORT sort) {
    this.expr = ctx.mkConst(name, (Sort) sort);
    this.kind = ExpressionKind.UNINTERPRETED;
    return this;
  }

  @Override
  public ExprBuilder mkNot(ExprBuilder node) {
    if (node.kind != ExpressionKind.BOOL) {
      Log.error("mkNot() is only implemented for BoolExpression");
    }

    this.expr = ctx.mkNot(node.expr());
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  public ExprBuilder mkAnd(ExprBuilder left, ExprBuilder right) {
    if (left.kind != ExpressionKind.BOOL || right.kind != ExpressionKind.BOOL) {
      Log.error("mkAnd(..,..) is only implemented for BoolExpressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    this.expr = ctx.mkAnd(left.expr(), right.expr());
    return this;
  }

  @Override
  public ExprBuilder mkOr(ExprBuilder leftNode, ExprBuilder rightNode) {
    if (leftNode.kind != ExpressionKind.BOOL || rightNode.kind != ExpressionKind.BOOL) {
      Log.error("mkOr(..,..) is only implemented for BoolExpressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    this.expr = ctx.mkOr(leftNode.expr(), rightNode.expr());
    return this;
  }

  @Override
  public ExprBuilder mkEq(ExprBuilder leftNode, ExprBuilder rightNode) {
    this.kind = ExpressionKind.BOOL;
    this.expr = ctx.mkEq(leftNode.expr(), rightNode.expr());
    return this;
  }

  @Override
  public ExprBuilder mkImplies(ExprBuilder leftNode, ExprBuilder rightNode) {
    if (leftNode.kind != ExpressionKind.BOOL || rightNode.kind != ExpressionKind.BOOL) {
      Log.error("mkImplies(..,..) is only implemented for BoolExpressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    this.expr = ctx.mkImplies(leftNode.expr(), rightNode.expr());
    return this;
  }

  @Override
  public ExprBuilder mkNeq(ExprBuilder leftNode, ExprBuilder rightNode) {
    this.kind = ExpressionKind.BOOL;
    this.expr = ctx.mkNot(ctx.mkEq(leftNode.expr(), rightNode.expr()));
    return this;
  }

  @Override
  public ExprBuilder mkLt(ExprBuilder leftNode, ExprBuilder rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkLt(leftNode.expr(), rightNode.expr());
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPLt(leftNode.expr(), rightNode.expr());
    } else {
      Log.error("mkLt(..,..) is only implemented for Int or real expressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  public ExprBuilder mkLeq(ExprBuilder leftNode, ExprBuilder rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkLe(leftNode.expr(), rightNode.expr());
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPLEq(leftNode.expr(), rightNode.expr());
    } else {
      Log.error("mkLeq(..,..) is only implemented for Int or real expressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  public ExprBuilder mkGt(ExprBuilder leftNode, ExprBuilder rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkGt(leftNode.expr(), rightNode.expr());
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPGt(leftNode.expr(), rightNode.expr());
    } else {
      Log.error("mkGt(..,..) is only implemented for Int or real expressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  public ExprBuilder mkGe(ExprBuilder leftNode, ExprBuilder rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkGe(leftNode.expr(), rightNode.expr());
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPGEq(leftNode.expr(), rightNode.expr());
    } else {
      Log.error("mkGe(..,..) is only implemented for Int or real expressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  public ExprBuilder mkSub(ExprBuilder leftNode, ExprBuilder rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkSub(leftNode.expr(), rightNode.expr());
      this.kind = ExpressionKind.INTEGER;
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPSub(ctx.mkFPRNA(), leftNode.expr(), rightNode.expr());
      this.kind = ExpressionKind.DOUBLE;
    } else {
      Log.error("mkSub(..,..) is only implemented for Int or real expressions left and right");
    }

    return this;
  }

  @Override
  public ExprBuilder mkPlus(ExprBuilder leftNode, ExprBuilder rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkAdd(leftNode.expr(), rightNode.expr());
      this.kind = ExpressionKind.INTEGER;
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPAdd(ctx.mkFPRNA(), leftNode.expr(), rightNode.expr());
      this.kind = ExpressionKind.DOUBLE;
    } else if (leftNode.kind == ExpressionKind.STRING || rightNode.kind == ExpressionKind.STRING) {
      this.expr = ctx.mkConcat(leftNode.expr(), rightNode.expr());
      this.kind = ExpressionKind.STRING;
    } else {
      Log.error("mkPlus(..,..) is only implemented for Int or real expressions left and right");
    }
    return this;
  }

  @Override
  public ExprBuilder mkMul(ExprBuilder leftNode, ExprBuilder rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkMul(leftNode.expr(), rightNode.expr());
      this.kind = ExpressionKind.INTEGER;
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPMul(ctx.mkFPRNA(), leftNode.expr(), rightNode.expr());
      this.kind = ExpressionKind.DOUBLE;
    } else {
      Log.error("mkMul(..,..) is only implemented for Int or real expressions left and right");
    }
    return this;
  }

  @Override
  public ExprBuilder mkDiv(ExprBuilder leftNode, ExprBuilder rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkDiv(leftNode.expr(), rightNode.expr());
      this.kind = ExpressionKind.INTEGER;
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPDiv(ctx.mkFPRNA(), leftNode.expr(), rightNode.expr());
      this.kind = ExpressionKind.DOUBLE;
    } else {
      Log.error("mkDiv(..,..) is only implemented for Int or real expressions left and right");
    }
    return this;
  }

  @Override
  public ExprBuilder mkMod(ExprBuilder leftNode, ExprBuilder rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkMod(leftNode.expr(), rightNode.expr());
    } else {
      Log.error("mkMod(..,..) is only implemented for Int or real expressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  public ExprBuilder mkPlusPrefix(ExprBuilder node) {
    if (node.kind != ExpressionKind.INTEGER && node.kind != ExpressionKind.DOUBLE) {
      Log.error(
          "mkPlusPrefix(..,..) is only implemented for Int or real expressions left and right");
    }
    this.expr = node.expr();
    this.kind = node.kind;
    return this;
  }

  @Override
  public ExprBuilder mkMinusPrefix(ExprBuilder node) {
    if (node.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkMul(ctx.mkInt(-1), node.expr());
      this.kind = ExpressionKind.INTEGER;
    } else if (node.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPMul(ctx.mkFPRNA(), ctx.mkFP(-1, ctx.mkFPSortDouble()), node.expr());
      this.kind = ExpressionKind.DOUBLE;
    } else {
      Log.error(
          "mkMinusPrefix(..,..) is only implemented for Int or real expressions left and right");
    }
    return this;
  }

  @Override
  public ExprBuilder mkIte(ExprBuilder cond, ExprBuilder expr1, ExprBuilder expr2) {
    if (cond.kind != ExpressionKind.BOOL) {
      Log.error("mkIte(..,..) is only implemented for Int or real expressions left and right");
    }
    this.expr = ctx.mkITE(cond.expr(), expr1.expr(), expr1.expr());
    this.kind = ExpressionKind.NULL;
    return this;
  }

  @Override
  public ExprBuilder mkReplace(ExprBuilder s, ExprBuilder src, ExprBuilder dst) {

    if (!s.isString() || !src.isString() || !dst.isString()) {
      Log.error("mkIte(..,..) parameter must all be strings");
    }
    expr = ctx.mkReplace(s.expr(), src.expr(), dst.expr());
    kind = ExpressionKind.STRING;
    return this;
  }

  @Override
  public ExprBuilder mkContains(ExprBuilder s1, ExprBuilder s2) {

    if (!s1.isString() || !s2.isString()) {
      Log.error("mkContains(..,..) parameter must all be strings");
    }
    expr = ctx.mkConcat(s1.expr(), s2.expr());
    kind = ExpressionKind.STRING;
    return this;
  }

  @Override
  public ExprBuilder mkPrefixOf(ExprBuilder s1, ExprBuilder s2) {

    if (!s1.isString() || !s2.isString()) {
      Log.error("mkPrefixOf(..,..) parameter must all be strings");
    }
    expr = ctx.mkPrefixOf(s1.expr(), s2.expr());
    kind = ExpressionKind.STRING;
    return this;
  }

  @Override
  public ExprBuilder mkSuffixOf(ExprBuilder s1, ExprBuilder s2) {

    if (!s1.isString() || !s2.isString()) {
      Log.error("mkSuffixOf(..,..) parameter must all be strings");
    }
    expr = ctx.mkSuffixOf(s1.expr(), s2.expr());
    kind = ExpressionKind.STRING;
    return this;
  }
}
