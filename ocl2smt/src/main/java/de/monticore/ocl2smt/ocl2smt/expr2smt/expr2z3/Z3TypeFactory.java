package de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3;

import com.microsoft.z3.Context;
import com.microsoft.z3.Sort;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl.types3.OCLTypeTraverserFactory;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.ocl2smt.expr2smt.ExprKind;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeFactorry.TypeFactory;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCPrimitiveType;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types3.Type4Ast;
import de.se_rwth.commons.logging.Log;
import java.util.Optional;

public class Z3TypeFactory implements TypeFactory<Sort> {
  private final Context ctx;
  private final CD2SMTGenerator cd2SMTGenerator;

  public Z3TypeFactory(CD2SMTGenerator cd2SMTGenerator) {
    this.ctx = cd2SMTGenerator.getContext();
    this.cd2SMTGenerator = cd2SMTGenerator;
  }

  @Override
  public Z3TypeAdapter mkBoolType() {
    return new Z3TypeAdapter("bool", ctx.mkBoolSort(), ExprKind.BOOL);
  }

  @Override
  public Z3TypeAdapter mkStringType() {
    return new Z3TypeAdapter("String", ctx.mkStringSort(), ExprKind.STRING);
  }

  @Override
  public Z3TypeAdapter mkCharType() {
    return new Z3TypeAdapter("char", ctx.mkIntSort(), ExprKind.CHAR);
  }

  @Override
  public Z3TypeAdapter mkInType() {
    return new Z3TypeAdapter("int", ctx.mkIntSort(), ExprKind.INTEGER);
  }

  @Override
  public TypeAdapter<Sort> mkSetType(Sort elemType) {
    String name = "set<" + elemType + ">";
    return new Z3TypeAdapter(name, elemType, ExprKind.SET);
  }

  @Override
  public Z3TypeAdapter adapt(ASTCDType cdType) {
    return new Z3TypeAdapter(cdType, cd2SMTGenerator.getSort(cdType), ExprKind.OO);
  }

  @Override
  public Z3TypeAdapter adapt(ASTMCType mcType) {
    Optional<Z3TypeAdapter> res = Optional.empty();

    // case primitive type
    if (mcType instanceof ASTMCPrimitiveType) {
      ASTMCPrimitiveType primType = (ASTMCPrimitiveType) mcType;
      if (primType.isBoolean()) {
        res = Optional.ofNullable(mkBoolType());
      } else if (primType.isDouble()) {
        res = Optional.ofNullable(mkDoubleType());
      } else if (primType.isInt()) {
        res = Optional.ofNullable(mkInType());
      } else if (primType.isChar()) {
        res = Optional.ofNullable(mkCharType());
      } else if (primType.isByte()) {
        res = Optional.ofNullable(mkInType());
      } else if (primType.isFloat()) {
        res = Optional.ofNullable(mkDoubleType());
      } else if (primType.isLong()) {
        res = Optional.ofNullable(mkInType());
      } else {
        res = Optional.ofNullable(mkInType());
      }

      // case qualified type
    } else if (mcType instanceof ASTMCQualifiedType) {
      res = Optional.ofNullable(adaptQName(mcType.printType()).orElse(null));
    }

    // case CDType
    if (res.isEmpty()) {
      Optional<ASTCDType> astcdType = resolveCDType(mcType.printType());
      res = astcdType.map(this::adapt);
    }

    if (res.isEmpty()) {
      Log.error("Cannot resolve the type " + mcType.printType());
      assert false;
    }

    return res.get();
  }

  @Override
  public TypeAdapter<Sort> adapt(SymTypeExpression type) {
    // case primitive type
    Optional<Z3TypeAdapter> res = adaptQName(type.printFullName());

    // case CEType
    if (res.isEmpty()) {
      String[] parts = type.print().split("\\.");
      String typeName = parts[parts.length - 1];
      Optional<ASTCDType> astcdType = resolveCDType(typeName);
      res = astcdType.map(this::adapt);
    }

    if (res.isEmpty()) {
      Log.error("Cannot resolve the type " + type.printFullName());
      assert false;
    }
    return res.get();
  }

  private Optional<Z3TypeAdapter> adaptQName(String qName) {
    switch (qName) {
      case "Boolean":
      case "boolean":
      case "java.lang.Boolean":
        return Optional.ofNullable(mkBoolType());
      case "Double":
      case "double":
      case "Real":
      case "java.lang.Double":
        return Optional.ofNullable(mkDoubleType());
      case "Integer":
      case "Date":
      case "int":
      case "Int":
      case "java.lang.Integer":
      case "java.util.Date":
        return Optional.ofNullable(mkInType());
      case "String":
      case "java.lang.String":
        return Optional.ofNullable(mkStringType());
    }
    return Optional.empty();
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
    return new Z3TypeAdapter("double", ctx.mkFPSortDouble(), ExprKind.DOUBLE);
  }

  @Override
  public SymTypeExpression deriveType(ASTNameExpression node) {
    return deriveType((ASTExpression) node);
  }

  private Optional<ASTCDType> resolveCDType(String name) {
    return Optional.ofNullable(
        CDHelper.getASTCDType(name, cd2SMTGenerator.getClassDiagram().getCDDefinition()));
  }
}
