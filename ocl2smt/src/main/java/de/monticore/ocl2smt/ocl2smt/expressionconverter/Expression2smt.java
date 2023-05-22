package de.monticore.ocl2smt.ocl2smt.expressionconverter;

import com.microsoft.z3.*;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.literals.mccommonliterals._ast.*;
import de.monticore.literals.mcliteralsbasis._ast.ASTLiteral;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl2smt.util.TypeConverter;
import de.se_rwth.commons.logging.Log;
import java.util.*;

public abstract class Expression2smt {

  protected Context ctx;

  public BoolExpr convertBoolExpr(ASTExpression node) {
    Optional<BoolExpr> result = convertBoolExprOpt(node);
    if (result.isEmpty()) {
      notFullyImplemented(node);
      assert false;
    }
    return result.get();
  }

  public Expr<? extends Sort> convertExpr(ASTExpression node) {
    Expr<? extends Sort> res;
    res = convertGenExprOpt(node).orElse(null);
    if (res == null) {
      res = convertBoolExprOpt(node).orElse(null);
      if (res == null) {
        res = convertExprArithOpt(node).orElse(null);
        if (res == null) {
          res = convertExprString(node);
        }
      }
    }
    return res;
  }

  protected Optional<BoolExpr> convertBoolExprOpt(ASTExpression node) {
    BoolExpr result;
    if (node instanceof ASTBooleanAndOpExpression) {
      result = convertAndBool((ASTBooleanAndOpExpression) node);
    } else if (node instanceof ASTBooleanOrOpExpression) {
      result = convertORBool((ASTBooleanOrOpExpression) node);
    } else if (node instanceof ASTBooleanNotExpression) {
      result = convertNotBool((ASTBooleanNotExpression) node);
    } else if (node instanceof ASTLogicalNotExpression) {
      result = convertNotBool((ASTLogicalNotExpression) node);
    } else if (node instanceof ASTLessEqualExpression) {
      result = convertLEq((ASTLessEqualExpression) node);
    } else if (node instanceof ASTLessThanExpression) {
      result = convertLThan((ASTLessThanExpression) node);
    } else if (node instanceof ASTEqualsExpression) {
      result = convertEq((ASTEqualsExpression) node);
    } else if (node instanceof ASTNotEqualsExpression) {
      result = convertNEq((ASTNotEqualsExpression) node);
    } else if (node instanceof ASTGreaterEqualExpression) {
      result = convertGEq((ASTGreaterEqualExpression) node);
    } else if (node instanceof ASTGreaterThanExpression) {
      result = convertGT((ASTGreaterThanExpression) node);
    } else if (node instanceof ASTImpliesExpression) {
      result = convertImpl((ASTImpliesExpression) node);
    } else if (node instanceof ASTCallExpression && methodReturnsBool((ASTCallExpression) node)) {
      result = convertBoolMethodCall((ASTCallExpression) node);
    } else if (node instanceof ASTEquivalentExpression) {
      result = convertEquiv((ASTEquivalentExpression) node);
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

  protected Optional<ArithExpr<? extends ArithSort>> convertExprArithOpt(ASTExpression node) {
    ArithExpr<? extends ArithSort> result;
    if (node instanceof ASTMinusPrefixExpression) {
      result = convertMinPref((ASTMinusPrefixExpression) node);
    } else if (node instanceof ASTPlusPrefixExpression) {
      result = convertPlusPref((ASTPlusPrefixExpression) node);
    } else if ((node instanceof ASTPlusExpression) && isAddition((ASTPlusExpression) node)) {
      result = convertPlus((ASTPlusExpression) node);
    } else if (node instanceof ASTMinusExpression) {
      result = convertMinus((ASTMinusExpression) node);
    } else if (node instanceof ASTDivideExpression) {
      result = convertDiv((ASTDivideExpression) node);
    } else if (node instanceof ASTMultExpression) {
      result = convertMul((ASTMultExpression) node);
    } else if (node instanceof ASTModuloExpression) {
      result = convertMod((ASTModuloExpression) node);
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

  protected Optional<SeqExpr<CharSort>> convertExprStringOpt(ASTExpression node) {
    SeqExpr<CharSort> result;

    if ((node instanceof ASTPlusExpression && isStringConcat((ASTPlusExpression) node))) {
      result = convertStringConcat((ASTPlusExpression) node);
    } else if (node instanceof ASTCallExpression
        && methodReturnsString((ASTCallExpression) node)
        && ((ASTCallExpression) node).getExpression() instanceof ASTFieldAccessExpression) {
      result = convertStringMethodCall((ASTCallExpression) node);
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

  protected SeqExpr<CharSort> convertExprString(ASTExpression node) {
    //  Log.info("I have got a " + node.getClass().getName(), this.getClass().getName());
    Optional<SeqExpr<CharSort>> result = convertExprStringOpt(node);
    if (result.isEmpty()) {
      notFullyImplemented(node);
      assert false;
    }
    return result.get();
  }

  protected SeqExpr<CharSort> convertStringMethodCall(ASTCallExpression node) {
    SeqExpr<CharSort> res = null;
    if (node.getDefiningSymbol().isPresent()) {
      String name = node.getDefiningSymbol().get().getName();
      if (node.getExpression() instanceof ASTFieldAccessExpression) {
        SeqExpr<CharSort> arg1 = convertExprString(node.getArguments().getExpression(0));
        SeqExpr<CharSort> arg2 = convertExprString(node.getArguments().getExpression(1));
        SeqExpr<CharSort> str =
            convertExprString(((ASTFieldAccessExpression) node.getExpression()).getExpression());
        if ("replace".equals(name)) {
          res = ctx.mkReplace(str, arg1, arg2);
        }
      }
      return res;
    }
    notFullyImplemented(node);
    return null;
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
      res = convertBracket((ASTBracketExpression) node);
    } else if (node instanceof ASTNameExpression) {
      res = convertName((ASTNameExpression) node);
    } else if (node instanceof ASTFieldAccessExpression) {
      res = convertFieldAcc((ASTFieldAccessExpression) node);
    } else if (node instanceof ASTIfThenElseExpression) {
      res = convertIfTEl((ASTIfThenElseExpression) node);
    } else if (node instanceof ASTConditionalExpression) {
      res = convertCond((ASTConditionalExpression) node);
    } else {
      return Optional.empty();
    }
    return Optional.of(res);
  }

  // -----------String--------------
  protected SeqExpr<CharSort> convertStringConcat(ASTPlusExpression node) {
    return ctx.mkConcat(convertExprString(node.getLeft()), convertExprString(node.getRight()));
  }

  // ----------------------Arit--------------------
  protected ArithExpr<? extends ArithSort> convertMinPref(ASTMinusPrefixExpression node) {
    return ctx.mkMul(ctx.mkInt(-1), convertExprArith(node.getExpression()));
  }

  protected ArithExpr<? extends ArithSort> convertPlusPref(ASTPlusPrefixExpression node) {
    return convertExprArith(node.getExpression());
  }

  protected ArithExpr<? extends ArithSort> convertMul(ASTMultExpression node) {
    return ctx.mkMul(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected ArithExpr<? extends ArithSort> convertDiv(ASTDivideExpression node) {
    return ctx.mkDiv(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected IntExpr convertMod(ASTModuloExpression node) {
    return ctx.mkMod(
        (IntExpr) convertExprArith(node.getLeft()), (IntExpr) convertExprArith(node.getRight()));
  }

  protected ArithExpr<? extends ArithSort> convertPlus(ASTPlusExpression node) {
    return ctx.mkAdd(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected ArithExpr<ArithSort> convertMinus(ASTMinusExpression node) {
    return ctx.mkSub(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }
  // ---------------------------------------Logic---------------------------------

  protected BoolExpr convertNotBool(ASTBooleanNotExpression node) {
    return ctx.mkNot(convertBoolExpr(node.getExpression()));
  }

  protected BoolExpr convertNotBool(ASTLogicalNotExpression node) {
    return ctx.mkNot(convertBoolExpr(node.getExpression()));
  }

  protected BoolExpr convertAndBool(ASTBooleanAndOpExpression node) {
    return ctx.mkAnd(convertBoolExpr(node.getLeft()), convertBoolExpr(node.getRight()));
  }

  protected BoolExpr convertORBool(ASTBooleanOrOpExpression node) {
    return ctx.mkOr(convertBoolExpr(node.getLeft()), convertBoolExpr(node.getRight()));
  }

  // --------------------------comparison----------------------------------------------
  protected BoolExpr convertLThan(ASTLessThanExpression node) {
    return ctx.mkLt(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convertLEq(ASTLessEqualExpression node) {
    return ctx.mkLe(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convertGT(ASTGreaterThanExpression node) {
    return ctx.mkGt(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convertGEq(ASTGreaterEqualExpression node) {
    return ctx.mkGe(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convertEq(ASTEqualsExpression node) {
    return ctx.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight()));
  }

  protected BoolExpr convertNEq(ASTNotEqualsExpression node) {
    return ctx.mkNot(ctx.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight())));
  }

  protected BoolExpr convertEquiv(ASTEquivalentExpression node) {
    return ctx.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight()));
  }

  protected BoolExpr convertBoolMethodCall(ASTCallExpression node) {
    BoolExpr res = null;
    if (node.getDefiningSymbol().isPresent()) {
      String name = node.getDefiningSymbol().get().getName();
      if (node.getExpression() instanceof ASTFieldAccessExpression) {
        ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();
        if (TypeConverter.isString(caller)) {
          res = convertBoolStringOp(caller, node.getArguments().getExpression(0), name);
        } else if (TypeConverter.isDate(caller)) {
          res = convertBoolDateOp(caller, node.getArguments().getExpression(0), name);
        }
      }
      return res;
    }
    notFullyImplemented(node);
    return null;
  }

  protected BoolExpr convertImpl(ASTImpliesExpression node) {
    return ctx.mkImplies(convertBoolExpr(node.getLeft()), convertBoolExpr(node.getRight()));
  }

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
    SeqExpr<CharSort> argument = convertExprString(arg);
    SeqExpr<CharSort> str = convertExprString(caller);
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

  /*------------------------------------quantified expressions----------------------------------------------------------*/

  /*----------------------------------control expressions----------------------------------------------------------*/
  protected Expr<? extends Sort> convertIfTEl(ASTIfThenElseExpression node) {
    return ctx.mkITE(
        convertBoolExpr(node.getCondition()),
        convertExpr(node.getThenExpression()),
        convertExpr(node.getElseExpression()));
  }

  protected Expr<? extends Sort> convertCond(ASTConditionalExpression node) {
    return ctx.mkITE(
        convertBoolExpr(node.getCondition()),
        convertExpr(node.getTrueExpression()),
        convertExpr(node.getFalseExpression()));
  }

  // -----------------------------------general----------------------------------------------------------------------*/
  protected abstract Expr<? extends Sort> convertName(ASTNameExpression node);

  protected Expr<? extends Sort> convertBracket(ASTBracketExpression node) {
    return convertExpr(node.getExpression());
  }

  protected abstract Expr<? extends Sort> convertFieldAcc(ASTFieldAccessExpression node);

  // a.auction**

  private boolean methodReturnsBool(ASTCallExpression node) {
    if (node.getDefiningSymbol().isPresent()) {
      String name = node.getDefiningSymbol().get().getName();
      return (Set.of(
              "contains",
              "endsWith",
              "startsWith",
              "before",
              "after",
              "containsAll",
              "isEmpty",
              "isPresent")
          .contains(name));
    }
    return false;
  }

  private boolean methodReturnsString(ASTCallExpression node) {
    if (node.getDefiningSymbol().isPresent()) {
      String name = node.getDefiningSymbol().get().getName();
      return (name.equals("replace"));
    }
    return false;
  }

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
      res = convertBool((ASTBooleanLiteral) literal);
    } else if (literal instanceof ASTStringLiteral) {
      res = convertString((ASTStringLiteral) literal);
    } else if (literal instanceof ASTNatLiteral) {
      return convertNat((ASTNatLiteral) literal);
    } else if (literal instanceof ASTBasicDoubleLiteral) {
      res = convertDouble((ASTBasicDoubleLiteral) literal);
    } else if (literal instanceof ASTCharLiteral) {
      res = convertChar((ASTCharLiteral) literal);
    } else {
      Log.error(
          "the conversion of expression with the type "
              + node.getClass().getName()
              + "in SMT is not totally implemented");
    }
    return res;
  }

  protected BoolExpr convertBool(ASTBooleanLiteral node) {
    return ctx.mkBool(node.getValue());
  }

  protected SeqExpr<CharSort> convertString(ASTStringLiteral node) {
    return ctx.mkString(node.getValue());
  }

  protected IntNum convertNat(ASTNatLiteral node) {
    return ctx.mkInt(node.getValue());
  }

  protected IntNum convertChar(ASTCharLiteral node) {
    return ctx.mkInt(node.getValue());
  }

  protected FPNum convertDouble(ASTBasicDoubleLiteral node) {
    return ctx.mkFP(node.getValue(), ctx.mkFPSortDouble());
  }
}
