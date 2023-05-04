package de.monticore.ocl2smt;

import de.monticore.ocl2smt.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Set;

public class AssociationTest extends ExpressionAbstractTest {

  @BeforeEach
  public void setup() throws IOException {
    super.initLogger();
    super.initMills();
    parse("/associations/Association.cd", "/associations/Association.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Assoc1", "Assoc2", "Assoc3", "Assoc4", "Assoc5", "Assoc6", "Assoc7", "Assoc8", "Assoc11",
        "Assoc13"
      })
  public void testAssociationNavigationSat(String inv) {
    testInv(inv, "association");
  }

  @ParameterizedTest
  @ValueSource(strings = {"Assoc10", "Assoc9", "Assoc12", "Assoc14", "Assoc15"})
  public void testAssociationNavigationUnSat(String inv) {
    testUnsatInv(Set.of(inv), "association");
  }
}
