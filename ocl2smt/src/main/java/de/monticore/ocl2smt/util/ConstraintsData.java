package de.monticore.ocl2smt.util;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import java.util.*;

/** this class contains all data obtains during the conversion of a OCLConstraint */
public class ConstraintsData {
  private final OCLContext oclContext;
  public Map<String, Expr<? extends Sort>> varNames;

  public Set<BoolExpr> genConstraints;

  public ConstraintsData() {
    this.varNames = new HashMap<>();
    this.genConstraints = new HashSet<>();
    oclContext = new OCLContext();
  }

  public void addVar(String name, Expr<? extends Sort> obj) {
    varNames.put(name, obj);
  }

  public void setOCLContext(Expr<? extends Sort> obj, OCLType type) {
    oclContext.setOClContext(obj, type);
  }

  public boolean containsVar(String name) {
    return varNames.containsKey(name);
  }

  public Expr<? extends Sort> getVar(String name) {
    return varNames.get(name);
  }

  public boolean isPresentContext() {
    return oclContext.getType() != null && oclContext.getValue() != null;
  }

  public void removeVar(String name) {
    varNames.remove(name);
  }

  public OCLType getOCLContextType() {
    return oclContext.getType();
  }

  public Expr<? extends Sort> getOClContextValue() {
    return oclContext.getValue();
  }

  public void setOpResult(Expr<? extends Sort> result, OCLType type) {
    oclContext.setResult(result, type);
  }

  public Expr<? extends Sort> getOpResult() {
    return oclContext.getResult();
  }

  public OCLType getOpResultType() {
    return oclContext.getOpResultType();
  }

  public boolean isPresentResult() {
    return (oclContext.getResult() != null || oclContext.getOpResultType() != null);
  }
}
