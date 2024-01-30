package de.monticore.ocl2smt.ocl2smt.expr2smt;

import com.microsoft.z3.Context;
import de.monticore.ocl2smt.ocl2smt.expr.ExpressionKind;
import de.monticore.ocl2smt.ocl2smt.expr2smt.oclExprFactory.OCLExprFactory;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.ocl2smt.util.TypeConverter;
import java.util.*;

public class Z3OCLExprFactory implements OCLExprFactory<Z3ExprAdapter> {
  protected TypeConverter typeConverter;
  protected Z3ExprFactory exprFactory;

  protected Context ctx;

  Z3OCLExprFactory(TypeConverter typeConverter, Z3ExprFactory exprFactory) {

    this.typeConverter = typeConverter;
    this.exprFactory = exprFactory;
  }

  @Override
  public Z3ExprAdapter mkConst(String name, OCLType type) {

    Z3ExprAdapter expr = new Z3ExprAdapter();
    expr.setExpr(ctx.mkConst(name, typeConverter.deriveSort(type)));
    expr.setKind(ExpressionKind.UNINTERPRETED);

    return expr;
  }
}
