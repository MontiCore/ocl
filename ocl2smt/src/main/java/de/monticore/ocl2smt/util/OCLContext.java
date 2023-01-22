package de.monticore.ocl2smt.util;

import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;

public class OCLContext {
  private OCLType type;
  private Expr<? extends Sort> value;
  private Expr<? extends Sort> result;

  private OCLType opResultType;

  public Expr<? extends Sort> getValue() {
    return value;
  }

  public void setOClContext(Expr<? extends Sort> oClContext, OCLType type) {
    this.type = type;
    this.value = oClContext;
  }

  public OCLType getType() {
    return type;
  }

  public void setResult(Expr<? extends Sort> result, OCLType type) {
    this.result = result;
    this.opResultType = type;
  }

  public Expr<? extends Sort> getResult() {
    return result;
  }

  public OCLType getOpResultType() {
    return opResultType;
  }
}
