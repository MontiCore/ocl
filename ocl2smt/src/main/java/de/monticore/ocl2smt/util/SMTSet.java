/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.util;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.OCLExpressionConverter;
import de.se_rwth.commons.logging.Log;
import java.util.List;
import java.util.function.Function;

public class SMTSet {
  private final Function<Expr<? extends Sort>, BoolExpr> setFunction;
  private final OCLType type;

  private final Context ctx;
  OCLExpressionConverter exprConv;

  public SMTSet(
      Function<Expr<? extends Sort>, BoolExpr> setFunction,
      OCLType type,
      OCLExpressionConverter exprConv) {
    this.setFunction = setFunction;
    this.type = type;
    this.ctx = exprConv.getCd2smtGenerator().getContext();
    this.exprConv = exprConv;
  }

  private SMTSet mkSetOperation(SMTSet rightSet, OPERATION op) {
    if (!rightSet.type.equals(this.type)) {
      Log.error("set intersection of Set from different Type not implemented");
    }
    Function<Expr<? extends Sort>, BoolExpr> setFunction;
    switch (op) {
      case UNION:
        setFunction = obj -> ctx.mkOr(this.contains(obj), rightSet.contains(obj));
        break;
      case INTERSECTION:
        setFunction = obj -> ctx.mkAnd(this.contains(obj), rightSet.contains(obj));
        break;
      case MINUS:
        setFunction = obj -> ctx.mkAnd(this.contains(obj), rightSet.notIn(obj));
        break;
      default:
        Log.error("the Set Operation " + op + " is not implemented ");
        setFunction = s -> ctx.mkTrue();
    }
    return new SMTSet(setFunction, this.type, exprConv);
  }

  public SMTSet mkSetUnion(SMTSet rightSet) {
    return mkSetOperation(rightSet, OPERATION.UNION);
  }

  public SMTSet mkSetIntersect(SMTSet rightSet) {
    return mkSetOperation(rightSet, OPERATION.INTERSECTION);
  }

  public SMTSet mkSetMinus(SMTSet rightSet) {
    return mkSetOperation(rightSet, OPERATION.MINUS);
  }

  public OCLType getType() {
    return type;
  }

  public BoolExpr contains(Expr<? extends Sort> expr) {
    /*  if (!literalConverter.getType(expr).equals(type)) {
      Log.error(
          "the obj "
              + expr
              + " with the Type "
              + literalConverter.getType(expr).getName()
              + " cannot be in the set  with sort "
              + type.getName());
    }*/
    return setFunction.apply(expr);
  }

  public BoolExpr containsAll(SMTSet set) {
    Expr<? extends Sort> expr = exprConv.declVariable(set.getType(), "expr111");
    return exprConv.mkForall(List.of(expr), ctx.mkImplies(set.contains(expr), this.contains(expr)));
  }

  public BoolExpr isEmpty(Context ctx) {
    Expr<? extends Sort> expr = exprConv.declVariable(type, "expr11");
    return exprConv.mkForall(List.of(expr), this.notIn(expr));
  }

  public BoolExpr notIn(Expr<? extends Sort> expr) {
    return ctx.mkNot(contains(expr));
  }

  public SMTSet collectAll(Function<Expr<? extends Sort>, SMTSet> function) {
    Expr<? extends Sort> expr = exprConv.declVariable(type, "xollector");
    return new SMTSet(
        kii ->
            exprConv.mkExists(
                List.of(expr), ctx.mkAnd(this.contains(expr), function.apply(expr).contains(kii))),
        function.apply(expr).type,
        exprConv);
  }

  public BoolExpr mkSetEq(SMTSet set2) {

    Expr<? extends Sort> expr = exprConv.declVariable(this.getType(), "const");
    return exprConv.mkForall(List.of(expr), ctx.mkEq(this.contains(expr), set2.contains(expr)));
  }

  enum OPERATION {
    UNION,
    INTERSECTION,
    MINUS
  }
}
