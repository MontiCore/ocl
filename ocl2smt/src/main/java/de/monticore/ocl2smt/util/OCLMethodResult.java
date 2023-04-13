package de.monticore.ocl2smt.util;

import com.microsoft.z3.BoolSort;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Sort;
import de.monticore.types.mcbasictypes._ast.ASTMCReturnType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCLMethodResult {
  private ResultType type;

  private OCLType oclType;
  private Expr<? extends Sort> res;
  private FuncDecl<BoolSort> resSet;

  public void setValue(FuncDecl<BoolSort> resSet) {
    this.resSet = resSet;
  }

  public void setValue(Expr<? extends Sort> res) {
    this.res = res;
  }

  public OCLType getOclType() {
    return oclType;
  }

  public void setType(ASTMCReturnType type) {
    if (type.isPresentMCVoidType()) {
      this.type = ResultType.VOID;
    } else if (type.isPresentMCType()) {
      ASTMCType mcType = type.getMCType();

      if (mcType.printType().startsWith("Set")) {
        String innerType = getSetElementType(mcType);
        if (TypeConverter.isPrimitiv(innerType)) {

          oclType = OCLType.buildOCLType(innerType);
        } else if (!innerType.contains("<")) {
          this.type = ResultType.SET_OF_OBJECT;

        } else {
          Log.error("Method return Type " + type.printType() + " not Supported");
        }
        oclType = OCLType.buildOCLType(innerType);
      } else if (TypeConverter.isPrimitiv(mcType.printType())) {
        this.type = ResultType.PRIMITIVE;
        oclType = OCLType.buildOCLType(mcType.printType());
      } else if (!mcType.printType().contains("<")) {
        this.type = ResultType.OBJECT;
        oclType = OCLType.buildOCLType(mcType.printType());
      } else {
        Log.error("Method return Type " + type.printType() + " not Supported");
      }

    } else {
      this.type = ResultType.NO_RETURN_TYPE;
    }
  }

  public void setType(OCLType oclType) {
    if (TypeConverter.isPrimitiv(oclType.getName())) {
      this.type = ResultType.PRIMITIVE;
    } else {
      this.type = ResultType.OBJECT;
    }
    this.oclType = oclType;
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

  public Expr<? extends Sort> getResultExpr() {
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
}
