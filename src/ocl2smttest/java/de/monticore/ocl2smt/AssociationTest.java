package de.monticore.ocl2smt;

import com.microsoft.z3.Status;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

public class AssociationTest extends ExpressionAbstractTest {
    @BeforeEach
    public void setup() throws IOException {
        Log.init();
        OCLMill.init();
        CD4CodeMill.init();
        oclAST = OCL_Loader.parseOCLModel(RELATIVE_MODEL_PATH + "/associations/Association.ocl");
        cdAST = OCL_Loader.parseCDModel(RELATIVE_MODEL_PATH + "/associations/Auction.cd");
        cdContext = cd2SMTGenerator.cd2smt(cdAST);
        ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);
        solver = cdContext.getContext().mkSolver();
    }

    @Test
    public void simple_assoc() {
        addConstraint("SimpleAssoc");
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);

        OCLDiffGenerator oclDiffGenerator = new OCLDiffGenerator();
        ASTODArtifact od = oclDiffGenerator.buildOd(cdContext,"SimpeAssoc",new ArrayList<>(),cdAST.getCDDefinition());

        printOD(od);
    }
}
