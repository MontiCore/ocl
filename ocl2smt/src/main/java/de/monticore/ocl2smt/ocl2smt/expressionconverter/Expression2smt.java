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
     * convert method call that returns an expression*
     */
    protected Expr<? extends Sort> convertCallObject(ASTCallExpression node) {
        if (node.getExpression() instanceof ASTFieldAccessExpression) {
            ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();
            if (TypeConverter.hasStringType(caller)) {
                return convertStringOpt(node).orElse(null);
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
    protected BoolExpr convertCallBool(ASTCallExpression node) {
        BoolExpr res = null;
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
    protected SeqExpr<CharSort> convertCallerString(ASTCallExpression node) {
        if (node.getExpression() instanceof ASTFieldAccessExpression) {
            ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();
            ASTArguments arguments = node.getArguments();
            String methodName = ((ASTFieldAccessExpression) node.getExpression()).getName();

            SeqExpr<CharSort> str = convertString(caller);
            if (methodName.equals("replace")) {
                SeqExpr<CharSort> arg1 = convertString(arguments.getExpression(0));
                SeqExpr<CharSort> arg2 = convertString(arguments.getExpression(1));
                return ctx.mkReplace(str, arg1, arg2);
            }
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

    public ExprBuilder convertBoolExpr(ASTExpression node) {
        ExprBuilder result = convertExpr(node);
        if (result.isPresent() && result.isBool()) {
            return result;
        }
        notFullyImplemented(node);
        assert false;
        return  null ;
    }

    protected Optional<ExprBuilder> convertBoolExprOpt(ASTExpression node) {
        ExprBuilder result;

        if (node instanceof ASTBooleanAndOpExpression) {
            ExprBuilder left = convertExpr(((ASTBooleanAndOpExpression) node).getLeft());
            ExprBuilder right = convertExpr(((ASTBooleanAndOpExpression) node).getRight());
            result = ExprMill.exprBuilder().mkAnd(left, right);
        } else if (node instanceof ASTBooleanOrOpExpression) {
            ExprBuilder left = convertExpr(((ASTBooleanOrOpExpression) node).getLeft());
            ExprBuilder right = convertExpr(((ASTBooleanOrOpExpression) node).getRight());
            result = ExprMill.exprBuilder().mkAnd(left, right);
        } else if (node instanceof ASTBooleanNotExpression) {
            result = ExprMill.exprBuilder().mkNot(convertExpr(node));
        } else if (node instanceof ASTLogicalNotExpression) {
            result = ExprMill.exprBuilder().mkNot(convertExpr(node));
        } else if (node instanceof ASTLessEqualExpression) {
            ExprBuilder left = convertExpr(((ASTLessEqualExpression) node).getLeft());
            ExprBuilder right = convertExpr(((ASTLessEqualExpression) node).getRight());
            result = ExprMill.exprBuilder().mkLeq(left, right);
        } else if (node instanceof ASTLessThanExpression) {
            ExprBuilder left = convertExpr(((ASTLessThanExpression) node).getLeft());
            ExprBuilder right = convertExpr(((ASTLessThanExpression) node).getRight());
            result = ExprMill.exprBuilder().mkLt(left, right);
        } else if (node instanceof ASTEqualsExpression) {
            ExprBuilder left = convertExpr(((ASTEqualsExpression) node).getLeft());
            ExprBuilder right = convertExpr(((ASTEqualsExpression) node).getRight());
            result = ExprMill.exprBuilder().mkEq(left, right);
        } else if (node instanceof ASTNotEqualsExpression) {
            ExprBuilder left = convertExpr(((ASTNotEqualsExpression) node).getLeft());
            ExprBuilder right = convertExpr(((ASTNotEqualsExpression) node).getRight());
            result = ExprMill.exprBuilder().mkNeq(left, right);
        } else if (node instanceof ASTGreaterEqualExpression) {
            ExprBuilder left = convertExpr(((ASTGreaterEqualExpression) node).getLeft());
            ExprBuilder right = convertExpr(((ASTGreaterEqualExpression) node).getRight());
            result = ExprMill.exprBuilder().mkGe(left, right);
        } else if (node instanceof ASTGreaterThanExpression) {
            ExprBuilder left = convertExpr(((ASTGreaterThanExpression) node).getLeft());
            ExprBuilder right = convertExpr(((ASTGreaterThanExpression) node).getRight());
            result = ExprMill.exprBuilder().mkGt(left, right);
        } else if (node instanceof ASTImpliesExpression) {
            ExprBuilder left = convertExpr(((ASTImpliesExpression) node).getLeft());
            ExprBuilder right = convertExpr(((ASTImpliesExpression) node).getRight());
            result = ExprMill.exprBuilder().mkImplies(left, right);
        } else if (node instanceof ASTCallExpression && TypeConverter.hasBooleanType(node)) {
            result = convertCallBool((ASTCallExpression) node);
        } else if (node instanceof ASTEquivalentExpression) {
            ExprBuilder left = convertExpr(((ASTEquivalentExpression) node).getLeft());
            ExprBuilder right = convertExpr(((ASTEquivalentExpression) node).getRight());
            result = ExprMill.exprBuilder().mkEq(left, right);
        } else {
            Optional<ExprBuilder> buf = convertGenExprOpt(node);
            if (buf.isPresent() && buf.get().kind == ExpressionKind.BOOL) {
                result = buf.get();
            } else {
                return Optional.empty();
            }
        }

        return Optional.ofNullable(result);
    }

    public ExprBuilder convertExpr(ASTExpression node) {
        ExprBuilder res;
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

    protected Optional<ExprBuilder> convertExprArithOpt(ASTExpression node) {
        ExprBuilder result;
        if (node instanceof ASTMinusPrefixExpression) {
            ExprBuilder subExpr = convertExpr(((ASTMinusPrefixExpression) node).getExpression());
            result = ExprMill.exprBuilder().mkMinusPrefix(subExpr);
        } else if (node instanceof ASTPlusPrefixExpression) {
            ExprBuilder subExpr = convertExpr(((ASTPlusPrefixExpression) node).getExpression());
            result = ExprMill.exprBuilder().mkPlusPrefix(subExpr);
        } else if ((node instanceof ASTPlusExpression) && isAddition((ASTPlusExpression) node)) {
            ExprBuilder left = convertExpr(((ASTPlusExpression) node).getLeft());
            ExprBuilder right = convertExpr(((ASTPlusExpression) node).getRight());
            result = ExprMill.exprBuilder().mkPlus(left, right);
        } else if (node instanceof ASTMinusExpression) {
            ExprBuilder left = convertExpr(((ASTMinusExpression) node).getLeft());
            ExprBuilder right = convertExpr(((ASTMinusExpression) node).getRight());
            result = ExprMill.exprBuilder().mkSub(left, right);
        } else if (node instanceof ASTDivideExpression) {
            ExprBuilder left = convertExpr(((ASTDivideExpression) node).getLeft());
            ExprBuilder right = convertExpr(((ASTDivideExpression) node).getRight());
            result = ExprMill.exprBuilder().mkDiv(left, right);
        } else if (node instanceof ASTMultExpression) {
            ExprBuilder left = convertExpr(((ASTMultExpression) node).getLeft());
            ExprBuilder right = convertExpr(((ASTMultExpression) node).getRight());
            result = ExprMill.exprBuilder().mkMul(left, right);
        } else if (node instanceof ASTModuloExpression) {
            ExprBuilder left = convertExpr(((ASTModuloExpression) node).getLeft());
            ExprBuilder right = convertExpr(((ASTModuloExpression) node).getRight());
            result = ExprMill.exprBuilder().mkAnd(left, right);
        } else {
            Optional<ExprBuilder> buf = convertGenExprOpt(node);
            if (buf.isPresent() && buf.get().isArithExpr()) {
                result = buf.get();
            } else {
                return Optional.empty();
            }
        }
        return Optional.ofNullable(result);
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

    protected Optional<ExprBuilder> convertGenExprOpt(ASTExpression node) {
        ExprBuilder res;
        if (node instanceof ASTLiteralExpression) {
            res = convert((ASTLiteralExpression) node);
        } else if (node instanceof ASTBracketExpression) {
            res = convert((ASTBracketExpression) node);
        } else if (node instanceof ASTNameExpression) {
            res = convert((ASTNameExpression) node);
        } else if (node instanceof ASTFieldAccessExpression) {
            res = convert((ASTFieldAccessExpression) node);
        } else if (node instanceof ASTIfThenElseExpression) {
            ExprBuilder cond = convertExpr(((ASTIfThenElseExpression) node).getCondition());
            ExprBuilder expr1 = convertExpr(((ASTIfThenElseExpression) node).getThenExpression());
            ExprBuilder expr2 = convertExpr(((ASTIfThenElseExpression) node).getElseExpression());
            res = ExprMill.exprBuilder().mkIte(cond, expr1, expr2);


        } else if (node instanceof ASTConditionalExpression) {
            ExprBuilder cond = convertExpr(((ASTConditionalExpression) node).getCondition());
            ExprBuilder expr1 = convertExpr(((ASTConditionalExpression) node).getTrueExpression());
            ExprBuilder expr2 = convertExpr(((ASTConditionalExpression) node).getFalseExpression());
            res = ExprMill.exprBuilder().mkIte(cond, expr1, expr2);
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

    protected BoolExpr convertBoolStringOp(ASTExpression caller, ASTExpression arg, String methodName) {
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


    // -----------------------------------general----------------------------------------------------------------------*/
    protected abstract ExprBuilder convert(ASTNameExpression node);

    protected ExprBuilder convert(ASTBracketExpression node) {
        return convertExpr(node.getExpression());
    }

    protected abstract ExprBuilder convert(ASTFieldAccessExpression node);

    private boolean isAddition(ASTPlusExpression node) {
        return (convertExpr(node.getLeft()).isArithExpr() || convertExpr(node.getLeft()).isArithExpr());
    }

    private boolean isStringConcat(ASTPlusExpression node) {
        return convertExpr((node).getLeft()).isString();
    }

    private void notFullyImplemented(ASTExpression node) {
        Log.error("conversion of Set of the type " + node.getClass().getName() + " not implemented");
    }

    public ExprBuilder convert(ASTLiteralExpression node) {
        ASTLiteral literal = node.getLiteral();
        ExprBuilder res = null;
        if (literal instanceof ASTBooleanLiteral) {
            res = ExprMill.exprBuilder().mkBool((ASTBooleanLiteral) literal);
        } else if (literal instanceof ASTStringLiteral) {
            res = ExprMill.exprBuilder().mkString((ASTStringLiteral) literal);
        } else if (literal instanceof ASTNatLiteral) {
            res = ExprMill.exprBuilder().mkInt((ASTNatLiteral) literal);
        } else if (literal instanceof ASTBasicDoubleLiteral) {
            res = ExprMill.exprBuilder().mkDouble((ASTBasicDoubleLiteral) literal);
        } else if (literal instanceof ASTCharLiteral) {
            res = ExprMill.exprBuilder().mkChar((ASTCharLiteral) literal);
        } else {
            Log.error("the conversion of expression with the type " + node.getClass().getName() + "in SMT is not totally implemented");
        }
        return res;
    }
}
