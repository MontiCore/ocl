/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt.expressionconverter;

import com.microsoft.z3.*;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.literals.mccommonliterals._ast.*;
import de.monticore.literals.mcliteralsbasis._ast.ASTLiteral;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.ocl2smt.util.TypeConverter;
import de.se_rwth.commons.logging.Log;
import java.util.HashMap;
import java.util.Map;

/** This class convert variable and Object in SMT and save their types in VarTypes */
public class LiteralConverter {
  protected Context context;
  protected final Map<Expr<? extends Sort>, OCLType> varTypes = new HashMap<>();
  protected final TypeConverter typeConverter;

  public LiteralConverter(Context context, TypeConverter typeConverter) {
    this.context = context;
    this.typeConverter = typeConverter;
  }

  public Context getContext() {
    return context;
  }

  public void reset() {
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

  protected IntNum convertChar(ASTCharLiteral node) {
    return context.mkInt(node.getValue());
  }

  protected FPNum convertDouble(ASTBasicDoubleLiteral node) {
    return context.mkFP(node.getValue(), context.mkFPSortDouble());
  }

  public Expr<? extends Sort> declObj(OCLType type, String name) {
    Expr<? extends Sort> expr = context.mkConst(name, typeConverter.getSort(type));
    varTypes.put(expr, type);
    return expr;
  }

  public OCLType getType(Expr<? extends Sort> expr) {
    if (varTypes.containsKey(expr)) {
      return varTypes.get(expr);
    } else if (TypeConverter.isPrimitiv(expr.getSort())) {
      return OCLType.buildOCLType(expr.getSort().toString());
    }
    Log.error("Type not found for the Variable " + expr);
    return null;
  }
}
