package de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3;

import com.microsoft.z3.Context;
import com.microsoft.z3.Sort;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.ocl2smt.ocl2smt.expr2smt.ExprKind;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeFactorry.TypeFactory;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCPrimitiveType;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;
import java.util.Optional;

public class Z3TypeFactory implements TypeFactory {
  private final Context ctx;
  private CD2SMTGenerator cd2SMTGenerator;

  public Z3TypeFactory(CD2SMTGenerator cd2SMTGenerator) {
    this.ctx = cd2SMTGenerator.getContext();
    this.cd2SMTGenerator = cd2SMTGenerator;
  }

  public Z3TypeFactory(Context ctx) {
    this.ctx = ctx;
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

  public Z3TypeAdapter mkDoubleType() {
    return new Z3TypeAdapter("double", ctx.mkFPSortDouble(), ExprKind.DOUBLE);
  }

  @Override
  public Z3TypeAdapter mkSetType(TypeAdapter type) {
    String name = "set<" + type.getName() + ">";
    Z3TypeAdapter z3Sort = (Z3TypeAdapter) type;
    return new Z3TypeAdapter(name, z3Sort.getSort(), ExprKind.SET);
  }

  @Override
  public Z3TypeAdapter mkSeqType(TypeAdapter type) {
    String name = "seq<" + type.getName() + ">";
    Z3TypeAdapter z3Sort = (Z3TypeAdapter) type;
    return new Z3TypeAdapter(name, ctx.mkSeqSort(z3Sort.getSort()), ExprKind.SEQ);
  }

  @Override
  public Z3TypeAdapter adapt(ASTCDType cdType) {
    checkCD2SMTGenerator(cd2SMTGenerator);
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
      res = adaptQName(mcType.printType());
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
  public TypeAdapter adapt(SymTypeExpression type) {
    // case primitive type
    Optional<Z3TypeAdapter> res = adaptQName(type.printFullName());

    // case CDType
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

  public Optional<Z3TypeAdapter> adaptQName(String qName) {
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

  protected Optional<ASTCDType> resolveCDType(String name) {
    checkCD2SMTGenerator(cd2SMTGenerator);
    ASTCDDefinition cd = cd2SMTGenerator.getClassDiagram().getCDDefinition();
    return Optional.ofNullable(CDHelper.getASTCDType(name, cd));
  }

  private void checkCD2SMTGenerator(CD2SMTGenerator cd2SMTGenerator) {
    if (cd2SMTGenerator == null) {
      Log.error("TypeFactory try to access to the CD2SMTGenerator but, this property was not set ");
      assert false;
    }
  }

  public Z3TypeAdapter mkType(String name, Sort sort, ExprKind kind) {
    return new Z3TypeAdapter(name, sort, kind);
  }
}
