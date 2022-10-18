package de.monticore.ocl2smt;

import com.microsoft.z3.*;
import de.se_rwth.commons.logging.Log;

public class SMTSet {
    protected String name;
    protected FuncDecl<BoolSort> setFunction;
    protected BoolExpr definition;
    //TODO: add variable filter as List of Bool Constraints
    public SMTSet(String name, FuncDecl<BoolSort> setFunction, BoolExpr filter) {
        this.definition = filter;
        this.setFunction = setFunction;
        this.name = name;
    }

    private static SMTSet mkSetOperation(SMTSet leftSet, SMTSet rightSet, Context ctx, OPERATION op) {
        if (!leftSet.getSort().equals(rightSet.getSort())) {
            Log.error("conversion of Set Operation with set of different type-element  not yet implemented ");
            //TODO:complete implementation
        }
        String setName = leftSet.getName() + op.name() + rightSet.getName();
        FuncDecl<BoolSort> setFunc = ctx.mkFuncDecl(setName, leftSet.getSort(), ctx.mkBoolSort());
        Expr<Sort> obj = ctx.mkConst("uObj", leftSet.getSort());
        BoolExpr operation;
        switch (op) {
            case UNION:
                operation = ctx.mkForall(new Expr[]{obj}, ctx.mkEq(ctx.mkApp(setFunc, obj), ctx.mkOr(
                                (BoolExpr) ctx.mkApp(leftSet.getSetFunction(), obj), (BoolExpr) ctx.mkApp(rightSet.getSetFunction(), obj)))
                        , 0, null, null, null, null);
                break;
            case INTERSECTION:
                operation = ctx.mkForall(new Expr[]{obj}, ctx.mkEq(ctx.mkApp(setFunc, obj), ctx.mkAnd(
                                (BoolExpr) ctx.mkApp(leftSet.getSetFunction(), obj), (BoolExpr) ctx.mkApp(rightSet.getSetFunction(), obj)))
                        , 0, null, null, null, null);
                break;
            case MINUS:
                operation = ctx.mkForall(new Expr[]{obj}, ctx.mkEq(ctx.mkApp(setFunc, obj), ctx.mkAnd((BoolExpr) ctx.mkApp(
                        leftSet.getSetFunction(), obj), ctx.mkNot ((BoolExpr) ctx.mkApp(rightSet.getSetFunction(), obj)))),
                        0, null, null, null, null);
                break;
            default:
                operation = ctx.mkTrue();
        }

        BoolExpr definition = ctx.mkAnd(leftSet.getDefinition(), rightSet.getDefinition(), operation);

        return new SMTSet(setName, setFunc, definition);
    }

    public static SMTSet mkSetUnion(SMTSet leftSet, SMTSet rightSet, Context ctx) {
        return mkSetOperation(leftSet, rightSet, ctx, OPERATION.UNION);
    }

    public static SMTSet mkSetIntersect(SMTSet lefSet, SMTSet rightSet, Context ctx) {
        return mkSetOperation(lefSet, rightSet, ctx, OPERATION.INTERSECTION);
    }

    public static SMTSet mkSetMinus(SMTSet leftSet, SMTSet rightSet,Context ctx){
        return mkSetOperation(leftSet, rightSet,ctx,OPERATION.MINUS);
    }

    public BoolExpr getDefinition() {
        return definition;
    }

    public FuncDecl<? extends Sort> getSetFunction() {
        return setFunction;
    }

    public String getName() {
        return name;
    }

    public Sort getSort() {
        return this.setFunction.getDomain()[0];
    }
     //TODO: replace it with add filter
    public void setDefinition(BoolExpr definition) {
        this.definition = definition;
    }

    enum OPERATION {UNION, INTERSECTION, MINUS}

}
