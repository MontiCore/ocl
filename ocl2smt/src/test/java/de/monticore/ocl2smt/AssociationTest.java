package de.monticore.ocl2smt;


import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AssociationTest extends ExpressionAbstractTest {

    @BeforeEach
    public void setup() throws IOException {
        Log.init();
        OCLMill.init();
        CD4CodeMill.init();
        parse("/associations/Association.cd", "/associations/Association.ocl");
        ocl2SMTGenerator = new OCL2SMTGenerator(cdAST);
    }


    @Test
    public void of_legal_age() {
        testInv("Of_legal_age");
    }

    @Test
    public void different_ids() {
        testInv("Diff_ids");
    }

    @Test
    public void atLeast2Person() {
        testInv("AtLeast_2_Person");
    }

    @Test
    public void Same_Person_in_2_Auction() {
        testInv("Same_Person_in_2_Auction");
    }


    @Test
    public void TestNestedFieldAccessExpr() {
        testInv("NestedFieldAccessExpr");
    }


    @Test
    public void TestNestedFieldAccess_inDec_UNSAT() {
        testInv("NestedFieldAccess2_UNSAT");
    }

}
