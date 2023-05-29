package de.monticore.ocl2smt.cd.inheritance;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceStrategy;
import de.monticore.ocl2smt.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Set;

public class AssocInheritanceTest extends ExpressionAbstractTest {

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
        CD2SMTMill.init(cs, is, as);
        ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());

        for (String inv : Set.of("Assoc1", "Assoc2")) {
            testInv(inv, "/inheritance/associations");
        }
    }
}
