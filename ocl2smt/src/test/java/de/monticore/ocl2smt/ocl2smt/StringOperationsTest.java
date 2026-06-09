package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.Status;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringOperationsTest extends CleanExpr2SMTTest {
  @BeforeEach
  public void setup() throws IOException {
    super.initLogger();
    super.initMills();
    parse("MinAuction.cd", "StringInv.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
  }

  @ParameterizedTest
  @ValueSource(strings = {"String1", "String3", "String4", "String6", "String8", "String10"})
  public void TestStringOperationsSAT(String value) {
    addConstraint(value);
    assertEquals(Status.SATISFIABLE, solver.check());
  }

  @ParameterizedTest
  @ValueSource(strings = {"String2", "String5", "String7", "String9", "String11"})
  public void TestStringOperationsUNSAT(String value) {
    addConstraint(value);
    assertEquals(Status.UNSATISFIABLE, solver.check());
  }
}
