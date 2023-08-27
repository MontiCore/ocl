package de.monticore.ocl2smt;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLArtifact;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl2smt.util.OCLFieldAccessExpressionTrafo;
import de.monticore.ocl2smt.util.OCL_Loader;
import org.junit.jupiter.api.BeforeEach;

public class FieldAccessTrafoTest  extends  OCL2SMTAbstractTest{

    @BeforeEach
    public  void  setup(){
        initLogger();
        initMills();
    }

   public void transformFieldAccessTest(){

       ASTOCLArtifact ocl = OCL_Loader.loadAndCheckOCL()
       OCLFieldAccessExpressionTrafo trafo = new OCLFieldAccessExpressionTrafo();

       OCLTraverser traverser = OCLMill.traverser();
       traverser.add4CommonExpressions(trafo);

   }

}
