package de.monticore.ocl2smt.util;

import com.microsoft.z3.Context;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;

/** this Class is saves data obtains after the conversion of an OCL Constraint in SMT */
public class OCLConstraint {
  boolean isInvariant;
  IdentifiableBoolExpr invariant;
  IdentifiableBoolExpr preCond;
  IdentifiableBoolExpr postCond;

  public OCLConstraint(IdentifiableBoolExpr preCond, IdentifiableBoolExpr postCond) {
    this.preCond = preCond;
    this.postCond = postCond;
    this.isInvariant = false;
  }

  public OCLConstraint(IdentifiableBoolExpr getInvariant) {
    this.invariant = getInvariant;
    this.isInvariant = true;
  }

  public OCLConstraint negateInv(Context ctx) {
    invariant = invariant.negate(ctx);
    return this;
  }

  public boolean isInvariant() {
    return isInvariant;
  }

  public boolean isOpConstraint() {
    return !isInvariant;
  }

  public IdentifiableBoolExpr getInvariant() {
    return invariant;
  }

  public IdentifiableBoolExpr getPostCond() {
    return postCond;
  }

  public IdentifiableBoolExpr getPreCond() {
    return preCond;
  }
}
