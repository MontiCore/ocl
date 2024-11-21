/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt.set;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.ocl2smt.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SetOperationsTest extends ExpressionAbstractTest {
  @BeforeEach
  public void setup() throws IOException {
    super.initLogger();
    super.initMills();
    CD2SMTMill.initDefault();
    parse("/setExpressions/setOperations/SetOp.cd", "/setExpressions/setOperations/SetOp.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"SetOp1", "SetOp2", "SetOp3", "SetOp4", "SetOp6", "SetOp8", "SetOp10", "SetOp11"})
  public void testSetOperationSat(String value) {
    testInv(value, "setOperation");
  }

  @ParameterizedTest
  @ValueSource(strings = {"SetOp5", "SetOp7", "SetOp9", "SetOp12"})
  public void testSetOperationUnSat(String value) {
    testUnsatInv(Set.of(value), "setOperation");
  }

  @AfterEach
  public void cleanUp() {
    if (ocl2SMTGenerator != null) {
      ocl2SMTGenerator.closeCtx();
    }
  }
}
