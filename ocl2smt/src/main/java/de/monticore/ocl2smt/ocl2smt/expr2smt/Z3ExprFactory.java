package de.monticore.ocl2smt.ocl2smt.expr2smt;

import com.microsoft.z3.*;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.oclexpressions._ast.ASTExistsExpression;
import de.monticore.ocl.oclexpressions._ast.ASTForallExpression;
import de.monticore.ocl2smt.ocl2smt.expr.ExpressionKind;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprFactory.ExprFactory;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.OCLExprConverter;
import de.monticore.ocl2smt.util.OCLType;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.monticore.cd2smt.Helper.CDHelper.getASTCDType;

public class Z3ExprFactory implements ExprFactory<Z3ExprAdapter> {
  Context ctx;

    public Z3ExprFactory(Context context) {
      this.ctx = context ;
    }

    @Override
  public Z3ExprAdapter mkBool(boolean node) {
    Z3ExprAdapter expr = new Z3ExprAdapter();
    expr.setExpr(ctx.mkBool(node));
    expr.setKind(ExpressionKind.BOOL);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkString(String node) {
    Z3ExprAdapter expr = new Z3ExprAdapter();
    expr.setExpr(ctx.mkString(node));
    expr.setKind(ExpressionKind.STRING);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkInt(int node) {
    Z3ExprAdapter expr = new Z3ExprAdapter();
    expr.setExpr(ctx.mkInt(node));
    expr.setKind(ExpressionKind.INTEGER);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkChar(char node) {
    Z3ExprAdapter expr = new Z3ExprAdapter();
    expr.setExpr(ctx.mkInt(node));
    expr.setKind(ExpressionKind.CHAR);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkDouble(double node) {
    Z3ExprAdapter expr = new Z3ExprAdapter();
    expr.setExpr(ctx.mkFP(node, ctx.mkFPSortDouble()));
    expr.setKind(ExpressionKind.DOUBLE);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkNot(Z3ExprAdapter node) {
    if (!node.isBool()) {
      Log.error("mkNot() is only implemented for BoolExpression");
    }

    Z3ExprAdapter res = new Z3ExprAdapter();
    res.setExpr(ctx.mkNot((BoolExpr) node.getExpr()));
    res.setKind(ExpressionKind.BOOL);
    return res;
  }

  @Override
  public Z3ExprAdapter mkAnd(Z3ExprAdapter left, Z3ExprAdapter right) {
    if (!left.isBool() || !right.isBool()) {
      Log.error("mkAnd(..,..) is only implemented for Bool Expressions left and right");
    }

    Z3ExprAdapter expr = new Z3ExprAdapter();
    expr.setKind(ExpressionKind.BOOL);
    expr.setExpr(ctx.mkAnd((BoolExpr) left.getExpr(), (BoolExpr) right.getExpr()));
    return expr;
  }

  @Override
  public Z3ExprAdapter mkOr(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    if (!leftNode.isBool() || !rightNode.isBool()) {
      Log.error("mkOr(..,..) is only implemented for BoolExpressions left and right");
    }

    Z3ExprAdapter expr = new Z3ExprAdapter();
    expr.setKind(ExpressionKind.BOOL);
    expr.setExpr(ctx.mkOr((BoolExpr) leftNode.getExpr(), (BoolExpr) rightNode.getExpr()));
    return expr;
  }

  @Override
  public Z3ExprAdapter mkImplies(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    if (!leftNode.isBool() || !rightNode.isBool()) {
      Log.error("mkImplies(..,..) is only implemented for BoolExpressions left and right");
    }

    Z3ExprAdapter expr = new Z3ExprAdapter();
    expr.setKind(ExpressionKind.BOOL);
    expr.setExpr(ctx.mkImplies((BoolExpr) leftNode.getExpr(), (BoolExpr) rightNode.getExpr()));
    return expr;
  }

  @Override
  public Z3ExprAdapter mkLt(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    Z3ExprAdapter expr = new Z3ExprAdapter();

    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      expr.setExpr(ctx.mkLt((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr()));
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      expr.setExpr(ctx.mkFPLt((FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr()));
    } else {
      Log.error("mkLt(..,..) is only implemented for Int or real expressions left and right");
    }
    expr.setKind(ExpressionKind.BOOL);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkLeq(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    Z3ExprAdapter expr = new Z3ExprAdapter();

    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      expr.setExpr(ctx.mkLe((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr()));
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      expr.setExpr(ctx.mkFPLEq((FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr()));
    } else {
      Log.error("mkLeq(..,..) is only implemented for Int or real expressions left and right");
    }
    expr.setKind(ExpressionKind.BOOL);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkGt(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    Z3ExprAdapter expr = new Z3ExprAdapter();

    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      expr.setExpr(ctx.mkGt((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr()));
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {

      expr.setExpr(ctx.mkFPGt((FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr()));
    } else {
      Log.error("mkGt(..,..) is only implemented for Int or real expressions left and right");
    }
    expr.setKind(ExpressionKind.BOOL);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkGe(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    Z3ExprAdapter expr = new Z3ExprAdapter();

    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      expr.setExpr(ctx.mkGe((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr()));
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      expr.setExpr(ctx.mkFPGEq((FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr()));
    } else {
      Log.error("mkGe(..,..) is only implemented for Int or real expressions left and right");
    }
    expr.setKind(ExpressionKind.BOOL);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkSub(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    Z3ExprAdapter expr = new Z3ExprAdapter();

    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      expr.setExpr(
          ctx.mkSub((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr()));
      expr.setKind(ExpressionKind.INTEGER);
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      expr.setExpr(
          ctx.mkFPSub(ctx.mkFPRNA(), (FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr()));
      expr.setKind(ExpressionKind.DOUBLE);
    } else {
      Log.error("mkSub(..,..) is only implemented for Int or real expressions left and right");
    }

    return expr;
  }

  @Override
  public Z3ExprAdapter mkPlus(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    Z3ExprAdapter expr = new Z3ExprAdapter();

    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      expr.setExpr(
          ctx.mkAdd((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr()));
      expr.setKind(ExpressionKind.INTEGER);
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      expr.setExpr(
          ctx.mkFPAdd(ctx.mkFPRNA(), (FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr()));
      expr.setKind(ExpressionKind.DOUBLE);
    } else if (leftNode.kind == ExpressionKind.STRING || rightNode.kind == ExpressionKind.STRING) {

      expr.setExpr(
          ctx.mkConcat(
              (Expr<SeqSort<Sort>>) leftNode.getExpr(), (Expr<SeqSort<Sort>>) rightNode.getExpr()));
      expr.setKind(ExpressionKind.STRING);
    } else {
      Log.error("mkPlus(..,..) is only implemented for Int or real expressions left and right");
    }
    return expr;
  }

  @Override
  public Z3ExprAdapter mkMul(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    Z3ExprAdapter expr = new Z3ExprAdapter();

    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      expr.setExpr(
          ctx.mkMul((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr()));
      expr.setKind(ExpressionKind.INTEGER);
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      expr.setExpr(
          ctx.mkFPMul(ctx.mkFPRNA(), (FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr()));
      expr.setKind(ExpressionKind.DOUBLE);
    } else {
      Log.error("mkMul(..,..) is only implemented for Int or real expressions left and right");
    }
    return expr;
  }

  @Override
  public Z3ExprAdapter mkDiv(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    Z3ExprAdapter expr = new Z3ExprAdapter();

    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      expr.setExpr(
          ctx.mkDiv((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr()));
      expr.setKind(ExpressionKind.INTEGER);
    } else if (leftNode.kind == ExpressionKind.DOUBLE || rightNode.kind == ExpressionKind.DOUBLE) {
      expr.setExpr(
          ctx.mkFPDiv(ctx.mkFPRNA(), (FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr()));
      expr.setKind(ExpressionKind.DOUBLE);
    } else {
      Log.error("mkDiv(..,..) is only implemented for Int or real expressions left and right");
    }
    return expr;
  }

  @Override
  public Z3ExprAdapter mkMod(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    Z3ExprAdapter expr = new Z3ExprAdapter();

    if (leftNode.kind == ExpressionKind.INTEGER || rightNode.kind == ExpressionKind.INTEGER) {
      expr.setExpr(ctx.mkMod((IntExpr) leftNode.getExpr(), (IntExpr) rightNode.getExpr()));
    } else {
      Log.error("mkMod(..,..) is only implemented for Int or real expressions left and right");
    }
    expr.setKind(ExpressionKind.BOOL);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkPlusPrefix(Z3ExprAdapter node) {
    Z3ExprAdapter expr = new Z3ExprAdapter();

    if (node.kind != ExpressionKind.INTEGER && node.kind != ExpressionKind.DOUBLE) {
      Log.error(
          "mkPlusPrefix(..,..) is only implemented for Int or real expressions left and right");
    }
    expr.setExpr(node.getExpr());
    expr.setKind(node.kind);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkMinusPrefix(Z3ExprAdapter node) {
    Z3ExprAdapter expr = new Z3ExprAdapter();

    if (node.kind == ExpressionKind.INTEGER) {
      expr.setExpr(ctx.mkMul(ctx.mkInt(-1), (ArithExpr<?>) node.getExpr()));
      expr.setKind(ExpressionKind.INTEGER);
    } else if (node.kind == ExpressionKind.DOUBLE) {
      expr.setExpr(
          ctx.mkFPMul(ctx.mkFPRNA(), ctx.mkFP(-1, ctx.mkFPSortDouble()), (FPExpr) node.getExpr()));
      expr.setKind(ExpressionKind.DOUBLE);
    } else {
      Log.error(
          "mkMinusPrefix(..,..) is only implemented for Int or real expressions left and right");
    }
    return expr;
  }

  @Override
  public Z3ExprAdapter mkIte(Z3ExprAdapter cond, Z3ExprAdapter expr1, Z3ExprAdapter expr2) {
    Z3ExprAdapter expr = new Z3ExprAdapter();

    if (!cond.isBool()) {
      Log.error("mkIte(..,..) is only implemented for Int or real expressions left and right");
    }
    expr.setExpr(ctx.mkITE((BoolExpr) cond.getExpr(), expr1.getExpr(), expr2.getExpr()));
    expr.setKind(ExpressionKind.BOOL);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkReplace(Z3ExprAdapter s, Z3ExprAdapter src, Z3ExprAdapter dst) {
    Z3ExprAdapter expr = new Z3ExprAdapter();

    if (!s.isString() || !src.isString() || !dst.isString()) {
      Log.error("mkIte(..,..) parameter must all be strings");
    }
    expr.setExpr(
        ctx.mkReplace(
            (Expr<SeqSort<Sort>>) s.getExpr(),
            (Expr<SeqSort<Sort>>) src.getExpr(),
            (Expr<SeqSort<Sort>>) dst.getExpr()));
    expr.setKind(ExpressionKind.STRING);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkContains(Z3ExprAdapter s1, Z3ExprAdapter s2) {
    Z3ExprAdapter expr = new Z3ExprAdapter();

    if (!s1.isString() || !s2.isString()) {
      Log.error("mkContains(..,..) parameter must all be strings");
    }
    Expr<SeqSort<Sort>> expr1 = (Expr<SeqSort<Sort>>) s1.getExpr();
    Expr<SeqSort<Sort>> expr2 = (Expr<SeqSort<Sort>>) s2.getExpr();

    expr.setExpr(ctx.mkContains(expr1, expr2));
    expr.setKind(ExpressionKind.STRING);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkPrefixOf(Z3ExprAdapter s1, Z3ExprAdapter s2) {
    if (!s1.isString() || !s2.isString()) {
      Log.error("mkPrefixOf(..,..) parameter must all be strings");
    }

    Z3ExprAdapter expr = new Z3ExprAdapter();
    Expr<SeqSort<Sort>> expr1 = (Expr<SeqSort<Sort>>) s1.getExpr();
    Expr<SeqSort<Sort>> expr2 = (Expr<SeqSort<Sort>>) s2.getExpr();
    expr.setExpr(ctx.mkPrefixOf(expr1, expr2));
    expr.setKind(ExpressionKind.STRING);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkSuffixOf(Z3ExprAdapter s1, Z3ExprAdapter s2) {
    if (!s1.isString() || !s2.isString()) {
      Log.error("mkSuffixOf(..,..) parameter must all be strings");
    }

    Z3ExprAdapter expr = new Z3ExprAdapter();
    Expr<SeqSort<Sort>> expr1 = (Expr<SeqSort<Sort>>) s1.getExpr();
    Expr<SeqSort<Sort>> expr2 = (Expr<SeqSort<Sort>>) s2.getExpr();
    expr.setExpr(ctx.mkSuffixOf(expr1, expr2));
    expr.setKind(ExpressionKind.STRING);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkSet(
      Function<Z3ExprAdapter, Z3ExprAdapter> setFunction,
      OCLType type,
      OCLExprConverter exprConv) {
    Z3SetExprAdapter expr = new Z3SetExprAdapter();

    expr.setFunction(setFunction);
    expr.setType(type);

    expr.setExprConverter(exprConv);
    expr.setKind(ExpressionKind.SET);
    return expr;
  }

  @Override
  public Z3ExprAdapter containsAll(Z3ExprAdapter exp1, Z3ExprAdapter exp2) {
    Z3ExprAdapter res = new Z3ExprAdapter();
    // todo check
    Z3SetExprAdapter set1 = (Z3SetExprAdapter) exp1;
    Z3SetExprAdapter set2 = (Z3SetExprAdapter) exp2;

    Z3ExprAdapter expr = set1.exprConv.declVariable(set1.type, "expr111");
    res.setExpr(
        set1.exprConv.mkForall(
            List.of(expr), mkImplies(mkContains(set1, expr), mkContains(set2, expr))));
    res.setKind(ExpressionKind.BOOL);
    return res;
  }

  @Override
  public Z3ExprAdapter mkIsEmpty(Z3ExprAdapter expr) {
    Z3ExprAdapter res = new Z3ExprAdapter();

    Z3SetExprAdapter set = ((Z3SetExprAdapter) expr);
    Z3ExprAdapter con = set.exprConv.declVariable(set.type, "expr11");

    res.setExpr(set.exprConv.mkForall(List.of(expr), mkNot(mkContains(set, con))));
    res.setKind(ExpressionKind.BOOL);
    return res;
  }

  @Override
  public Z3ExprAdapter mkSetUnion(Z3ExprAdapter expr1, Z3ExprAdapter expr2) {
    // todoe check types
    Z3SetExprAdapter set1 = (Z3SetExprAdapter) expr1;
    Z3SetExprAdapter set2 = (Z3SetExprAdapter) expr2;
    Function<Z3ExprAdapter, Z3ExprAdapter> setFunction =
        obj -> mkOr(mkContains(set1, obj), mkContains(set2, obj));
    return mkSet(setFunction, set1.type, set1.exprConv);
  }

  @Override
  public Z3ExprAdapter mkSetIntersect(Z3ExprAdapter expr1, Z3ExprAdapter expr2) {
    Z3SetExprAdapter set1 = (Z3SetExprAdapter) expr1;
    Z3SetExprAdapter set2 = (Z3SetExprAdapter) expr2;
    Function<Z3ExprAdapter, Z3ExprAdapter> setFunction =
        obj -> mkAnd(mkContains(set1, obj), mkContains(set2, obj));
    return mkSet(setFunction, set1.type, set1.exprConv);
  }

  @Override
  public Z3ExprAdapter mkSetMinus(Z3ExprAdapter expr1, Z3ExprAdapter expr2) {
    Z3SetExprAdapter set1 = (Z3SetExprAdapter) expr1;
    Z3SetExprAdapter set2 = (Z3SetExprAdapter) expr2;
    Function<Z3ExprAdapter, Z3ExprAdapter> setFunction =
        obj -> mkOr(mkContains(set1, obj), mkContains(set2, obj));
    return mkSet(setFunction, set1.type, set2.exprConv);
  }

  @Override
  public Z3ExprAdapter mkForall(List<Z3ExprAdapter> vars, Z3ExprAdapter body) {
    return mkQuantifier(vars,body,true);
  }

  @Override
  public Z3ExprAdapter mkExists(List<Z3ExprAdapter> vars, Z3ExprAdapter body) {
    return mkQuantifier(vars,body,false);
  }



  public Z3ExprAdapter mkQuantifier(List<Z3ExprAdapter> vars, Z3ExprAdapter body, boolean isForall) {

    // split expressions int non CDZ3ExprAdapterype(String , Bool..) and CDType Expression(Auction, Person...)
    List<Z3ExprAdapter> unInterpretedObj =
            vars.stream().filter(e -> e.isUnInterpreted() && !e.isBool()).collect(Collectors.toList());

    List<Z3ExprAdapter> uninterpretedBool =
            vars.stream().filter(e -> e.isUnInterpreted() && e.isBool()).collect(Collectors.toList());

    // collect the CDType of the CDType Expressions
    List<ASTCDType> types =
            unInterpretedObj.stream()
                    .map(var -> getASTCDType(getType(var).getName(), getCD()))
                    .collect(Collectors.toList());
    BoolExpr subRes = body.expr();

    if (!unInterpretedObj.isEmpty()) {
      if (isForall) {
        subRes =
                cd2smtGenerator.mkForall(
                        types,
                        unInterpretedObj.stream().map(e -> (Expr<?>) e.expr()).collect(Collectors.toList()),
                        body.expr());
        // todo: refactoring this method
      } else {
        subRes =
                cd2smtGenerator.mkExists(
                        types,
                        unInterpretedObj.stream().map(e -> (Expr<?>) e.expr()).collect(Collectors.toList()),
                        body.expr());
      }
    }

    if (uninterpretedBool.isEmpty()) {
      return subRes;
    } else {
      if (isForall) {
        return ctx.mkForall(
                uninterpretedBool.stream().map(e -> (Expr<?>) e.expr()).toArray(Expr[]::new),
                subRes,
                0,
                null,
                null,
                null,
                null);
      } else {
        return ctx.mkExists(
                uninterpretedBool.stream().map(e -> (Expr<?>) e.expr()).toArray(Expr[]::new),
                subRes,
                0,
                null,
                null,
                null,
                null);
      }
    }
  }

  @Override
  public Z3ExprAdapter mkEq(Z3ExprAdapter left, Z3ExprAdapter right) {
    Z3ExprAdapter expr = new Z3ExprAdapter();
    if (!left.isSet() && !right.isBool()) {
      expr.setKind(ExpressionKind.BOOL);
      expr.setExpr(ctx.mkEq(left.getExpr(), right.getExpr()));
      return expr;
    }

    Z3SetExprAdapter leftSet = (Z3SetExprAdapter) left;
    expr = leftSet.exprConv.declVariable(leftSet.type, "const");
    expr.setExpr(
        leftSet.exprConv.mkForall(
            List.of(expr), mkEq(mkContains(left, expr), mkContains(right, expr))));

    expr.setKind(ExpressionKind.BOOL);
    return expr;
  }

  @Override
  public Z3ExprAdapter mkNeq(Z3ExprAdapter left, Z3ExprAdapter right) {
    return mkNot(mkEq(left, right));
  }
}
