package de.monticore.ocl2smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Status;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AssociationTest extends ExpressionAbstractTest {
    @BeforeEach
    public void setup() throws IOException {
        Log.init();
        OCLMill.init();
        CD4CodeMill.init();
        oclAST = OCL_Loader.parseOCLModel(Paths.get(RELATIVE_MODEL_PATH,"/associations/Association.ocl").toString());
        cdAST = OCL_Loader.parseCDModel(Paths.get(RELATIVE_MODEL_PATH,"/associations/Auction.cd").toString());
        cdContext = cd2SMTGenerator.cd2smt(cdAST);
        ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);
        solver = cdContext.getContext().mkSolver();
    }

    void testInv(String invName){
        List<BoolExpr> constraintList = new ArrayList<>();
        constraintList.add(addConstraint(invName));
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);

        OCLDiffGenerator oclDiffGenerator = new OCLDiffGenerator();
        ASTODArtifact od = oclDiffGenerator.buildOd(cdContext, invName, constraintList, cdAST.getCDDefinition());
        printOD(od);
    }
    @Test
    public void of_legal_age() {
        testInv("Of_legal_age");
    }
    @Disabled
    @Test
    public void different_ids() {
        testInv("Diff_ids");
    }

    @Disabled
    @Test
    public void same_person_in_wo_auction(){ testInv("One_in_2_auctions");}
}
