package de.monticore.ocl2smt.cd.inheritance;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl2smt.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AttrInheritanceTest extends ExpressionAbstractTest {
  @BeforeEach
  public void setup() throws IOException {
    Log.init();
    OCLMill.init();
    CD4CodeMill.init();
    parse("/inheritance/attributes/attributes.cd", "/inheritance/attributes/attributes.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
  }

  @Test
  public void testAttributeInherAllHierarchies() {
    testInv("ALL", "/inheritance/attributes");
  }
}
