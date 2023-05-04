package de.monticore.ocl2smt.cd.inheritance;

import de.monticore.ocl2smt.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AssocInheritanceTest extends ExpressionAbstractTest {

  @BeforeEach
  public void setup() throws IOException {
    super.initLogger();
    super.initMills();
    parse("/inheritance/associations/Association.cd", "/inheritance/associations/Association.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
  }

  @ParameterizedTest
  @ValueSource(strings = {"Assoc1", "Assoc2"})
  public void testAssocInheritance(String value) {
    testInv(value, "/inheritance/associations");
  }
}
