package de.monticore.ocl2smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.se_rwth.commons.logging.Log;
import java.util.function.Function;

public class SMTSet {
  private final Function<Expr<? extends Sort>, BoolExpr> setFunction;
  private final OCLType type;

  public SMTSet(Function<Expr<? extends Sort>, BoolExpr> setFunction, OCLType type) {
    this.setFunction = setFunction;
    this.type = type;
  }

  private static SMTSet mkSetOperation(SMTSet leftSet, SMTSet rightSet, Context ctx, OPERATION op) {
    if (!rightSet.type.equals(leftSet.type)) {
      Log.error("set intersection of Set from different Type not implemented");
    }
    Function<Expr<? extends Sort>, BoolExpr> setFunction;
    switch (op) {
      case UNION:
        setFunction = obj -> ctx.mkOr(leftSet.isIn(obj), rightSet.isIn(obj));
        break;
      case INTERSECTION:
        setFunction = obj -> ctx.mkAnd(leftSet.isIn(obj), rightSet.isIn(obj));
        break;
      case MINUS:
        setFunction = obj -> ctx.mkAnd(leftSet.isIn(obj), rightSet.notIn(obj, ctx));
        break;
      default:
        Log.error("the Set Operation " + op + " is not implemented ");
        setFunction = s -> ctx.mkTrue();
    }
    return new SMTSet(setFunction, leftSet.type);
  }

  public static SMTSet mkSetUnion(SMTSet leftSet, SMTSet rightSet, Context ctx) {
    return mkSetOperation(leftSet, rightSet, ctx, OPERATION.UNION);
  }

  public static SMTSet mkSetIntersect(SMTSet lefSet, SMTSet rightSet, Context ctx) {
    return mkSetOperation(lefSet, rightSet, ctx, OPERATION.INTERSECTION);
  }

  public static SMTSet mkSetMinus(SMTSet leftSet, SMTSet rightSet, Context ctx) {
    return mkSetOperation(leftSet, rightSet, ctx, OPERATION.MINUS);
  }

  public OCLType getType() {
    return type;
  }

  public BoolExpr isIn(Expr<? extends Sort> expr) {
    if (!expr.getSort().equals(TypeConverter.getSort(type))) {
      Log.error(
          "the obj "
              + expr
              + " with the sort "
              + expr.getSort()
              + " cannot be in the set  with sort "
              + type.getName());
    }
    return setFunction.apply(expr);
  }

  public BoolExpr notIn(Expr<? extends Sort> expr, Context ctx) {
    return ctx.mkNot(isIn(expr));
  }

  public Sort getSort() {
    return TypeConverter.getSort(type);
  }

  public SMTSet collectAll(Function<Expr<? extends Sort>, SMTSet> function, Context ctx) {
    Expr<? extends Sort> expr = ctx.mkConst("xollector", TypeConverter.getSort(type));
    return new SMTSet(
        kii ->
            ctx.mkExists(
                new Expr[] {expr},
                ctx.mkAnd(this.isIn(expr), function.apply(expr).isIn(kii)),
                0,
                null,
                null,
                null,
                null),
        function.apply(expr).type);
  }

  public static BoolExpr mkSetEq(SMTSet set, SMTSet set2, Context ctx) {
    Expr<? extends Sort> expr = ctx.mkConst("const", TypeConverter.getSort(set.getType()));
    return ctx.mkForall(
        new Expr[] {expr}, ctx.mkEq(set.isIn(expr), set2.isIn(expr)), 0, null, null, null, null);
  }

  enum OPERATION {
    UNION,
    INTERSECTION,
    MINUS
  }
}
