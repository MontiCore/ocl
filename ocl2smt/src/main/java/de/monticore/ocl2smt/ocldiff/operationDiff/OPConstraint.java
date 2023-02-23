package de.monticore.ocl2smt.ocldiff.operationDiff;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.ocl2smt.util.OCLType;
import java.util.Optional;

/** this Class is saves data obtains after the conversion of an OCL Constraint in SMT */
public class OPConstraint {

  protected IdentifiableBoolExpr preCond;
  protected IdentifiableBoolExpr postCond;
  protected IdentifiableBoolExpr operationConstraint;

  protected final Expr<? extends Sort> result;
  protected final Expr<? extends Sort> thisObj;
  protected final OCLType resultType;
  protected final OCLType ThisType;

  public OPConstraint(
      IdentifiableBoolExpr preCond,
      IdentifiableBoolExpr postCond,
      Expr<? extends Sort> res,
      Expr<? extends Sort> ThisObj,
      OCLType resType,
      OCLType thisType,
      Context ctx) {
    this.preCond = preCond;
    this.postCond = postCond;
    this.result = res;
    this.resultType = resType;
    this.ThisType = thisType;
    this.thisObj = ThisObj;

    BoolExpr op = ctx.mkImplies(preCond.getValue(), postCond.getValue());
    operationConstraint =
        IdentifiableBoolExpr.buildIdentifiable(
            op, preCond.getSourcePosition(), Optional.of("pre ==> Post"));
  }

  public IdentifiableBoolExpr getPreCond() {
    return preCond;
  }

  public IdentifiableBoolExpr getOperationConstraint() {
    return operationConstraint;
  }

  public Expr<? extends Sort> getThisObj() {
    return thisObj;
  }

  public Expr<? extends Sort> getResult() {
    return result;
  }

  public OCLType getResultType() {
    return resultType;
  }

  public boolean isPresentResult() {
    return this.result != null;
  }
}
