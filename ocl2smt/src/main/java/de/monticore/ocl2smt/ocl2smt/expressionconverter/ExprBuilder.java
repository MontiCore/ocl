package de.monticore.ocl2smt.ocl2smt.expressionconverter;

import de.monticore.literals.mccommonliterals._ast.*;

public abstract class ExprBuilder<Expr> {

  protected Expr expr;
  protected ExpressionKind kind;

  abstract ExprBuilder<Expr> mkBool(ASTBooleanLiteral node);

  abstract ExprBuilder<Expr> mkString(ASTStringLiteral node);

  abstract ExprBuilder<Expr> mkInt(ASTNatLiteral node);

  abstract ExprBuilder<Expr> mkChar(ASTCharLiteral node);

  abstract ExprBuilder<Expr> mkDouble(ASTBasicDoubleLiteral node);

  abstract ExprBuilder<Expr> mkNot(ExprBuilder<Expr> node);

  abstract ExprBuilder<Expr> mkAnd(ExprBuilder<Expr> leftNode, ExprBuilder<Expr> rightNode);

  abstract ExprBuilder<Expr> mkOr(ExprBuilder<Expr> leftNode, ExprBuilder<Expr> rightNode);

  abstract ExprBuilder<Expr> mkEq(ExprBuilder<Expr> leftNode, ExprBuilder<Expr> rightNode);

  abstract ExprBuilder<Expr> mkImplies(ExprBuilder<Expr> leftNode, ExprBuilder<Expr> rightNode);

  abstract ExprBuilder<Expr> mkNeq(ExprBuilder<Expr> leftNode, ExprBuilder<Expr> rightNode);

  abstract ExprBuilder<Expr> mkLt(ExprBuilder<Expr> leftNode, ExprBuilder<Expr> rightNode);

  abstract ExprBuilder<Expr> mkLeq(ExprBuilder<Expr> leftNode, ExprBuilder<Expr> rightNode);

  abstract ExprBuilder<Expr> mkGt(ExprBuilder<Expr> leftNode, ExprBuilder<Expr> rightNode);

  abstract ExprBuilder<Expr> mkGe(ExprBuilder<Expr> leftNode, ExprBuilder<Expr> rightNode);

  abstract ExprBuilder<Expr> mkSub(ExprBuilder<Expr> leftNode, ExprBuilder<Expr> rightNode);

  abstract ExprBuilder<Expr> mkPlus(ExprBuilder<Expr> leftNode, ExprBuilder<Expr> rightNode);

  abstract ExprBuilder<Expr> mkMul(ExprBuilder<Expr> leftNode, ExprBuilder<Expr> rightNode);

  abstract ExprBuilder<Expr> mkDiv(ExprBuilder<Expr> leftNode, ExprBuilder<Expr> rightNode);

  abstract ExprBuilder<Expr> mkMod(ExprBuilder<Expr> leftNode, ExprBuilder<Expr> rightNode);

  abstract ExprBuilder<Expr> mkPlusPrefix(ExprBuilder<Expr> leftNode);

  abstract ExprBuilder<Expr> mkMinusPrefix(ExprBuilder<Expr> leftNode);

  abstract ExprBuilder<Expr> mkIte(
      ExprBuilder<Expr> cond, ExprBuilder<Expr> expr1, ExprBuilder<Expr> expr2);
}
