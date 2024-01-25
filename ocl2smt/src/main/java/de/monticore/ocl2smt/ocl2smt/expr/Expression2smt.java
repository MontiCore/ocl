package de.monticore.ocl2smt.ocl2smt.expr;

import com.microsoft.z3.*;
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
import de.monticore.ocl2smt.util.TypeConverter;
import de.se_rwth.commons.logging.Log;

public abstract class Expression2smt {

  protected Context ctx;

  /** convert method call that returns an expression* */
  protected ExprBuilder convertCallObject(ASTCallExpression node) {
    if (node.getExpression() instanceof ASTFieldAccessExpression) {
      ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();
      if (TypeConverter.hasStringType(caller)) {
        return convertCallerString(node);
      }
    }
    return null;
  }

  /***
   * convert the following methods call in smt and return a Bool-Expressions
   * -String.startsWith(String)
   * -String.endsWith(String)
   * -String.contains(String)
   * -Date.before(Date)
   * -Date.after(Date)
   */
  protected ExprBuilder convertCallBool(ASTCallExpression node) {
    ExprBuilder res = null;
    if (node.getExpression() instanceof ASTFieldAccessExpression) {
      ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();
      String methodName = ((ASTFieldAccessExpression) node.getExpression()).getName();
      if (TypeConverter.hasStringType(caller)) {
        res = convertBoolStringOp(caller, node.getArguments().getExpression(0), methodName);
      } else if (TypeConverter.hasDateType(caller)) {
        res = convertBoolDateOp(caller, node.getArguments().getExpression(0), methodName);
      }
    }
    return res;
  }

  /***
   * convert method call when the caller is a string and the method return an object
   * String.replace(x,y)
   */
  protected ExprBuilder convertCallerString(ASTCallExpression node) {
    if (node.getExpression() instanceof ASTFieldAccessExpression) {
      ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();
      ASTArguments arguments = node.getArguments();
      String methodName = ((ASTFieldAccessExpression) node.getExpression()).getName();

      ExprBuilder str = convertExpr(caller);
      if (methodName.equals("replace")) {
        ExprBuilder arg1 = convertExpr(arguments.getExpression(0));
        ExprBuilder arg2 = convertExpr(arguments.getExpression(1));
        return ExprMill.exprBuilder(ctx).mkReplace(str, arg1, arg2);
      }
    }
    return null;
  }

  private ExprBuilder convert(ASTLiteral node) {
    ExprBuilder result = null;
    if (node instanceof ASTBooleanLiteral) {
      result = ExprMill.exprBuilder(ctx).mkBool(((ASTBooleanLiteral) node).getValue());
    } else if (node instanceof ASTStringLiteral) {
      result = ExprMill.exprBuilder(ctx).mkString(((ASTStringLiteral) node).getValue());
    } else if (node instanceof ASTNatLiteral) {
      result = ExprMill.exprBuilder(ctx).mkInt(((ASTNatLiteral) node).getValue());
    } else if (node instanceof ASTBasicDoubleLiteral) {
      result = ExprMill.exprBuilder(ctx).mkDouble(((ASTBasicDoubleLiteral) node).getValue());
    } else if (node instanceof ASTCharLiteral) {
      result = ExprMill.exprBuilder(ctx).mkChar(((ASTCharLiteral) node).getValue());
    } else {
      result = ExprMill.exprBuilder(ctx);
    }

    return result;
  }

  public ExprBuilder convertExpr(ASTExpression node) {
    ExprBuilder result = null;
    if (node instanceof ASTLiteralExpression) {
      result = convert(((ASTLiteralExpression) node).getLiteral());
    } else if (node instanceof ASTBooleanAndOpExpression) {
      ExprBuilder left = convertExpr(((ASTBooleanAndOpExpression) node).getLeft());
      ExprBuilder right = convertExpr(((ASTBooleanAndOpExpression) node).getRight());
      result = ExprMill.exprBuilder(ctx).mkAnd(left, right);
    } else if (node instanceof ASTBooleanOrOpExpression) {
      ExprBuilder left = convertExpr(((ASTBooleanOrOpExpression) node).getLeft());
      ExprBuilder right = convertExpr(((ASTBooleanOrOpExpression) node).getRight());
      result = ExprMill.exprBuilder(ctx).mkOr(left, right);
    } else if (node instanceof ASTBooleanNotExpression) {
      result = ExprMill.exprBuilder(ctx).mkNot(convertExpr(node));
    } else if (node instanceof ASTLogicalNotExpression) {
      result =
          ExprMill.exprBuilder(ctx)
              .mkNot(convertExpr(((ASTLogicalNotExpression) node).getExpression()));
    } else if (node instanceof ASTLessEqualExpression) {
      ExprBuilder left = convertExpr(((ASTLessEqualExpression) node).getLeft());
      ExprBuilder right = convertExpr(((ASTLessEqualExpression) node).getRight());
      result = ExprMill.exprBuilder(ctx).mkLeq(left, right);
    } else if (node instanceof ASTLessThanExpression) {
      ExprBuilder left = convertExpr(((ASTLessThanExpression) node).getLeft());
      ExprBuilder right = convertExpr(((ASTLessThanExpression) node).getRight());
      result = ExprMill.exprBuilder(ctx).mkLt(left, right);
    } else if (node instanceof ASTEqualsExpression) {
      ExprBuilder left = convertExpr(((ASTEqualsExpression) node).getLeft());
      ExprBuilder right = convertExpr(((ASTEqualsExpression) node).getRight());
      result = ExprMill.exprBuilder(ctx).mkEq(left, right);
    } else if (node instanceof ASTNotEqualsExpression) {
      ExprBuilder left = convertExpr(((ASTNotEqualsExpression) node).getLeft());
      ExprBuilder right = convertExpr(((ASTNotEqualsExpression) node).getRight());
      result = ExprMill.exprBuilder(ctx).mkNeq(left, right);
    } else if (node instanceof ASTGreaterEqualExpression) {
      ExprBuilder left = convertExpr(((ASTGreaterEqualExpression) node).getLeft());
      ExprBuilder right = convertExpr(((ASTGreaterEqualExpression) node).getRight());
      result = ExprMill.exprBuilder(ctx).mkGe(left, right);
    } else if (node instanceof ASTGreaterThanExpression) {
      ExprBuilder left = convertExpr(((ASTGreaterThanExpression) node).getLeft());
      ExprBuilder right = convertExpr(((ASTGreaterThanExpression) node).getRight());
      result = ExprMill.exprBuilder(ctx).mkGt(left, right);
    } else if (node instanceof ASTImpliesExpression) {
      ExprBuilder left = convertExpr(((ASTImpliesExpression) node).getLeft());
      ExprBuilder right = convertExpr(((ASTImpliesExpression) node).getRight());
      result = ExprMill.exprBuilder(ctx).mkImplies(left, right);
    } else if (node instanceof ASTCallExpression && TypeConverter.hasBooleanType(node)) {
      result = convertCallBool((ASTCallExpression) node);
    } else if (node instanceof ASTEquivalentExpression) {
      ExprBuilder left = convertExpr(((ASTEquivalentExpression) node).getLeft());
      ExprBuilder right = convertExpr(((ASTEquivalentExpression) node).getRight());
      result = ExprMill.exprBuilder(ctx).mkEq(left, right);
    } else if (node instanceof ASTMinusPrefixExpression) {
      ExprBuilder subExpr = convertExpr(((ASTMinusPrefixExpression) node).getExpression());
      result = ExprMill.exprBuilder(ctx).mkMinusPrefix(subExpr);
    } else if (node instanceof ASTPlusPrefixExpression) {
      ExprBuilder subExpr = convertExpr(((ASTPlusPrefixExpression) node).getExpression());
      result = ExprMill.exprBuilder(ctx).mkPlusPrefix(subExpr);
    } else if ((node instanceof ASTPlusExpression)) {
      ExprBuilder left = convertExpr(((ASTPlusExpression) node).getLeft());
      ExprBuilder right = convertExpr(((ASTPlusExpression) node).getRight());
      result = ExprMill.exprBuilder(ctx).mkPlus(left, right);
    } else if (node instanceof ASTMinusExpression) {
      ExprBuilder left = convertExpr(((ASTMinusExpression) node).getLeft());
      ExprBuilder right = convertExpr(((ASTMinusExpression) node).getRight());
      result = ExprMill.exprBuilder(ctx).mkSub(left, right);
    } else if (node instanceof ASTDivideExpression) {
      ExprBuilder left = convertExpr(((ASTDivideExpression) node).getLeft());
      ExprBuilder right = convertExpr(((ASTDivideExpression) node).getRight());
      result = ExprMill.exprBuilder(ctx).mkDiv(left, right);
    } else if (node instanceof ASTMultExpression) {
      ExprBuilder left = convertExpr(((ASTMultExpression) node).getLeft());
      ExprBuilder right = convertExpr(((ASTMultExpression) node).getRight());
      result = ExprMill.exprBuilder(ctx).mkMul(left, right);
    } else if (node instanceof ASTModuloExpression) {
      ExprBuilder left = convertExpr(((ASTModuloExpression) node).getLeft());
      ExprBuilder right = convertExpr(((ASTModuloExpression) node).getRight());
      result = ExprMill.exprBuilder(ctx).mkMod(left, right);
    } else if (node instanceof ASTBracketExpression) {
      result = convertExpr(((ASTBracketExpression) node).getExpression());
    } else if (node instanceof ASTNameExpression) {
      result = convert((ASTNameExpression) node);
    } else if (node instanceof ASTFieldAccessExpression) {
      result = convert((ASTFieldAccessExpression) node);
    } else if (node instanceof ASTIfThenElseExpression) {
      ExprBuilder cond = convertExpr(((ASTIfThenElseExpression) node).getCondition());
      ExprBuilder expr1 = convertExpr(((ASTIfThenElseExpression) node).getThenExpression());
      ExprBuilder expr2 = convertExpr(((ASTIfThenElseExpression) node).getElseExpression());
      result = ExprMill.exprBuilder(ctx).mkIte(cond, expr1, expr2);

    } else if (node instanceof ASTConditionalExpression) {
      ExprBuilder cond = convertExpr(((ASTConditionalExpression) node).getCondition());
      ExprBuilder expr1 = convertExpr(((ASTConditionalExpression) node).getTrueExpression());
      ExprBuilder expr2 = convertExpr(((ASTConditionalExpression) node).getFalseExpression());
      result = ExprMill.exprBuilder(ctx).mkIte(cond, expr1, expr2);
    } else if (node instanceof ASTCallExpression) {
      result = convertCallObject((ASTCallExpression) node);
    } else if (node instanceof ASTCallExpression && TypeConverter.hasStringType(node)) {
      result = convertCallerString((ASTCallExpression) node);
    } else {
      notFullyImplemented(node);
    }

    if (result != null && !result.isNull()) {
      return result;

    } else {
      Log.error("Problem"); // todo fixme
      return null;
    }
  }

  // ---------------------------------------Logic---------------------------------

  protected ExprBuilder convertBoolDateOp(
      ASTExpression caller, ASTExpression arg, String methodName) {
    ExprBuilder res = null;

    ExprBuilder argument = convertExpr(arg);
    ExprBuilder date = convertExpr(caller);
    switch (methodName) {
      case "before":
        res = ExprMill.exprBuilder(ctx).mkLt(date, argument);
        break;

      case "after":
        res = ExprMill.exprBuilder(ctx).mkGt(date, argument);
        break;
    }
    return res;
  }

  protected ExprBuilder convertBoolStringOp(
      ASTExpression caller, ASTExpression arg, String methodName) {
    ExprBuilder res = null;
    ExprBuilder argument = convertExpr(arg);
    ExprBuilder str = convertExpr(caller);
    switch (methodName) {
      case "contains":
        res = ExprMill.exprBuilder(ctx).mkContains(str, argument);
        break;
      case "endsWith":
        res = ExprMill.exprBuilder(ctx).mkSuffixOf(argument, str);
        break;
      case "startsWith":
        res = ExprMill.exprBuilder(ctx).mkPrefixOf(argument, str);
        break;
    }
    return res;
  }

  // -----------------------------------general----------------------------------------------------------------------*/
  protected abstract ExprBuilder convert(ASTNameExpression node);

  protected abstract ExprBuilder convert(ASTFieldAccessExpression node);

  private void notFullyImplemented(ASTExpression node) {
    Log.error("conversion of Set of the type " + node.getClass().getName() + " not implemented");
  }
}
