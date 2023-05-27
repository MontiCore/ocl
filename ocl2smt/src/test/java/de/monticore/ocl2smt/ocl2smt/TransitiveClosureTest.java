/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TransitiveClosureTest extends ExpressionAbstractTest {
  @BeforeEach
  public void setup() throws IOException {
    super.initLogger();
    super.initMills();
    parse("/Transitive-closure/transitiveClosure.cd", "/Transitive-closure/transitiveClosure.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
    Date a;
  }

  @Test
  public void Test_SimpleTransitive_Closure() {
    testInv("SimpleTransitive_Closure", "transitive-closure");
  }

  @Test
  public void Test_SimpleTransitive_ClosureUNSAT() {
    testUnsatInv(Set.of("SimpleTransitive_Closure_UNSAT"), "transitive-closure");
  }
}
