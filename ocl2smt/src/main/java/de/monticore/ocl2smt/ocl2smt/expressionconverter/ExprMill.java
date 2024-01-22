package de.monticore.ocl2smt.ocl2smt.expressionconverter;

public class ExprMill {
    public static ExprBuilder exprBuilder(){
        return new Z3ExprBuilder();
    }
}
