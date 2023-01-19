package de.monticore.ocl2smt.util;

import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import java.util.ArrayList;
import java.util.List;

public class OCLContext {
  private final OCLType type;
  private final Expr<? extends Sort> value;


  public OCLContext(OCLType type, Expr<? extends Sort> value) {
    this.type = type;
    this.value = value;
  }


  public Expr<? extends Sort> getValue() {
    return value;
  }



  public OCLType getType() {
    return type;
  }
}
