/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.literals.mccommonliterals._ast.*;
import de.monticore.literals.mcliteralsbasis._ast.ASTLiteral;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.ocl2smt.util.TypeConverter;
import de.se_rwth.commons.logging.Log;
import java.util.HashMap;
import java.util.Map;

public class ConstConverter {
  protected  Context context;
  protected  final Map<Expr<? extends Sort>, OCLType> varTypes = new HashMap<>();

  public void reset(Context context) {
    this.context = context;
    varTypes.clear();
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
    return context.mkBool(node.getValue());
  }

  protected SeqExpr<CharSort> convertString(ASTStringLiteral node) {
    return context.mkString(node.getValue());
  }

  protected IntNum convertNat(ASTNatLiteral node) {
    return context.mkInt(node.getValue());
  }

  protected IntNum convertChar(ASTCharLiteral node) { // TODO:: return a Char here
    return context.mkInt(node.getValue());
  }

  protected FPNum convertDouble(ASTBasicDoubleLiteral node) {
    return context.mkFP(node.getValue(), context.mkFPSortDouble());
  }

  protected Expr<? extends Sort> declObj(OCLType type, String name) {
    Expr<? extends Sort> expr = context.mkConst(name, TypeConverter.getSort(type));
    varTypes.put(expr, type);
    return expr;
  }

  public OCLType getType(Expr<? extends Sort> expr) {
    if (varTypes.containsKey(expr)) {
      return varTypes.get(expr); // Person p
    }
    return OCLType.buildOCLType(expr.getSort().getName().toString()); // x*x
  }
}
