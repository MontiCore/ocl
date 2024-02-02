package de.monticore.ocl2smt.ocl2smt.expr2smt;

import com.microsoft.z3.Context;
import com.microsoft.z3.Sort;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl.types3.OCLTypeTraverserFactory;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeFactorry.TypeFactory;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCPrimitiveType;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types3.Type4Ast;
import de.se_rwth.commons.logging.Log;

public class Z3TypeFactory implements TypeFactory<Sort> {
  private final Context ctx;
  private final CD2SMTGenerator cd2SMTGenerator;

  public Z3TypeFactory(CD2SMTGenerator cd2SMTGenerator) {
    this.ctx = cd2SMTGenerator.getContext();
    this.cd2SMTGenerator = cd2SMTGenerator;
  }

  @Override
  public Z3TypeAdapter mkBoolType() {
    return new Z3TypeAdapter("bool", ctx.mkBoolSort(), ExpressionKind.BOOL);
  }

  @Override
  public Z3TypeAdapter mkStringType() {
    return new Z3TypeAdapter("String", ctx.mkStringSort(), ExpressionKind.STRING);
  }

  @Override
  public Z3TypeAdapter mkCharTYpe() {
    return new Z3TypeAdapter("char", ctx.mkIntSort(), ExpressionKind.CHAR);
  }

  @Override
  public Z3TypeAdapter mkInType() {
    return new Z3TypeAdapter("int", ctx.mkIntSort(), ExpressionKind.INTEGER);
  }

  @Override
  public TypeAdapter<Sort> mkSetType(Sort elementType) {
    String name = "<" + elementType + ">";
    return new Z3TypeAdapter(name, elementType, ExpressionKind.SET);
  }

  @Override
  public Z3TypeAdapter adapt(ASTCDType cdType) {
    return null;
  }

  @Override
  public Z3TypeAdapter adapt(ASTMCType mcType) {
    Z3TypeAdapter res;

    if (mcType instanceof ASTMCPrimitiveType) {
      ASTMCPrimitiveType primType = (ASTMCPrimitiveType) mcType;
      if (primType.isBoolean()) {
        res = mkBoolType();
      } else if (primType.isDouble()) {
        res = mkDoubleType();
      } else if (primType.isInt()) {
        res = mkInType();
      } else if (primType.isChar()) {
        res = mkCharTYpe();
      } else if (primType.isByte()) {
        res = mkInType();
      } else if (primType.isFloat()) {
        res = mkDoubleType();
      } else if (primType.isLong()) {
        return mkInType();
      } else {
        return mkInType();
      }

    } else if (mcType instanceof ASTMCQualifiedType) {
      res = adaptQName(mcType.printType());
    } else {
      res = null;
      Log.error("type conversion ist only implemented for  primitives types and Qualified types");
    }
    return res;
  }

  @Override
  public TypeAdapter<Sort> adapt(SymTypeExpression typeSymbol) {
    if (typeSymbol.isObjectType() || typeSymbol.isPrimitive()) {
      String typename = typeSymbol.getTypeInfo().getFullName();
      return adaptQName(typename);
    }
    Log.error("Building OCLType not implement for the type " + typeSymbol.printFullName());
    return null;
  }

  private Z3TypeAdapter adaptQName(String qName) {
    switch (qName) {
      case "Boolean":
      case "boolean":
      case "java.lang.Boolean":
        return mkBoolType();
      case "Double":
      case "double":
      case "Real":
      case "java.lang.Double":
        return mkDoubleType();
      case "Integer":
      case "Date":
      case "int":
      case "Int":
      case "java.lang.Integer":
      case "java.util.Date":
        return mkInType();
      case "String":
      case "java.lang.String":
        return mkStringType();
    }
    return null;
  }

  public static SymTypeExpression deriveType(ASTExpression node) {
    Type4Ast type4Ast = new Type4Ast();
    OCLTraverser typeMapTraverser = new OCLTypeTraverserFactory().createTraverser(type4Ast);
    node.accept(typeMapTraverser);
    SymTypeExpression typeExpr = type4Ast.getTypeOfExpression(node);
    if (typeExpr == null) {
      Log.error("Unable to derive the type of the expression " + IOHelper.print(node));
      assert false;
    }
    return typeExpr;
  }

  public Z3TypeAdapter mkDoubleType() {
    return new Z3TypeAdapter("double", ctx.mkFPSortDouble(), ExpressionKind.DOUBLE);
  }

  @Override
  public SymTypeExpression deriveType(ASTNameExpression node) {
    return deriveType((ASTExpression) node);
  }
}
