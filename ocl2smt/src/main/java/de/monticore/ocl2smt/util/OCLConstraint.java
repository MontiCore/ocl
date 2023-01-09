package de.monticore.ocl2smt.util;

import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;

/** this Class is saves data obtains after the conversion of an OCL Constraint in SMT */
public class OCLConstraint {

  public OCLConstraint(IdentifiableBoolExpr preCond, IdentifiableBoolExpr postCond) {
    this.preCond = preCond;
    this.postCond = postCond;
    this.isInvariant = false;
  }

  public OCLConstraint(IdentifiableBoolExpr constraint) {
    this.constraint = constraint;
    this.isInvariant = true;
  }

  boolean isInvariant;
  IdentifiableBoolExpr constraint;
  IdentifiableBoolExpr preCond;
  IdentifiableBoolExpr postCond;

  public boolean isInvariant() {
    return isInvariant;
  }

  public boolean isOpConstraint() {
    return !isInvariant;
  }

  public IdentifiableBoolExpr getConstraint() {
    return constraint;
  }

  public IdentifiableBoolExpr getPostCond() {
    return postCond;
  }

  public IdentifiableBoolExpr getPreCond() {
    return preCond;
  }
}
