package de.monticore.ocl2smt.ocl2smt.expr2smt.oclExprFactory;

import de.monticore.ocl2smt.ocl2smt.expr2smt.Z3ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.Z3ExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.ocl2smt.util.OCLType;

public interface OCLExprFactory<T extends ExprAdapter<?>> {
  Z3ExprAdapter mkConst(String name, OCLType type);
}
