package de.monticore.ocl2smt.util;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import java.util.*;

/** this class contains all data obtains during the conversion of a OCLConstraint */
public class ConstraintsData {
  private OCLContext oclContext;

  public final Map<String, Expr<? extends Sort>> varNames = new HashMap<>();

  public final Set<BoolExpr> genConstraints = new HashSet<>();

  public void addVar(String name, Expr<? extends Sort> obj) {
    varNames.put(name, obj);
  }

  public void setOCLContext(Expr<? extends Sort> obj, OCLType type) {
    oclContext = new OCLContext(type, obj);
  }

  public boolean containsVar(String name) {
    return varNames.containsKey(name);
  }

  public Expr<? extends Sort> getVar(String name) {
    return varNames.get(name);
  }

  public boolean isPresentContext() {
    return oclContext != null;
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

}
