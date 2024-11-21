/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class TransitiveClosureTest extends CleanExpr2SMTTest {
  @BeforeEach
  public void setup() throws IOException {
    super.initLogger();
    super.initMills();
    parse("/Transitive-closure/transitiveClosure.cd", "/Transitive-closure/transitiveClosure.ocl");
  }

  @ParameterizedTest
  @MethodSource("cd2smtStrategies")
  public void Test_SimpleTransitive_Closure(
      ClassStrategy.Strategy cs, InheritanceData.Strategy is, AssociationStrategy.Strategy as) {
    CD2SMTMill.init(cs, is, as);
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
    testInv("SimpleTransitive_Closure", "transitive-closure");
  }

  @Test
  public void Test_SimpleTransitive_ClosureUNSAT() {
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
    testUnsatInv(Set.of("SimpleTransitive_Closure_UNSAT"), "transitive-closure");
  }
}
