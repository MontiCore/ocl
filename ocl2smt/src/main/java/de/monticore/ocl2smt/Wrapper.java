package de.monticore.ocl2smt;

import com.microsoft.z3.Context;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.helpers.Helper;



public class Wrapper {
    protected CD2SMTGenerator posCD2smt = new CD2SMTGenerator();

    protected  CD2SMTGenerator negCD2smt = new CD2SMTGenerator();




    public  void computeSemDiff(ASTCDCompilationUnit ast , Context ctx, ASTOCLCompilationUnit in , ASTOCLCompilationUnit notin){
        posCD2smt.cd2smt(ast,ctx);

        Helper.buildPreCD(ast);

        negCD2smt.cd2smt(ast,ctx);


    }
}
