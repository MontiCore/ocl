package de.monticore.ocl2smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.se_rwth.commons.logging.Log;

import java.util.function.Function;

public class SMTSet {
    Function<Expr<? extends Sort>, BoolExpr> setFunction;
    Sort sort;


    public SMTSet(Function<Expr<? extends Sort>, BoolExpr> setFunction, Sort sort) {
        this.setFunction = setFunction;
        this.sort = sort;
    }

    private static SMTSet mkSetOperation(SMTSet leftSet, SMTSet rightSet, Context ctx, OPERATION op) {
        if (!rightSet.sort.equals(leftSet.sort)) {
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
        return new SMTSet(setFunction, leftSet.sort);
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

    public BoolExpr isIn(Expr<? extends Sort> expr) {
        return setFunction.apply(expr);
    }

    public BoolExpr notIn(Expr<? extends Sort> expr, Context ctx) {
        return ctx.mkNot(setFunction.apply(expr));
    }

    public SMTSet collectAll(Function<Expr<? extends Sort>, SMTSet> function, Context ctx) {
        Expr<? extends Sort> expr = ctx.mkConst("xollector", sort);
        return new SMTSet(kii -> ctx.mkForall(new Expr[]{expr}, ctx.mkImplies(this.isIn(expr),
                function.apply(expr).isIn(kii)), 0, null, null, null, null), function.apply(expr).sort);
    }

    enum OPERATION {UNION, INTERSECTION, MINUS}

}
