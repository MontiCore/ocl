package de.monticore.ocl2smt;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TypeCheck extends ExpressionAbstractTest {
  @BeforeEach
  public void setup() throws IOException {
    Log.init();
    OCLMill.init();
    CD4CodeMill.init();
    parse("/typecheck/typecheck.cd", "/typecheck/typecheck.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST);
  }

  @Disabled
  @Test
  public void testTypeCheck() {
    testInv("TypeCheck");
  }
}
