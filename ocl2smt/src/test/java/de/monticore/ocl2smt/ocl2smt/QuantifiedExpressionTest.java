package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.Status;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class QuantifiedExpressionTest extends ExpressionAbstractTest {

  @BeforeEach
  public void setup() throws IOException {
    Log.init();
    OCLMill.init();
    CD4CodeMill.init();
    parse("MinAuction.cd", "QuantifiedExpr.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Quan1", "Quan2", "Quan5", "Quan6", "Quan8", "Quan9", "Quan13", "Quan15", "Quan16", "Quan17"
      })
  public void TestQuantifiedExpressionsSat(String value) {
    addConstraint(value);
    Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Quan3UNSAT",
        "Quan4UNSAT",
        "Quan7UNSAT",
        "Quan10UNSAT",
        "Quan11UNSAT",
        "Quan12UNSAT",
        "Quan14UNSAT",
        "Quan18UNSAT"
      })
  public void TestQuantifiedExpressionsUNSAT(String value) {
    addConstraint(value);
    Assertions.assertEquals(solver.check(), Status.UNSATISFIABLE);
  }
}
