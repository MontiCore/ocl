package de.monticore.ocl2smt.cd.inheritance;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceStrategy;
import de.monticore.ocl2smt.ocl2smt.CleanExpr2SMTTest;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class AssocInheritanceTest extends CleanExpr2SMTTest {

  @BeforeEach
  public void setup() throws IOException {
    super.initLogger();
    super.initMills();
    parse("/inheritance/associations/Association.cd", "/inheritance/associations/Association.ocl");
  }

  @ParameterizedTest
  @MethodSource("cd2smtStrategies")
  public void testAssocInheritance(
      ClassStrategy.Strategy cs, InheritanceStrategy.Strategy is, AssociationStrategy.Strategy as) {
    // This one combination tends to run endlessly (120min+) -> ignore it
    Assumptions.assumeFalse(
        cs == ClassStrategy.Strategy.SSCOMB
            && is == InheritanceStrategy.Strategy.SE
            && as == AssociationStrategy.Strategy.DEFAULT,
        "This test is prone to timeouts");
    CD2SMTMill.init(cs, is, as);
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());

    String outDir = "/inheritance/associations";
    // Assertions.assertTrue(testInv("Assoc1",outDir));
    Assertions.assertTrue(testInv("Assoc2", outDir));
  }
}
