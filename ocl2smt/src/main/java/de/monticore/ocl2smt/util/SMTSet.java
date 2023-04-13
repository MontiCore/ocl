/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.util;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.LiteralConverter;
import de.se_rwth.commons.logging.Log;
import java.util.function.Function;

public class SMTSet {
  private final Function<Expr<? extends Sort>, BoolExpr> setFunction;
  private final OCLType type;

  private final Context ctx;
  private final LiteralConverter literalConverter;

  public SMTSet(
      Function<Expr<? extends Sort>, BoolExpr> setFunction,
      OCLType type,
      LiteralConverter literalConverter) {
    this.setFunction = setFunction;
    this.type = type;
    this.literalConverter = literalConverter;
    this.ctx = literalConverter.getContext();
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
    return new SMTSet(setFunction, this.type, literalConverter);
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
    Expr<? extends Sort> expr = literalConverter.declObj(set.getType(), "expr111");
    return ctx.mkForall(
        new Expr[] {expr},
        ctx.mkImplies(set.contains(expr), this.contains(expr)),
        0,
        null,
        null,
        null,
        null);
  }

  public BoolExpr isEmpty(Context ctx) {
    Expr<? extends Sort> expr = literalConverter.declObj(type, "expr11");
    return ctx.mkForall(new Expr[] {expr}, this.notIn(expr), 0, null, null, null, null);
  }

  public BoolExpr notIn(Expr<? extends Sort> expr) {
    return ctx.mkNot(contains(expr));
  }

  public SMTSet collectAll(Function<Expr<? extends Sort>, SMTSet> function) {
    Expr<? extends Sort> expr = literalConverter.declObj(type, "xollector");
    return new SMTSet(
        kii ->
            ctx.mkExists(
                new Expr[] {expr},
                ctx.mkAnd(this.contains(expr), function.apply(expr).contains(kii)),
                0,
                null,
                null,
                null,
                null),
        function.apply(expr).type,
        literalConverter);
  }

  public BoolExpr mkSetEq(SMTSet set2) {

    Expr<? extends Sort> expr = literalConverter.declObj(this.getType(), "const");
    return ctx.mkForall(
        new Expr[] {expr},
        ctx.mkEq(this.contains(expr), set2.contains(expr)),
        0,
        null,
        null,
        null,
        null);
  }

  enum OPERATION {
    UNION,
    INTERSECTION,
    MINUS
  }
}
