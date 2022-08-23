package de.monticore.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.ocl.ocl._ast.*;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;

public class OCL2SMTGenerator {
    protected final Context context;
    protected final LiteralExpressionsConverter literalExpressionsConverter;

    public OCL2SMTGenerator(Context context) {
        this.context = context;
        this.literalExpressionsConverter = new LiteralExpressionsConverter(context);
    }

    public List<BoolExpr> ocl2smt(ASTOCLArtifact astoclArtifact) {
        List<BoolExpr> constraints = new ArrayList<>();
        for (ASTOCLConstraint constraint : astoclArtifact.getOCLConstraintList()) {
            constraints.add(convertConstr(constraint));
        }
        return constraints;
    }

    protected BoolExpr convertConstr(ASTOCLConstraint constraint) {
        if (constraint instanceof ASTOCLInvariant) {
            return convertInv((ASTOCLInvariant) constraint);
        } else {
            assert false;
            Log.error("the conversion of  ASTOCLConstraint of type   ASTOCLMethodSignature " +
                    "and ASTOCLConstructorSignature in SMT is not implemented");
            return null;
        }
    }

    protected BoolExpr convertInv(ASTOCLInvariant invariant) {
        return (BoolExpr) convertExpr(invariant.getExpression());
    }

    protected Expr convertExpr(ASTExpression node) {
        if (node instanceof ASTLiteralExpression) {
            return literalExpressionsConverter.convert((ASTLiteralExpression) node);
        } else if (node instanceof ASTMinusPrefixExpression) {
            return convertMinPref((ASTMinusPrefixExpression) node);
        } else if (node instanceof ASTPlusPrefixExpression) {
            return convertPlusPref((ASTPlusPrefixExpression) node);
        } else if (node instanceof ASTBooleanAndOpExpression) {
            return convertAndBool((ASTBooleanAndOpExpression) node);
        } else if (node instanceof ASTBooleanOrOpExpression) {
            return convertORBool((ASTBooleanOrOpExpression) node);
        } else if (node instanceof ASTBooleanNotExpression) {
            return convertNotBool((ASTBooleanNotExpression) node);
        } else if (node instanceof ASTLogicalNotExpression) {
            return convertNotBool((ASTLogicalNotExpression) node);
        } else if (node instanceof ASTPlusExpression) {
            return convertPlus((ASTPlusExpression) node);
        } else if (node instanceof ASTMinusExpression) {
            return convertMinus((ASTMinusExpression) node);
        } else if (node instanceof ASTDivideExpression) {
            return convertDiv((ASTDivideExpression) node);
        } else if (node instanceof ASTMultExpression) {
            return convertMul((ASTMultExpression) node);
        } else if (node instanceof ASTModuloExpression) {
            return convertMod((ASTModuloExpression) node);
        } else if (node instanceof ASTLessEqualExpression) {
            return convertLEq((ASTLessEqualExpression) node);
        } else if (node instanceof ASTLessThanExpression) {
            return convertLThan((ASTLessThanExpression) node);
        } else if (node instanceof ASTEqualsExpression) {
            return convertEq((ASTEqualsExpression) node);
        } else if (node instanceof ASTNotEqualsExpression) {
            return convertNEq((ASTNotEqualsExpression) node);
        } else if (node instanceof ASTGreaterEqualExpression) {
            return convertGEq((ASTGreaterEqualExpression) node);
        } else if (node instanceof ASTGreaterThanExpression) {
            return convertGT((ASTGreaterThanExpression) node);
        } else {
            assert false;
            Log.error("the conversion of expressions with the type " + node.getClass().getName() + "is   not totally  implemented");
            return null;
        }
    }

    //--------------------------------------Arithmetic -----------------------------------------------
    protected ArithExpr<ArithSort> convertMinPref(ASTMinusPrefixExpression node) {
        return context.mkMul(context.mkInt(-1), convertExpr(node.getExpression()));
    }

    protected ArithExpr<ArithSort> convertPlusPref(ASTPlusPrefixExpression node) {
        return (ArithExpr<ArithSort>) convertExpr(node.getExpression());
    }

    protected ArithExpr<ArithSort> convertMul(ASTMultExpression node) {
        return context.mkMul(convertExpr(node.getLeft()), convertExpr(node.getRight()));
    }

    protected ArithExpr<ArithSort> convertDiv(ASTDivideExpression node) {
        return context.mkDiv(convertExpr(node.getLeft()), convertExpr(node.getRight()));
    }

    protected IntExpr convertMod(ASTModuloExpression node) {
        return context.mkMod(convertExpr(node.getLeft()), convertExpr(node.getRight()));
    }

    protected ArithExpr<ArithSort> convertPlus(ASTPlusExpression node) {
        return context.mkAdd(convertExpr(node.getLeft()), convertExpr(node.getRight()));
    }

    protected ArithExpr<ArithSort> convertMinus(ASTMinusExpression node) {
        return context.mkSub(convertExpr(node.getLeft()), convertExpr(node.getRight()));
    }
//---------------------------------------Logic---------------------------------

    protected BoolExpr convertNotBool(ASTBooleanNotExpression node) {
        return context.mkNot(convertExpr(node.getExpression()));
    }

    protected BoolExpr convertNotBool(ASTLogicalNotExpression node) {
        return context.mkNot(convertExpr(node.getExpression()));
    }

    protected BoolExpr convertAndBool(ASTBooleanAndOpExpression node) {
        return context.mkAnd(convertExpr(node.getLeft()), convertExpr(node.getRight()));
    }

    protected BoolExpr convertORBool(ASTBooleanOrOpExpression node) {
        return context.mkOr(convertExpr(node.getLeft()), convertExpr(node.getRight()));
    }

    //--------------------------comparison----------------------------------------------
    protected BoolExpr convertLThan(ASTLessThanExpression node) {
        return context.mkLt(convertExpr(node.getLeft()), convertExpr(node.getRight()));
    }

    protected BoolExpr convertLEq(ASTLessEqualExpression node) {
        return context.mkLe(convertExpr(node.getLeft()), convertExpr(node.getRight()));
    }

    protected BoolExpr convertGT(ASTGreaterThanExpression node) {
        return context.mkGt(convertExpr(node.getLeft()), convertExpr(node.getRight()));
    }

    protected BoolExpr convertGEq(ASTGreaterEqualExpression node) {
        return context.mkGe(convertExpr(node.getLeft()), convertExpr(node.getRight()));
    }

    protected BoolExpr convertEq(ASTEqualsExpression node) {
        return context.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight()));
    }

    protected BoolExpr convertNEq(ASTNotEqualsExpression node) {
        return context.mkNot(context.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight())));
    }
}
