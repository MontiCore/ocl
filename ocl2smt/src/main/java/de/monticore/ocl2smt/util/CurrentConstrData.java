package de.monticore.ocl2smt.util;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import java.util.*;

/** this class contains all the necessary data for the conversion of a OCLConstraint */
public class CurrentConstrData {
  public Expr<? extends Sort> oclCtx;
  public OCLType oclCtxType;

  public final Map<String, Expr<? extends Sort>> varNames = new HashMap<>();

  public final Set<BoolExpr> genInvConstraints = new HashSet<>();

  public List<Expr<? extends Sort>> declExpr = new ArrayList<>();

  private boolean isPreCond = false;

  public boolean isPreCond() {
    return isPreCond;
  }

  private void reset() {
    oclCtxType = null;
    oclCtx = null;
    varNames.clear();
    genInvConstraints.clear();
    declExpr.clear(); // TODO:use it local
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
    genInvConstraints.clear();
    declExpr.clear();
    isPreCond = false;
  }

  public void addVar(String name, Expr<? extends Sort> obj) {
    varNames.put(name, obj);
  }

  public void setOCLContext(Expr<? extends Sort> obj, OCLType type) {
    this.oclCtx = obj;
    this.oclCtxType = type;
  }

  public boolean containsVar(String name) {
    return varNames.containsKey(name);
  }

  public Expr<? extends Sort> getVar(String name) {
    return varNames.get(name);
  }

  public boolean isPresentContext() {
    return oclCtx != null && oclCtxType != null;
  }

  public void removeVar(String name) {
    varNames.remove(name);
  }

  public void addDeclExpr(Expr<? extends Sort> obj) {
    declExpr.add(obj);
  }
}
