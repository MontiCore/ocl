package de.monticore.ocl2smt;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Typecheck extends ExpressionAbstractTest {
    @BeforeAll
    public static void setup() throws IOException {
        Log.init();
        OCLMill.init();
        CD4CodeMill.init();
        parse("/typecheck/typecheck.cd", "/typecheck/typecheck.ocl");
        Set<ASTOCLCompilationUnit> oclFiles = new HashSet<>();
        oclFiles.add(oclAST);
        ocl2SMTGenerator = new OCL2SMTGenerator(cdAST);
    }

    @Disabled
    @Test
    public void testTypeCheck() {
        testInv("TypeCheck");
    }
}
