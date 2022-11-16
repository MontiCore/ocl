package de.monticore.ocl2smt;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TransitiveClosureTest extends ExpressionAbstractTest {
    @BeforeEach
    public void setup() throws IOException {
        Log.init();
        OCLMill.init();
        CD4CodeMill.init();
        parse("/Transitive-closure/transitiveClosure.cd", "/Transitive-closure/transitiveClosure.ocl");
        ocl2SMTGenerator = new OCL2SMTGenerator(cdAST);
    }


    @Test
    public void Test_SimpleTransitive_Closure() {
        testInv("SimpleTransitive_Closure");
    }

    @Test
    public void Test_SimpleTransitive_ClosureUNSAT() {
        testUnsatInv("SimpleTransitive_Closure_UNSAT");
    }

}
