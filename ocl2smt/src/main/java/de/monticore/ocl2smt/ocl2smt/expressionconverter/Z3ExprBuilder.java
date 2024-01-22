package de.monticore.ocl2smt.ocl2smt.expressionconverter;

import com.microsoft.z3.*;
import de.monticore.literals.mccommonliterals._ast.*;
import de.se_rwth.commons.logging.Log;

public class Z3ExprBuilder extends ExprBuilder<Expr<?>> {
  private Expr<?> expr = null;
  private ExpressionKind kind;
  private Context ctx;

  @Override
  public Z3ExprBuilder mkBool(ASTBooleanLiteral node) {
    this.expr = ctx.mkBool(node.getValue());
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  public Z3ExprBuilder mkString(ASTStringLiteral node) {
    this.expr = ctx.mkString(node.getValue());
    this.kind = ExpressionKind.STRING;
    return this;
  }

  @Override
  public Z3ExprBuilder mkInt(ASTNatLiteral node) {
    this.expr = ctx.mkInt(node.getValue());
    this.kind = ExpressionKind.INTEGER;
    return this;
  }

  @Override
  public Z3ExprBuilder mkChar(ASTCharLiteral node) {
    this.expr = ctx.mkInt(node.getValue());
    this.kind = ExpressionKind.CHAR;
    return this;
  }

  @Override
  public Z3ExprBuilder mkDouble(ASTBasicDoubleLiteral node) {
    this.expr = ctx.mkFP(node.getValue(), ctx.mkFPSortDouble());
    this.kind = ExpressionKind.DOUBLE;
    return this;
  }

  @Override
  public Z3ExprBuilder mkNot(ExprBuilder<Expr<?>> node) {
    if (node.kind != ExpressionKind.BOOL) {
      Log.error("mkNot() is only implemented for BoolExpression");
    }

    this.expr = ctx.mkNot((BoolExpr) expr);
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  Z3ExprBuilder mkAnd(ExprBuilder<Expr<?>> leftNode, ExprBuilder<Expr<?>> rightNode) {
    if (leftNode.kind != ExpressionKind.BOOL || rightNode.kind == ExpressionKind.BOOL) {
      Log.error("mkAnd(..,..) is only implemented for BoolExpressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    this.expr = ctx.mkAnd((BoolExpr) leftNode.expr, (BoolExpr) rightNode.expr);
    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkOr(ExprBuilder<Expr<?>> leftNode, ExprBuilder<Expr<?>> rightNode) {
    if (leftNode.kind != ExpressionKind.BOOL || rightNode.kind == ExpressionKind.BOOL) {
      Log.error("mkOr(..,..) is only implemented for BoolExpressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    this.expr = ctx.mkOr((BoolExpr) leftNode.expr, (BoolExpr) rightNode.expr);
    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkEq(ExprBuilder<Expr<?>> leftNode, ExprBuilder<Expr<?>> rightNode) {
    this.kind = ExpressionKind.BOOL;
    this.expr = ctx.mkEq(leftNode.expr, rightNode.expr);
    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkImplies(ExprBuilder<Expr<?>> leftNode, ExprBuilder<Expr<?>> rightNode) {
    if (leftNode.kind != ExpressionKind.BOOL || rightNode.kind == ExpressionKind.BOOL) {
      Log.error("mkImplies(..,..) is only implemented for BoolExpressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    this.expr = ctx.mkImplies((BoolExpr) leftNode.expr, (BoolExpr) rightNode.expr);
    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkNeq(ExprBuilder<Expr<?>> leftNode, ExprBuilder<Expr<?>> rightNode) {
    this.kind = ExpressionKind.BOOL;
    this.expr = ctx.mkNot(ctx.mkEq(leftNode.expr, rightNode.expr));
    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkLt(ExprBuilder<Expr<?>> leftNode, ExprBuilder<Expr<?>> rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkLt((IntExpr) leftNode.expr, (IntExpr) rightNode.expr);
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPLt((FPNum) leftNode.expr, (FPNum) rightNode.expr);
    } else {
      Log.error("mkLt(..,..) is only implemented for Int or real expressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkLeq(ExprBuilder<Expr<?>> leftNode, ExprBuilder<Expr<?>> rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkLe((IntExpr) leftNode.expr, (IntExpr) rightNode.expr);
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPLEq((FPNum) leftNode.expr, (FPNum) rightNode.expr);
    } else {
      Log.error("mkLeq(..,..) is only implemented for Int or real expressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkGt(ExprBuilder<Expr<?>> leftNode, ExprBuilder<Expr<?>> rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkGt((IntExpr) leftNode.expr, (IntExpr) rightNode.expr);
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPGt((FPNum) leftNode.expr, (FPNum) rightNode.expr);
    } else {
      Log.error("mkGt(..,..) is only implemented for Int or real expressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkGe(ExprBuilder<Expr<?>> leftNode, ExprBuilder<Expr<?>> rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkGe((IntExpr) leftNode.expr, (IntExpr) rightNode.expr);
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPGEq((FPNum) leftNode.expr, (FPNum) rightNode.expr);
    } else {
      Log.error("mkGe(..,..) is only implemented for Int or real expressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkSub(ExprBuilder<Expr<?>> leftNode, ExprBuilder<Expr<?>> rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkSub((IntExpr) leftNode.expr, (IntExpr) rightNode.expr);
      this.kind = ExpressionKind.INTEGER;
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPSub(ctx.mkFPRNA(), (FPNum) leftNode.expr, (FPNum) rightNode.expr);
      this.kind = ExpressionKind.DOUBLE;
    } else {
      Log.error("mkSub(..,..) is only implemented for Int or real expressions left and right");
    }

    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkPlus(ExprBuilder<Expr<?>> leftNode, ExprBuilder<Expr<?>> rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkAdd((IntExpr) leftNode.expr, (IntExpr) rightNode.expr);
      this.kind = ExpressionKind.INTEGER;
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPAdd(ctx.mkFPRNA(), (FPNum) leftNode.expr, (FPNum) rightNode.expr);
      this.kind = ExpressionKind.DOUBLE;
    } else {
      Log.error("mkPlus(..,..) is only implemented for Int or real expressions left and right");
    }
    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkMul(ExprBuilder<Expr<?>> leftNode, ExprBuilder<Expr<?>> rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkMul((IntExpr) leftNode.expr, (IntExpr) rightNode.expr);
      this.kind = ExpressionKind.INTEGER;
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPMul(ctx.mkFPRNA(), (FPNum) leftNode.expr, (FPNum) rightNode.expr);
      this.kind = ExpressionKind.DOUBLE;
    } else {
      Log.error("mkMul(..,..) is only implemented for Int or real expressions left and right");
    }
    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkDiv(ExprBuilder<Expr<?>> leftNode, ExprBuilder<Expr<?>> rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkDiv((IntExpr) leftNode.expr, (IntExpr) rightNode.expr);
      this.kind = ExpressionKind.INTEGER;
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPDiv(ctx.mkFPRNA(), (FPNum) leftNode.expr, (FPNum) rightNode.expr);
      this.kind = ExpressionKind.DOUBLE;
    } else {
      Log.error("mkDiv(..,..) is only implemented for Int or real expressions left and right");
    }
    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkMod(ExprBuilder<Expr<?>> leftNode, ExprBuilder<Expr<?>> rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkMod((IntExpr) leftNode.expr, (IntExpr) rightNode.expr);
    } else {
      Log.error("mkMod(..,..) is only implemented for Int or real expressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkPlusPrefix(ExprBuilder<Expr<?>> node) {
    if (node.kind != ExpressionKind.INTEGER && node.kind != ExpressionKind.DOUBLE) {
      Log.error(
          "mkPlusPrefix(..,..) is only implemented for Int or real expressions left and right");
    }
    this.expr = node.expr;
    this.kind = node.kind;
    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkMinusPrefix(ExprBuilder<Expr<?>> node) {
    if (node.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkMul(ctx.mkInt(-1), (IntExpr) node.expr);
      this.kind = ExpressionKind.INTEGER;
    } else if (node.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPMul(ctx.mkFPRNA(), ctx.mkFP(-1, ctx.mkFPSortDouble()), (FPNum) node.expr);
      this.kind = ExpressionKind.DOUBLE;
    } else {
      Log.error(
          "mkMinusPrefix(..,..) is only implemented for Int or real expressions left and right");
    }
    return this;
  }

  @Override
  ExprBuilder<Expr<?>> mkIte(
      ExprBuilder<Expr<?>> cond, ExprBuilder<Expr<?>> expr1, ExprBuilder<Expr<?>> expr2) {
    if (cond.kind != ExpressionKind.BOOL) {
      Log.error("mkIte(..,..) is only implemented for Int or real expressions left and right");
    }
    this.expr = ctx.mkITE((BoolExpr) cond.expr, expr1.expr, expr1.expr);
    this.kind = ExpressionKind.ANY;
    return this;
  }
}
