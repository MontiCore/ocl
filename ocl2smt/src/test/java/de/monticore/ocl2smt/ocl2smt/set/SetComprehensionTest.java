/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt.set;

import de.monticore.ocl2smt.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Set;

public class SetComprehensionTest extends ExpressionAbstractTest {
  @BeforeEach
  public void setup() throws IOException {
    super.initLogger();
    super.initMills();
    parse(
        "/setExpressions/setComprehension/SetComp.cd",
        "/setExpressions/setComprehension/SetComp.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        /*"Test1", "Test3", "Test7", "Test8", "Test10", "Test12",*/
        "Test13"
      })
  public void testSetComprehensionSat(String value) {
    testInv(value, "setComprehension");
  }

  @ParameterizedTest
  @ValueSource(strings = {"Test2", "Test4", "Test5", "Test9", "Test11"})
  public void testSetComprehensionUnSat(String value) {
    testUnsatInv(Set.of(value), "setComprehension");
  }
}
