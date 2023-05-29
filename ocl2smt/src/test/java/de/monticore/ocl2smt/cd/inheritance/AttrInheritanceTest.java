package de.monticore.ocl2smt.cd.inheritance;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceStrategy;
import de.monticore.ocl2smt.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;

import static de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy.Strategy.SS;
import static de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData.Strategy.ME;

public class AttrInheritanceTest extends ExpressionAbstractTest {
  @BeforeEach
  public void setup() throws IOException {
    super.initLogger();
    super.initMills();
    parse("/inheritance/attributes/attributes.cd", "/inheritance/attributes/attributes.ocl");
  }

    @ParameterizedTest
    @MethodSource("cd2smtStrategies")
    public void testAttributeInherAllHierarchies(
            ClassStrategy.Strategy cs, InheritanceStrategy.Strategy is, AssociationStrategy.Strategy as) {
        Assumptions.assumeFalse(cs == SS && is == ME);
        CD2SMTMill.init(cs, is, as);
        ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
        testInv("ALL", "/inheritance/attributes");
    }
}
