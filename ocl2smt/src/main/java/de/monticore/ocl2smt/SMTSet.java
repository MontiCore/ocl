package de.monticore.ocl2smt;

import com.microsoft.z3.*;
import de.se_rwth.commons.logging.Log;

public class SMTSet {
    protected String name;
    protected FuncDecl<BoolSort> setFunction;
    protected BoolExpr definition;

    public SMTSet(String name, FuncDecl<BoolSort> setFunction, BoolExpr filter) {
        this.definition = filter;
        this.setFunction = setFunction;
        this.name = name;
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

    public Sort getSort(){
        return this.setFunction.getDomain()[0];
    }

    public static SMTSet mkSetUnion(SMTSet lefSet, SMTSet rightSet, Context ctx){
        if (!lefSet.getSort().equals(rightSet.getSort())){
            Log.error("conversion of Set Union with set of different type not yet implemented ");
            //TODO:complete implementation
        }
        String setName =lefSet.getName()+"union"+rightSet.getName();
        FuncDecl<BoolSort> setFunc = ctx.mkFuncDecl(setName,lefSet.getSort(),ctx.mkBoolSort());
        Expr<Sort> obj =  ctx.mkConst("uObj", lefSet.getSort());

        BoolExpr union = ctx.mkForall(new Expr[]{obj}, ctx.mkEq(ctx.mkApp(setFunc, obj), ctx.mkOr(
                        (BoolExpr) ctx.mkApp(lefSet.getSetFunction(), obj), (BoolExpr) ctx.mkApp(rightSet.getSetFunction(), obj)))
                , 0, null, null, null, null);

        BoolExpr definition = ctx.mkAnd(lefSet.getDefinition(),rightSet.getDefinition(),union);

        return new SMTSet(setName,setFunc,definition);
    }

}
