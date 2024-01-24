package de.monticore.ocl2smt.ocl2smt.expressionconverter;

import com.microsoft.z3.*;
import de.monticore.literals.mccommonliterals._ast.*;
import de.se_rwth.commons.logging.Log;

public class Z3ExprBuilder   extends ExprBuilder{
  private Expr<?>  expr = null;
  private ExpressionKind kind;
  private MCContext ctx;

  public Z3ExprBuilder(MCContext ctx){
   this.kind = ExpressionKind.NULL ;
   this.ctx = ctx ;
  }

  @Override
  protected <T> T expr() {
    if (expr != null){
      return (T) expr;
    }
    Log.error("false Expr");
    assert  false ;
    return  null ;
  }

  @Override
  public ExprBuilder mkBool(ASTBooleanLiteral node) {
    this.expr = ctx.mkBool(node.getValue());
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  public ExprBuilder   mkString(ASTStringLiteral node) {
    this.expr = ctx.mkString(node.getValue());
    this.kind = ExpressionKind.STRING;
    return this;
  }

  @Override
  public ExprBuilder   mkInt(ASTNatLiteral node) {
    this.expr = ctx.mkInt(node.getValue());
    this.kind = ExpressionKind.INTEGER;
    return this;
  }

  @Override
  public ExprBuilder   mkChar(ASTCharLiteral node) {
    this.expr = ctx.mkInt(node.getValue());
    this.kind = ExpressionKind.CHAR;
    return this;
  }

  @Override
  public ExprBuilder   mkDouble(ASTBasicDoubleLiteral node) {
    this.expr = ctx.mkFP(node.getValue(), ctx.mkFPSortDouble());
    this.kind = ExpressionKind.DOUBLE;
    return this;
  }


  @Override
  public ExprBuilder   mkNot(ExprBuilder node) {
    if (node.kind != ExpressionKind.BOOL) {
      Log.error("mkNot() is only implemented for BoolExpression");
    }

    this.expr = ctx.mkNot((BoolExpr) expr);
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  ExprBuilder   mkAnd(ExprBuilder leftNode, ExprBuilder rightNode) {
    if (leftNode.kind != ExpressionKind.BOOL || rightNode.kind == ExpressionKind.BOOL) {
      Log.error("mkAnd(..,..) is only implemented for BoolExpressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    this.expr = ctx.mkAnd(leftNode.expr(), rightNode.expr());
    return this;
  }

  @Override
  ExprBuilder  mkOr(ExprBuilder  leftNode, ExprBuilder  rightNode) {
    if (leftNode.kind != ExpressionKind.BOOL || rightNode.kind == ExpressionKind.BOOL) {
      Log.error("mkOr(..,..) is only implemented for BoolExpressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    this.expr = ctx.mkOr(leftNode.expr(), rightNode.expr());
    return this;
  }

  @Override
  ExprBuilder  mkEq(ExprBuilder  leftNode, ExprBuilder  rightNode) {
    this.kind = ExpressionKind.BOOL;
    this.expr = ctx.mkEq(leftNode.expr(), rightNode.expr());
    return this;
  }

  @Override
  ExprBuilder  mkImplies(ExprBuilder  leftNode, ExprBuilder  rightNode) {
    if (leftNode.kind != ExpressionKind.BOOL || rightNode.kind == ExpressionKind.BOOL) {
      Log.error("mkImplies(..,..) is only implemented for BoolExpressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    this.expr = ctx.mkImplies(leftNode. expr(), rightNode. expr());
    return this;
  }

  @Override
  ExprBuilder   mkNeq(ExprBuilder   leftNode, ExprBuilder   rightNode) {
    this.kind = ExpressionKind.BOOL;
    this.expr = ctx.mkNot(ctx.mkEq(leftNode. expr(), rightNode. expr()));
    return this;
  }

  @Override
  ExprBuilder   mkLt(ExprBuilder   leftNode, ExprBuilder   rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkLt(leftNode. expr(), rightNode. expr());
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPLt(leftNode. expr(), rightNode. expr());
    } else {
      Log.error("mkLt(..,..) is only implemented for Int or real expressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  ExprBuilder   mkLeq(ExprBuilder   leftNode, ExprBuilder   rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this. expr = ctx.mkLe(leftNode. expr(), rightNode. expr());
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPLEq(leftNode. expr(), rightNode. expr());
    } else {
      Log.error("mkLeq(..,..) is only implemented for Int or real expressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  ExprBuilder   mkGt(ExprBuilder   leftNode, ExprBuilder   rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkGt(leftNode. expr(), rightNode. expr());
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPGt(leftNode. expr(), rightNode. expr());
    } else {
      Log.error("mkGt(..,..) is only implemented for Int or real expressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  ExprBuilder   mkGe(ExprBuilder   leftNode, ExprBuilder   rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkGe(leftNode. expr(), rightNode. expr());
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPGEq(leftNode. expr(), rightNode. expr());
    } else {
      Log.error("mkGe(..,..) is only implemented for Int or real expressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  ExprBuilder   mkSub(ExprBuilder   leftNode, ExprBuilder   rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkSub(leftNode. expr(),  rightNode. expr());
      this.kind = ExpressionKind.INTEGER;
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPSub(ctx.mkFPRNA(), leftNode. expr(), rightNode. expr());
      this.kind = ExpressionKind.DOUBLE;
    } else {
      Log.error("mkSub(..,..) is only implemented for Int or real expressions left and right");
    }

    return this;
  }

  @Override
  ExprBuilder   mkPlus(ExprBuilder   leftNode, ExprBuilder   rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkAdd( leftNode. expr(),  rightNode. expr());
      this.kind = ExpressionKind.INTEGER;
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPAdd(ctx.mkFPRNA(), leftNode. expr(), rightNode. expr());
      this.kind = ExpressionKind.DOUBLE;
    }  else if (leftNode.kind == ExpressionKind.STRING || rightNode.kind == ExpressionKind.STRING) {
    this.expr = ctx.mkConcat(leftNode. expr(), rightNode. expr());
    this.kind = ExpressionKind.STRING;
  }
    else {
      Log.error("mkPlus(..,..) is only implemented for Int or real expressions left and right");
    }
    return this;
  }

  @Override
  ExprBuilder   mkMul(ExprBuilder   leftNode, ExprBuilder   rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkMul(leftNode. expr(), rightNode. expr());
      this.kind = ExpressionKind.INTEGER;
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPMul(ctx.mkFPRNA(), leftNode. expr(), rightNode. expr());
      this.kind = ExpressionKind.DOUBLE;
    } else {
      Log.error("mkMul(..,..) is only implemented for Int or real expressions left and right");
    }
    return this;
  }

  @Override
  ExprBuilder   mkDiv(ExprBuilder   leftNode, ExprBuilder   rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkDiv(leftNode. expr(),  rightNode. expr());
      this.kind = ExpressionKind.INTEGER;
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPDiv(ctx.mkFPRNA(), leftNode. expr(), rightNode. expr());
      this.kind = ExpressionKind.DOUBLE;
    } else {
      Log.error("mkDiv(..,..) is only implemented for Int or real expressions left and right");
    }
    return this;
  }

  @Override
  ExprBuilder   mkMod(ExprBuilder   leftNode, ExprBuilder   rightNode) {
    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkMod(leftNode. expr(), rightNode. expr());
    } else {
      Log.error("mkMod(..,..) is only implemented for Int or real expressions left and right");
    }
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  ExprBuilder   mkPlusPrefix(ExprBuilder   node) {
    if (node.kind != ExpressionKind.INTEGER && node.kind != ExpressionKind.DOUBLE) {
      Log.error(
          "mkPlusPrefix(..,..) is only implemented for Int or real expressions left and right");
    }
    this.expr = node. expr();
    this.kind = node.kind;
    return this;
  }

  @Override
  ExprBuilder   mkMinusPrefix(ExprBuilder   node) {
    if (node.kind == ExpressionKind.INTEGER) {
      this.expr = ctx.mkMul(ctx.mkInt(-1), node. expr());
      this.kind = ExpressionKind.INTEGER;
    } else if (node.kind == ExpressionKind.DOUBLE) {
      this.expr = ctx.mkFPMul(ctx.mkFPRNA(), ctx.mkFP(-1, ctx.mkFPSortDouble()), node. expr());
      this.kind = ExpressionKind.DOUBLE;
    } else {
      Log.error(
          "mkMinusPrefix(..,..) is only implemented for Int or real expressions left and right");
    }
    return this;
  }

  @Override
  ExprBuilder   mkIte(
      ExprBuilder   cond, ExprBuilder   expr1, ExprBuilder   expr2) {
    if (cond.kind != ExpressionKind.BOOL) {
      Log.error("mkIte(..,..) is only implemented for Int or real expressions left and right");
    }
    this.expr = ctx.mkITE(cond. expr(), expr1. expr(), expr1. expr());
    this.kind = ExpressionKind.NULL;
    return this;
  }

  @Override
  ExprBuilder mkReplace(ExprBuilder s, ExprBuilder src, ExprBuilder dst) {
    Z3ExprBuilder res = new Z3ExprBuilder() ;
    if (!s.isString() || !src.isString() || !dst.isString()) {
      Log.error("mkIte(..,..) parameter must all be strings");
    }
   res.expr =  ctx.mkReplace(s.expr(),src.expr(),dst.expr());
   res.kind = ExpressionKind.STRING ;
   return res ;
  }

  @Override
  ExprBuilder mkContains(ExprBuilder s1, ExprBuilder s2) {
    Z3ExprBuilder res = new Z3ExprBuilder() ;
    if (!s1.isString() || !s2.isString()) {
      Log.error("mkContains(..,..) parameter must all be strings");
    }
    res.expr =  ctx.mkConcat(s1.expr(),s2.expr());
    res.kind = ExpressionKind.STRING ;
    return res ;
  }

  @Override
  ExprBuilder mkPrefixOf(ExprBuilder s1, ExprBuilder s2) {
    Z3ExprBuilder res = new Z3ExprBuilder() ;
    if (!s1.isString() || !s2.isString()) {
      Log.error("mkPrefixOf(..,..) parameter must all be strings");
    }
    res.expr =  ctx.mkPrefixOf(s1.expr(),s2.expr());
    res.kind = ExpressionKind.STRING ;
    return res ;
  }

  @Override
  ExprBuilder mkSuffixOf( ExprBuilder s1, ExprBuilder s2) {
    Z3ExprBuilder res = new Z3ExprBuilder() ;
    if (!s1.isString() || !s2.isString()) {
      Log.error("mkSuffixOf(..,..) parameter must all be strings");
    }
    res.expr =  ctx.mkSuffixOf(s1.expr(),s2.expr());
    res.kind =ExpressionKind.STRING ;
    return res ;
  }

}
