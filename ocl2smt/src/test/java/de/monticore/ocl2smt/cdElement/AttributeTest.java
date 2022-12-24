package de.monticore.ocl2smt.cdElement;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl2smt.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

public class AttributeTest extends ExpressionAbstractTest {
    @BeforeEach
    public void setup() throws IOException {
        Log.init();
        OCLMill.init();
        CD4CodeMill.init();
        parse("/cdElement/attribute/attribute.cd", "/cdElement/attribute/attribute.ocl");
        ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
    }
    @Disabled
    @ParameterizedTest
    @ValueSource(
            strings = {"Attr1"})
    public void testAttributeAccess(String inv) {
        testInv(inv);
    }
}
