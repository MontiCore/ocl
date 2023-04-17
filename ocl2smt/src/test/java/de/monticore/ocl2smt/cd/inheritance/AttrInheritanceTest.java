package de.monticore.ocl2smt.cd.inheritance;

import de.monticore.ocl2smt.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AttrInheritanceTest extends ExpressionAbstractTest {
  @BeforeEach
  public void setup() throws IOException {
    super.initLogger();
    super.initMills();
    parse("/inheritance/attributes/attributes.cd", "/inheritance/attributes/attributes.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
  }

  @Test
  public void testAttributeInherAllHierarchies() {
    testInv("ALL", "/inheritance/attributes");
  }
}
