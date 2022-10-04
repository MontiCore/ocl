package de.monticore.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.literals.mccommonliterals._ast.ASTBasicDoubleLiteral;
import de.monticore.literals.mccommonliterals._ast.ASTBooleanLiteral;
import de.monticore.literals.mccommonliterals._ast.ASTNatLiteral;
import de.monticore.literals.mccommonliterals._ast.ASTStringLiteral;
import de.monticore.literals.mcliteralsbasis._ast.ASTLiteral;
import de.se_rwth.commons.logging.Log;

public class SetExpressionConverter {
    protected final Context context;

    public SetExpressionConverter(Context context) {
        this.context = context;
    }

    public Expr<? extends Sort> convert(ASTLiteralExpression node) {
        ASTLiteral literal = node.getLiteral();
        if (literal instanceof ASTBooleanLiteral) {
            return convertBool((ASTBooleanLiteral) literal);
        } else if (literal instanceof ASTStringLiteral) {
            return convertString((ASTStringLiteral) literal);
        } else if (literal instanceof ASTNatLiteral) {
            return convertNat((ASTNatLiteral) literal);
        } else if (literal instanceof ASTBasicDoubleLiteral) {
            return convertDouble((ASTBasicDoubleLiteral) literal);
        } else {
            assert false;
            Log.error("the conversion of expression with the type " + node.getClass().getName() + "in SMT is not totally implemented");
            return null;
        }

    }

    protected BoolExpr convertBool(ASTBooleanLiteral node) {
        return context.mkBool(node.getValue());
    }

    protected SeqExpr<CharSort> convertString(ASTStringLiteral node) {
        return context.mkString(node.getValue());
    }

    protected IntNum convertNat(ASTNatLiteral node) {
        return context.mkInt(node.getValue());
    }

    protected FPNum convertDouble(ASTBasicDoubleLiteral node) {
        return context.mkFP(node.getValue(), context.mkFPSortDouble());
    }
}
