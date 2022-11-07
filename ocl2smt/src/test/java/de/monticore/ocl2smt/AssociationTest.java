package de.monticore.ocl2smt;


import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AssociationTest extends ExpressionAbstractTest {

    @BeforeAll
    public static void setup() throws IOException {
        Log.init();
        OCLMill.init();
        CD4CodeMill.init();
        parse("/associations/Association.cd", "/associations/Association.ocl");
        ocl2SMTGenerator = new OCL2SMTGenerator(cdAST);
    }


    @Test
    public void of_legal_age() {
        testInv( "Of_legal_age");
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

   /* @Test
    public void transitive_closure() throws IOException {
        parse("/Transitive-closure/transitiveClosure.cd", "/Transitive-closure/transitiveClosure.ocl");
        ocl2SMTGenerator = new OCL2SMTGenerator(cdAST);
        ocl2SMTGenerator.cd2smtGenerator.cd2smt(cdAST, (buildContext()));
        Set<ASTOCLCompilationUnit> ocls = new HashSet<>();
        ocls.add(oclAST);
        ASTODArtifact od = OCLDiffGenerator.oclWitness(cdAST, ocls, false);
        printOD(od);
    }*/
}
