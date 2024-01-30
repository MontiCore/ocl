package de.monticore.ocl2smt.ocl2smt.expr2smt;

import de.monticore.ocl2smt.ocl2smt.expressionconverter.OCLExprConverter;
import de.monticore.ocl2smt.util.OCLType;
import java.util.function.Function;

public class Z3SetExprAdapter extends Z3ExprAdapter {

  Function<Z3ExprAdapter, Z3ExprAdapter> function;
  OCLType type;
  OCLExprConverter<Z3ExprAdapter> exprConv;

  public void setFunction(Function<Z3ExprAdapter, Z3ExprAdapter> function) {
    this.function = function;
  }

  public void setType(OCLType type) {
    this.type = type;
  }

  public void setExprConverter(OCLExprConverter<Z3ExprAdapter> exprConv) {
    this.exprConv = exprConv;
  }

  public Z3ExprAdapter collectAll(Function<Z3ExprAdapter, Z3ExprAdapter> function) {
    Z3ExprAdapter expr = exprConv.mkConst("xollector", type);
    /*  return mkSet(
    kii ->
            exprConv
                    .mkExists(
                            List.of(expr),
                            ExprMill.Z3ExprAdapter(ctx)
                                    .mkAnd(this.contains(expr), function.apply(expr).contains(kii)))
                    .expr(),
    ((Z3Z3ExprAdapter)function.apply(expr)).type,
    exprConv);*/
    return null; // todo fix me
  }
}
