package de.monticore.ocl2smt.util;

import com.microsoft.z3.BoolSort;
import com.microsoft.z3.FuncDecl;
import de.monticore.ocl2smt.ocl2smt.expr2smt.Z3ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.Z3TypeAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.Z3TypeFactory;
import de.monticore.types.mcbasictypes._ast.ASTMCReturnType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCLMethodResult{
  private ResultType type;

  private Z3TypeAdapter exprType;
  private Z3ExprAdapter res;
  private FuncDecl<BoolSort> resSet;

  public void setValue(FuncDecl<BoolSort> resSet) {
    this.resSet = resSet;
  }

  public void setValue(Z3ExprAdapter res) {
    this.res = res;
  }

  public Z3TypeAdapter getOclType() {
    return exprType;
  }

  public void setType(ASTMCReturnType type) {
    /* if (type.isPresentMCVoidType()) {
      this.type = ResultType.VOID;
    } else if (type.isPresentMCType()) {
      ASTMCType mcType = type.getMCType();

      if (mcType.printType().startsWith("Set")) {
        String innerType = getSetElementType(mcType);
        if (TypeConverter.hasSimpleType(innerType)) {

          ExprTypeAdapter = ExprTypeAdapter.buildOCLType(innerType);
        } else if (!innerType.contains("<")) {
          this.type = ResultType.SET_OF_OBJECT;

        } else {
          Log.error("Method return Type " + type.printType() + " not Supported");
        }
        ExprTypeAdapter = ExprTypeAdapter.buildOCLType(innerType);
      } else if (TypeConverter.hasSimpleType(mcType.printType())) {
        this.type = ResultType.PRIMITIVE;
        ExprTypeAdapter = ExprTypeAdapter.buildOCLType(mcType.printType());
      } else if (!mcType.printType().contains("<")) {
        this.type = ResultType.OBJECT;
        ExprTypeAdapter = ExprTypeAdapter.buildOCLType(mcType.printType());
      } else {
        Log.error("Method return Type " + type.printType() + " not Supported");
      }

    } else {
      this.type = ResultType.NO_RETURN_TYPE;
    }*/
  }

  public void setType(Z3TypeAdapter type) {
      if (type.isNative()) {
      this.type = ResultType.PRIMITIVE;
    } else {
      this.type = ResultType.OBJECT;
    }
    this.exprType = exprType;
  }

  public boolean isPrimitive() {
    return type == ResultType.PRIMITIVE;
  }

  public boolean isSetOfPrimitive() {
    return type == ResultType.SET_OF_PRIMITIVE;
  }

  public boolean isSetOfObject() {
    return type == ResultType.SET_OF_OBJECT;
  }

  public boolean isVoid() {
    return this.type == ResultType.VOID;
  }

  public boolean isObject() {
    return type == ResultType.OBJECT;
  }

  public Z3ExprAdapter getResultExpr() {
    return res;
  }

  public FuncDecl<BoolSort> getResultSet() {
    return resSet;
  }

  enum ResultType {
    PRIMITIVE,
    SET_OF_PRIMITIVE,
    OBJECT,
    SET_OF_OBJECT,
    VOID,
    NO_RETURN_TYPE
  }

  private String getSetElementType(ASTMCType astmcType) {
    String type = astmcType.printType();
    String res = null;
    Pattern pattern = Pattern.compile("Set<(.+?)>");
    Matcher matcher = pattern.matcher(type);

    if (matcher.find()) {
      res = matcher.group(1);
    }
    return res;
  }

  public boolean isPresent() {
    return this.res != null || this.resSet != null;
  }
}
