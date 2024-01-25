package de.monticore.ocl2smt.ocl2smt.expr;

import com.microsoft.z3.Context;

public class ExprMill {
  public static ExprBuilder exprBuilder(Context ctx) {
    return new Z3ExprBuilder(ctx);
  }
}
