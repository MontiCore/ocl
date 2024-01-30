package de.monticore.ocl2smt.ocl2smt.expressionconverter;

import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTArguments;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.literals.mccommonliterals._ast.*;
import de.monticore.literals.mcliteralsbasis._ast.ASTLiteral;
import de.monticore.ocl.oclexpressions._ast.ASTEquivalentExpression;
import de.monticore.ocl.oclexpressions._ast.ASTIfThenElseExpression;
import de.monticore.ocl.oclexpressions._ast.ASTImpliesExpression;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprFactory.ExprFactory;
import de.monticore.ocl2smt.util.TypeConverter;
import de.se_rwth.commons.logging.Log;

public abstract class BasicExprConverter<T extends ExprAdapter<?>> {

  public ExprFactory<T> factory;

  public BasicExprConverter(ExprFactory<T> factory) {
    this.factory = factory;
  }

  protected T convertMethodCall(ASTCallExpression node) {

    T res = null;
    if (node.getExpression() instanceof ASTFieldAccessExpression) {
      ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();
      String methodName = ((ASTFieldAccessExpression) node.getExpression()).getName();
      ASTArguments args = node.getArguments();

      T arg1 = convertExpr(args.getExpression(0));
      T callerExpr = convertExpr(caller);
      switch (methodName) {
        case "before":
          res = factory.mkLt(callerExpr, arg1);
          break;

        case "after":
          res = factory.mkGt(callerExpr, arg1);
          break;
        case "contains":
          res = factory.mkContains(callerExpr, arg1);
          break;
        case "endsWith":
          res = factory.mkSuffixOf(callerExpr, arg1);
          break;
        case "startsWith":
          res = factory.mkPrefixOf(callerExpr, arg1);
          break;

        case "replace":
          T arg2 = convertExpr(args.getExpression(1));
          res = factory.mkReplace(callerExpr, arg1, arg2);
          break;

        case "containsAll":
          res = factory.containsAll(callerExpr, arg1);
          break;

        case "isEmpty":
          res = factory.mkIsEmpty(callerExpr);
          break;
      }
    }
    return res;
  }

  private T convert(ASTLiteral node) {
    T result = null;
    if (node instanceof ASTBooleanLiteral) {
      result = factory.mkBool(((ASTBooleanLiteral) node).getValue());
    } else if (node instanceof ASTStringLiteral) {
      result = factory.mkString(((ASTStringLiteral) node).getValue());
    } else if (node instanceof ASTNatLiteral) {
      result = factory.mkInt(((ASTNatLiteral) node).getValue());
    } else if (node instanceof ASTBasicDoubleLiteral) {
      result = factory.mkDouble(((ASTBasicDoubleLiteral) node).getValue());
    } else if (node instanceof ASTCharLiteral) {
      result = factory.mkChar(((ASTCharLiteral) node).getValue());
    }
    return result;
  }

  public T convertExpr(ASTExpression node) {
    T result = null;
    if (node instanceof ASTLiteralExpression) {
      result = convert(((ASTLiteralExpression) node).getLiteral());
    } else if (node instanceof ASTBooleanAndOpExpression) {
      T left = convertExpr(((ASTBooleanAndOpExpression) node).getLeft());
      T right = convertExpr(((ASTBooleanAndOpExpression) node).getRight());
      result = factory.mkAnd(left, right);
    } else if (node instanceof ASTBooleanOrOpExpression) {
      T left = convertExpr(((ASTBooleanOrOpExpression) node).getLeft());
      T right = convertExpr(((ASTBooleanOrOpExpression) node).getRight());
      result = factory.mkOr(left, right);
    } else if (node instanceof ASTBooleanNotExpression) {
      result = factory.mkNot(convertExpr(node));
    } else if (node instanceof ASTLogicalNotExpression) {
      result = factory.mkNot(convertExpr(((ASTLogicalNotExpression) node).getExpression()));
    } else if (node instanceof ASTLessEqualExpression) {
      T left = convertExpr(((ASTLessEqualExpression) node).getLeft());
      T right = convertExpr(((ASTLessEqualExpression) node).getRight());
      result = factory.mkLeq(left, right);
    } else if (node instanceof ASTLessThanExpression) {
      T left = convertExpr(((ASTLessThanExpression) node).getLeft());
      T right = convertExpr(((ASTLessThanExpression) node).getRight());
      result = factory.mkLt(left, right);
    } else if (node instanceof ASTEqualsExpression) {
      T left = convertExpr(((ASTEqualsExpression) node).getLeft());
      T right = convertExpr(((ASTEqualsExpression) node).getRight());
      result = factory.mkEq(left, right);
    } else if (node instanceof ASTNotEqualsExpression) {
      T left = convertExpr(((ASTNotEqualsExpression) node).getLeft());
      T right = convertExpr(((ASTNotEqualsExpression) node).getRight());
      result = factory.mkNeq(left, right);
    } else if (node instanceof ASTGreaterEqualExpression) {
      T left = convertExpr(((ASTGreaterEqualExpression) node).getLeft());
      T right = convertExpr(((ASTGreaterEqualExpression) node).getRight());
      result = factory.mkGe(left, right);
    } else if (node instanceof ASTGreaterThanExpression) {
      T left = convertExpr(((ASTGreaterThanExpression) node).getLeft());
      T right = convertExpr(((ASTGreaterThanExpression) node).getRight());
      result = factory.mkGt(left, right);
    } else if (node instanceof ASTImpliesExpression) {
      T left = convertExpr(((ASTImpliesExpression) node).getLeft());
      T right = convertExpr(((ASTImpliesExpression) node).getRight());
      result = factory.mkImplies(left, right);
    } else if (node instanceof ASTCallExpression && TypeConverter.hasBooleanType(node)) {
      result = convertMethodCall((ASTCallExpression) node);
    } else if (node instanceof ASTEquivalentExpression) {
      T left = convertExpr(((ASTEquivalentExpression) node).getLeft());
      T right = convertExpr(((ASTEquivalentExpression) node).getRight());
      result = factory.mkEq(left, right);
    } else if (node instanceof ASTMinusPrefixExpression) {
      T subExpr = convertExpr(((ASTMinusPrefixExpression) node).getExpression());
      result = factory.mkMinusPrefix(subExpr);
    } else if (node instanceof ASTPlusPrefixExpression) {
      T subExpr = convertExpr(((ASTPlusPrefixExpression) node).getExpression());
      result = factory.mkPlusPrefix(subExpr);
    } else if ((node instanceof ASTPlusExpression)) {
      T left = convertExpr(((ASTPlusExpression) node).getLeft());
      T right = convertExpr(((ASTPlusExpression) node).getRight());
      result = factory.mkPlus(left, right);
    } else if (node instanceof ASTMinusExpression) {
      T left = convertExpr(((ASTMinusExpression) node).getLeft());
      T right = convertExpr(((ASTMinusExpression) node).getRight());
      result = factory.mkSub(left, right);
    } else if (node instanceof ASTDivideExpression) {
      T left = convertExpr(((ASTDivideExpression) node).getLeft());
      T right = convertExpr(((ASTDivideExpression) node).getRight());
      result = factory.mkDiv(left, right);
    } else if (node instanceof ASTMultExpression) {
      T left = convertExpr(((ASTMultExpression) node).getLeft());
      T right = convertExpr(((ASTMultExpression) node).getRight());
      result = factory.mkMul(left, right);
    } else if (node instanceof ASTModuloExpression) {
      T left = convertExpr(((ASTModuloExpression) node).getLeft());
      T right = convertExpr(((ASTModuloExpression) node).getRight());
      result = factory.mkMod(left, right);
    } else if (node instanceof ASTBracketExpression) {
      result = convertExpr(((ASTBracketExpression) node).getExpression());
    } else if (node instanceof ASTNameExpression) {
      result = convert((ASTNameExpression) node);
    } else if (node instanceof ASTFieldAccessExpression) {
      result = convert((ASTFieldAccessExpression) node);
    } else if (node instanceof ASTIfThenElseExpression) {
      T cond = convertExpr(((ASTIfThenElseExpression) node).getCondition());
      T expr1 = convertExpr(((ASTIfThenElseExpression) node).getThenExpression());
      T expr2 = convertExpr(((ASTIfThenElseExpression) node).getElseExpression());
      result = factory.mkIte(cond, expr1, expr2);
    } else if (node instanceof ASTConditionalExpression) {
      T cond = convertExpr(((ASTConditionalExpression) node).getCondition());
      T expr1 = convertExpr(((ASTConditionalExpression) node).getTrueExpression());
      T expr2 = convertExpr(((ASTConditionalExpression) node).getFalseExpression());
      result = factory.mkIte(cond, expr1, expr2);
    } else if (node instanceof ASTCallExpression) {
      result = convertMethodCall((ASTCallExpression) node);
    } else {
      Log.error("conversion of Set of the type " + node.getClass().getName() + " not implemented");
    }
    return result;
  }

  protected abstract T convert(ASTNameExpression node);

  protected abstract T convert(ASTFieldAccessExpression node);
}
