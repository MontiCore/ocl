package de.monticore.ocl2smt;

import com.microsoft.z3.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;


public class AssociationTest extends ExpressionAbstractTest {
    @BeforeEach
    public void setup() throws IOException {
        parse("/associations/Auction.cd", "/associations/Association.ocl");
        cdContext = cd2SMTGenerator.cd2smt(cdAST);
        ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);
        solver = cdContext.getContext().mkSolver();
       /* Log.init();
        OCLMill.init();
        CD4CodeMill.init();
        oclAST = OCL_Loader.parseOCLModel(RELATIVE_MODEL_PATH + "/associations/Association.ocl");
        cdAST = OCL_Loader.parseCDModel(RELATIVE_MODEL_PATH + "/associations/Auction.cd");
        cdContext = cd2SMTGenerator.cd2smt(cdAST);
        ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);
        solver = cdContext.getContext().mkSolver();*/
    }

    @Test
    @Disabled
    public void simple_assoc() {
        addConstraint("SimpleAssoc");
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);

       // OCLDiffGenerator oclDiffGenerator = new OCLDiffGenerator();
      //  ASTODArtifact od = oclDiffGenerator.buildOd(cdContext,"SimpeAssoc",new ArrayList<>(),cdAST.getCDDefinition());

      //  printOD(od);
    }
}
