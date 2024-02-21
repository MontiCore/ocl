package de.monticore.ocl2smt;

import static de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy.Strategy.*;
import static de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData.Strategy.ME;
import static de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData.Strategy.SE;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData;
import de.monticore.ocl2smt.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class AssociationTest extends ExpressionAbstractTest {

  @BeforeEach
  public void setup() throws IOException {
    super.initLogger();
    super.initMills();
    parse("/associations/Association.cd", "/associations/Association.ocl");
  }

  @ParameterizedTest
  @MethodSource("cd2smtStrategies")
  public void testAssociationSat(
      ClassStrategy.Strategy cs, InheritanceData.Strategy is, AssociationStrategy.Strategy as) {
    Assumptions.assumeFalse(cs == SS && is == ME);
    Assumptions.assumeFalse(cs == SSCOMB && is == SE);

    CD2SMTMill.init(cs, is, as);
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());

    String outDir = "association/sat" + cs.name() + "_" + is.name() + "_" + as.name();

    Assertions.assertTrue(testInv("Assoc1", outDir));
    Assertions.assertTrue(testInv("Assoc2", outDir));
    Assertions.assertTrue(testInv("Assoc3", outDir));
    Assertions.assertTrue(testInv("Assoc4", outDir));
    Assertions.assertTrue(testInv("Assoc5", outDir));
    Assertions.assertTrue(testInv("Assoc6", outDir));
    Assertions.assertTrue(testInv("Assoc7", outDir));
    Assertions.assertTrue(testInv("Assoc8", outDir));
    Assertions.assertTrue(testInv("Assoc11", outDir));
    Assertions.assertTrue(testInv("Assoc13", outDir));
  }

  @ParameterizedTest
  @MethodSource("cd2smtStrategies")
  public void testAssociationUnSat(
      ClassStrategy.Strategy cs, InheritanceData.Strategy is, AssociationStrategy.Strategy as) {

    CD2SMTMill.init(cs, is, as);
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());

    String outDir = "association/unsat" + cs.name() + "_" + is.name() + "_" + as.name();

    // Assertions.assertTrue(testUnsatInv(Set.of("Assoc9"), outDir)); todo  is true/false
    // depending// on the strategy.
    Assertions.assertTrue(testUnsatInv(Set.of("Assoc12"), outDir));
    Assertions.assertTrue(testUnsatInv(Set.of("Assoc14"), outDir));
    Assertions.assertTrue(testUnsatInv(Set.of("Assoc15"), outDir));
  }
}
