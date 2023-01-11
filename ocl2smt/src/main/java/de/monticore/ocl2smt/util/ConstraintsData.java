package de.monticore.ocl2smt.util;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import java.util.*;

/** this class contains all data obtains during the conversion of a OCLConstraint */
public class ConstraintsData {
  public Expr<? extends Sort> oclContext;
  public OCLType oclContextType;

  public final Map<String, Expr<? extends Sort>> varNames = new HashMap<>();

  public final Set<BoolExpr> genConstraints = new HashSet<>();

  private boolean isPreCond = false;

  public boolean isPreCond() {
    return isPreCond;
  }

  private void reset() {
    oclContextType = null;
    oclContext = null;
    varNames.clear();
    genConstraints.clear();
    isPreCond = false;
  }

  public void initInv() {
    reset();
  }

  public void initOpConst() {
    reset();
  }

  public void initPre() {
    isPreCond = true;
  }

  public void initPost() {
    genConstraints.clear();
    isPreCond = false;
  }

  public void addVar(String name, Expr<? extends Sort> obj) {
    varNames.put(name, obj);
  }

  public void setOCLContext(Expr<? extends Sort> obj, OCLType type) {
    this.oclContext = obj;
    this.oclContextType = type;
  }

  public boolean containsVar(String name) {
    return varNames.containsKey(name);
  }

  public Expr<? extends Sort> getVar(String name) {
    return varNames.get(name);
  }

  public boolean isPresentContext() {
    return oclContext != null && oclContextType != null;
  }

  public void removeVar(String name) {
    varNames.remove(name);
  }
}
