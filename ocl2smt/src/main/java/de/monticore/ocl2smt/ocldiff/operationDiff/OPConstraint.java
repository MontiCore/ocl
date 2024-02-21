package de.monticore.ocl2smt.ocldiff.operationDiff;

import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3TypeAdapter;

/** this Class is saves data obtains after the conversion of an OCL Constraint in SMT */
public class OPConstraint {

  protected IdentifiableBoolExpr preCond;
  protected IdentifiableBoolExpr postCond;
  protected IdentifiableBoolExpr operationConstraint;

  protected final Z3ExprAdapter thisObj;
  protected final Z3ExprAdapter result;
  protected final Z3TypeAdapter ThisType;

  public OPConstraint(
      IdentifiableBoolExpr preCond,
      IdentifiableBoolExpr postCond,
      IdentifiableBoolExpr opConstraint,
      Z3ExprAdapter res,
      Z3ExprAdapter thisObj) {
    this.preCond = preCond;
    this.postCond = postCond;
    this.result = res;

    this.ThisType = thisObj.getType();
    this.thisObj = thisObj;

    operationConstraint = opConstraint;
  }

  public Z3TypeAdapter getThisType() {
    return ThisType;
  }

  public IdentifiableBoolExpr getPreCond() {
    return preCond;
  }

  public IdentifiableBoolExpr getPostCond() {
    return postCond;
  }

  public IdentifiableBoolExpr getOperationConstraint() {
    return operationConstraint;
  }

  public Expr<? extends Sort> getThisObj() {
    return thisObj.getExpr();
  }

  public Z3ExprAdapter getResult() {
    return result;
  }

  public boolean isPresentResult() {
    return this.result != null;
  }
}
