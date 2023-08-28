package de.monticore.ocl2smt.ocl2smt.expressionconverter;

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
import java.util.Optional;

public abstract class Expression2smt {

  protected Context ctx;

  /**
   * ++++++++++++++++++++++++++++++++++++++++++literal-expressions++++++++++++++++++++++++++++++++++++++++++++++++++++++
   */
  protected BoolExpr convert(ASTBooleanLiteral node) {
    return ctx.mkBool(node.getValue());
  }

  protected SeqExpr<CharSort> convert(ASTStringLiteral node) {
    return ctx.mkString(node.getValue());
  }

  protected IntNum convert(ASTNatLiteral node) {
    return ctx.mkInt(node.getValue());
  }

  protected IntNum convert(ASTCharLiteral node) {
    return ctx.mkInt(node.getValue());
  }

  protected FPNum convert(ASTBasicDoubleLiteral node) {
    return ctx.mkFP(node.getValue(), ctx.mkFPSortDouble());
  }

  /**
   * ++++++++++++++++++++++++++++++++++++++++++logical-expressions++++++++++++++++++++++++++++++++++++++++++++++++++++++
   */
  protected BoolExpr convert(ASTBooleanNotExpression node) {
    return ctx.mkNot(convertBoolExpr(node.getExpression()));
  }

  protected BoolExpr convert(ASTLogicalNotExpression node) {
    return ctx.mkNot(convertBoolExpr(node.getExpression()));
  }

  protected BoolExpr convert(ASTBooleanAndOpExpression node) {
    return ctx.mkAnd(convertBoolExpr(node.getLeft()), convertBoolExpr(node.getRight()));
  }

  protected BoolExpr convert(ASTBooleanOrOpExpression node) {
    return ctx.mkOr(convertBoolExpr(node.getLeft()), convertBoolExpr(node.getRight()));
  }

  protected BoolExpr convert(ASTEquivalentExpression node) {
    return ctx.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight()));
  }

  protected BoolExpr convert(ASTImpliesExpression node) {
    return ctx.mkImplies(convertBoolExpr(node.getLeft()), convertBoolExpr(node.getRight()));
  }

  /**
   * ++++++++++++++++++++++++++++++++++++++++++comparison-expressions++++++++++++++++++++++++++++++++++++++++++++++++++++++
   */
  protected BoolExpr convert(ASTLessThanExpression node) {
    return ctx.mkLt(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convert(ASTLessEqualExpression node) {
    return ctx.mkLe(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convert(ASTGreaterThanExpression node) {
    return ctx.mkGt(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convert(ASTGreaterEqualExpression node) {
    return ctx.mkGe(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convert(ASTEqualsExpression node) {
    return ctx.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight()));
  }

  protected BoolExpr convert(ASTNotEqualsExpression node) {
    return ctx.mkNot(ctx.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight())));
  }

  /**
   * ++++++++++++++++++++++++++++++++++++++++++arithmetic-expressions++++++++++++++++++++++++++++++++++++++++++++++++++++++
   */
  protected ArithExpr<? extends ArithSort> convert(ASTMinusPrefixExpression node) {
    return ctx.mkMul(ctx.mkInt(-1), convertExprArith(node.getExpression()));
  }

  protected ArithExpr<? extends ArithSort> convert(ASTPlusPrefixExpression node) {
    return convertExprArith(node.getExpression());
  }

  protected ArithExpr<? extends ArithSort> convert(ASTMultExpression node) {
    return ctx.mkMul(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected ArithExpr<? extends ArithSort> convert(ASTDivideExpression node) {
    return ctx.mkDiv(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected IntExpr convert(ASTModuloExpression node) {
    return ctx.mkMod(
        (IntExpr) convertExprArith(node.getLeft()), (IntExpr) convertExprArith(node.getRight()));
  }

  protected ArithExpr<? extends ArithSort> convertPlus(ASTPlusExpression node) {
    return ctx.mkAdd(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected ArithExpr<ArithSort> convert(ASTMinusExpression node) {
    return ctx.mkSub(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  /***
   * ++++++++++++++++++++++++++++++++++++++++++method-call-expressions++++++++++++++++++++++++++++++++++++++++++++++++++
   */

  /** convert method call that returns an expression* */
  protected Expr<? extends Sort> convertCallObject(ASTCallExpression node) {
    ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();
    if (TypeConverter.hasStringType(caller)) {
      return convertStringOpt(node).orElse(null);
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
  protected BoolExpr convertMethodCallBool(ASTCallExpression node) {
    BoolExpr res = null;
    ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();
    String methodName = ((ASTFieldAccessExpression) node.getExpression()).getName();
    if (TypeConverter.hasStringType(caller)) {
      res = convertBoolStringOp(caller, node.getArguments().getExpression(0), methodName);
    } else if (TypeConverter.hasDateType(caller)) {
      res = convertBoolDateOp(caller, node.getArguments().getExpression(0), methodName);
    }

    return res;
  }
  /***
   * convert method call when the caller is a string and the method return an object
   * String.replace(x,y)
   */
  protected SeqExpr<CharSort> convertCallerString(ASTCallExpression node) {
    ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();
    ASTArguments arguments = node.getArguments();
    String methodName = ((ASTFieldAccessExpression) node.getExpression()).getName();

    SeqExpr<CharSort> str = convertString(caller);
    if (methodName.equals("replace")) {
      SeqExpr<CharSort> arg1 = convertString(arguments.getExpression(0));
      SeqExpr<CharSort> arg2 = convertString(arguments.getExpression(1));
      return ctx.mkReplace(str, arg1, arg2);
    }

    return null;
  }

  protected SeqExpr<CharSort> convertString(ASTExpression node) {
    Optional<SeqExpr<CharSort>> result = convertStringOpt(node);
    if (result.isEmpty()) {
      notFullyImplemented(node);
      assert false;
    }
    return result.get();
  }

  public BoolExpr convertBoolExpr(ASTExpression node) {
    Optional<BoolExpr> result = convertBoolExprOpt(node);
    if (result.isEmpty()) {
      notFullyImplemented(node);
      assert false;
    }
    return result.get();
  }

  protected Optional<BoolExpr> convertBoolExprOpt(ASTExpression node) {
    BoolExpr result;

    if (node instanceof ASTBooleanAndOpExpression) {
      result = convert((ASTBooleanAndOpExpression) node);
    } else if (node instanceof ASTBooleanOrOpExpression) {
      result = convert((ASTBooleanOrOpExpression) node);
    } else if (node instanceof ASTBooleanNotExpression) {
      result = convert((ASTBooleanNotExpression) node);
    } else if (node instanceof ASTLogicalNotExpression) {
      result = convert((ASTLogicalNotExpression) node);
    } else if (node instanceof ASTLessEqualExpression) {
      result = convert((ASTLessEqualExpression) node);
    } else if (node instanceof ASTLessThanExpression) {
      result = convert((ASTLessThanExpression) node);
    } else if (node instanceof ASTEqualsExpression) {
      result = convert((ASTEqualsExpression) node);
    } else if (node instanceof ASTNotEqualsExpression) {
      result = convert((ASTNotEqualsExpression) node);
    } else if (node instanceof ASTGreaterEqualExpression) {
      result = convert((ASTGreaterEqualExpression) node);
    } else if (node instanceof ASTGreaterThanExpression) {
      result = convert((ASTGreaterThanExpression) node);
    } else if (node instanceof ASTImpliesExpression) {
      result = convert((ASTImpliesExpression) node);
    } else if (node instanceof ASTCallExpression && TypeConverter.hasBooleanType(node)) {
      result = convertMethodCallBool((ASTCallExpression) node);
    } else if (node instanceof ASTEquivalentExpression) {
      result = convert((ASTEquivalentExpression) node);
    } else {
      Optional<Expr<? extends Sort>> buf = convertGenExprOpt(node);
      if (buf.isPresent() && buf.get() instanceof BoolExpr) {
        result = (BoolExpr) buf.get();
      } else {
        return Optional.empty();
      }
    }

    return Optional.of(result);
  }

  public Expr<? extends Sort> convertExpr(ASTExpression node) {
    Expr<? extends Sort> res;
    res = convertGenExprOpt(node).orElse(null);
    if (res == null) {
      res = convertBoolExprOpt(node).orElse(null);
      if (res == null) {
        res = convertExprArithOpt(node).orElse(null);
        if (res == null) {
          res = convertString(node);
        }
      }
    }
    return res;
  }

  protected Optional<ArithExpr<? extends ArithSort>> convertExprArithOpt(ASTExpression node) {
    ArithExpr<? extends ArithSort> result;
    if (node instanceof ASTMinusPrefixExpression) {
      result = convert((ASTMinusPrefixExpression) node);
    } else if (node instanceof ASTPlusPrefixExpression) {
      result = convert((ASTPlusPrefixExpression) node);
    } else if ((node instanceof ASTPlusExpression) && isAddition((ASTPlusExpression) node)) {
      result = convertPlus((ASTPlusExpression) node);
    } else if (node instanceof ASTMinusExpression) {
      result = convert((ASTMinusExpression) node);
    } else if (node instanceof ASTDivideExpression) {
      result = convert((ASTDivideExpression) node);
    } else if (node instanceof ASTMultExpression) {
      result = convert((ASTMultExpression) node);
    } else if (node instanceof ASTModuloExpression) {
      result = convert((ASTModuloExpression) node);
    } else {
      Optional<Expr<? extends Sort>> buf = convertGenExprOpt(node);
      if (buf.isPresent() && buf.get() instanceof ArithExpr) {
        result = (ArithExpr<? extends ArithSort>) buf.get();
      } else {
        return Optional.empty();
      }
    }
    return Optional.of(result);
  }

  protected Optional<SeqExpr<CharSort>> convertStringOpt(ASTExpression node) {
    SeqExpr<CharSort> result;

    if ((node instanceof ASTPlusExpression && isStringConcat((ASTPlusExpression) node))) {
      result = convertStringConcat((ASTPlusExpression) node);
    } else if (node instanceof ASTCallExpression && TypeConverter.hasStringType(node)) {
      result = convertCallerString((ASTCallExpression) node);
    } else {
      Optional<Expr<? extends Sort>> buf = convertGenExprOpt(node);
      if (buf.isPresent() && buf.get().getSort().getName().isStringSymbol()) {
        result = (SeqExpr<CharSort>) buf.get();
      } else {
        return Optional.empty();
      }
    }
    return Optional.of(result);
  }

  protected ArithExpr<? extends ArithSort> convertExprArith(ASTExpression node) {
    Optional<ArithExpr<? extends ArithSort>> result = convertExprArithOpt(node);
    if (result.isEmpty()) {
      notFullyImplemented(node);
      assert false;
    }
    return result.get();
  }

  protected Optional<Expr<? extends Sort>> convertGenExprOpt(ASTExpression node) {
    Expr<? extends Sort> res;
    if (node instanceof ASTLiteralExpression) {
      res = convert((ASTLiteralExpression) node);
    } else if (node instanceof ASTBracketExpression) {
      res = convert((ASTBracketExpression) node);
    } else if (node instanceof ASTNameExpression) {
      res = convert((ASTNameExpression) node);
    } else if (node instanceof ASTFieldAccessExpression) {
      res = convert((ASTFieldAccessExpression) node);
    } else if (node instanceof ASTIfThenElseExpression) {
      res = convert((ASTIfThenElseExpression) node);
    } else if (node instanceof ASTConditionalExpression) {
      res = convert((ASTConditionalExpression) node);
    } else if (node instanceof ASTCallExpression) {
      res = convertCallObject((ASTCallExpression) node);
    } else {
      return Optional.empty();
    }
    return Optional.of(res);
  }

  // -----------String--------------
  protected SeqExpr<CharSort> convertStringConcat(ASTPlusExpression node) {
    return ctx.mkConcat(convertString(node.getLeft()), convertString(node.getRight()));
  }

  // ---------------------------------------Logic---------------------------------

  protected BoolExpr convertBoolDateOp(ASTExpression caller, ASTExpression arg, String methodName) {
    BoolExpr res = null;

    ArithExpr<? extends ArithSort> argument = convertExprArith(arg);
    ArithExpr<? extends ArithSort> date = convertExprArith(caller);
    switch (methodName) {
      case "before":
        res = ctx.mkLt(date, argument);
        break;

      case "after":
        res = ctx.mkGt(date, argument);
        break;
    }
    return res;
  }

  protected BoolExpr convertBoolStringOp(
      ASTExpression caller, ASTExpression arg, String methodName) {
    BoolExpr res = null;
    SeqExpr<CharSort> argument = convertString(arg);
    SeqExpr<CharSort> str = convertString(caller);
    switch (methodName) {
      case "contains":
        res = ctx.mkContains(str, argument);
        break;
      case "endsWith":
        res = ctx.mkSuffixOf(argument, str);
        break;
      case "startsWith":
        res = ctx.mkPrefixOf(argument, str);
        break;
    }
    return res;
  }

  protected Expr<? extends Sort> convert(ASTIfThenElseExpression node) {
    return ctx.mkITE(
        convertBoolExpr(node.getCondition()),
        convertExpr(node.getThenExpression()),
        convertExpr(node.getElseExpression()));
  }

  protected Expr<? extends Sort> convert(ASTConditionalExpression node) {
    return ctx.mkITE(
        convertBoolExpr(node.getCondition()),
        convertExpr(node.getTrueExpression()),
        convertExpr(node.getFalseExpression()));
  }

  // -----------------------------------general----------------------------------------------------------------------*/
  protected abstract Expr<? extends Sort> convert(ASTNameExpression node);

  protected Expr<? extends Sort> convert(ASTBracketExpression node) {
    return convertExpr(node.getExpression());
  }

  protected abstract Expr<? extends Sort> convert(ASTFieldAccessExpression node);

  private boolean isAddition(ASTPlusExpression node) {
    return (convertExpr(node.getLeft()).isInt() || convertExpr(node.getLeft()).isReal());
  }

  private boolean isStringConcat(ASTPlusExpression node) {
    return convertExpr((node).getLeft()).getSort().getName().isStringSymbol();
  }

  private void notFullyImplemented(ASTExpression node) {
    Log.error("conversion of Set of the type " + node.getClass().getName() + " not implemented");
  }

  public Expr<? extends Sort> convert(ASTLiteralExpression node) {
    ASTLiteral literal = node.getLiteral();
    Expr<? extends Sort> res = null;
    if (literal instanceof ASTBooleanLiteral) {
      res = convert((ASTBooleanLiteral) literal);
    } else if (literal instanceof ASTStringLiteral) {
      res = convert((ASTStringLiteral) literal);
    } else if (literal instanceof ASTNatLiteral) {
      return convert((ASTNatLiteral) literal);
    } else if (literal instanceof ASTBasicDoubleLiteral) {
      res = convert((ASTBasicDoubleLiteral) literal);
    } else if (literal instanceof ASTCharLiteral) {
      res = convert((ASTCharLiteral) literal);
    } else {
      Log.error(
          "the conversion of expression with the type "
              + node.getClass().getName()
              + "in SMT is not totally implemented");
    }
    return res;
  }
}
