package de.monticore.ocl2smt;

import static de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy.Strategy.SS;
import static de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy.Strategy.SSCOMB;
import static de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData.Strategy.ME;
import static de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData.Strategy.SE;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData;
import de.monticore.ocl2smt.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import java.io.IOException;
import java.util.List;
import java.util.Set;
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

    List<String> invs =
        List.of(
            "Assoc1", "Assoc2", "Assoc3", "Assoc4", "Assoc5", "Assoc6", "Assoc7", "Assoc8",
            "Assoc11", "Assoc13");

    CD2SMTMill.init(cs, is, as);
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());

    for (String inv : invs) {
      testInv(inv, "association/" + cs.name() + "_" + is.name() + "_" + as.name());
    }
  }

  @ParameterizedTest
  @MethodSource("cd2smtStrategies")
  public void testAssociationUnSat(
      ClassStrategy.Strategy cs, InheritanceData.Strategy is, AssociationStrategy.Strategy as) {

    List<String> invs = List.of("Assoc9", "Assoc12", "Assoc14", "Assoc15");
    CD2SMTMill.init(cs, is, as);
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());

    for (String inv : invs) {
      testUnsatInv(
          Set.of(inv), "association/sssss" + cs.name() + "_" + is.name() + "_" + as.name());
    }
  }
}
