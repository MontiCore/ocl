package de.monticore.ocl2smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Sort;

public class SMTSet {
    protected String name;
    protected FuncDecl<? extends Sort> setFunction;
    protected BoolExpr filter;

    public SMTSet(String name, FuncDecl<? extends Sort> setFunction, BoolExpr filter) {
        this.filter = filter;
        this.setFunction = setFunction;
        this.name = name;
    }

    public BoolExpr getFilter() {
        return filter;
    }

    public FuncDecl<? extends Sort> getSetFunction() {
        return setFunction;
    }

    public String getName() {
        return name;
    }

}
