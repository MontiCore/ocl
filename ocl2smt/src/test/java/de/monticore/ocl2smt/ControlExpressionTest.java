package de.monticore.ocl2smt;

import com.microsoft.z3.Status;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ControlExpressionTest extends ExpressionAbstractTest {

  @BeforeEach
  public void setup() throws IOException {
    Log.init();
    OCLMill.init();
    CD4CodeMill.init();
    parse("MinAuction.cd", "ControlExpr.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buidlContext());
    solver = ocl2SMTGenerator.cd2smtGenerator.getContext().mkSolver();
  }

  @Test
  public void if_then_else_boolean_sat() {
    addConstraint("ITE_sat");
    Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
  }

  @Test
  public void if_then_else_boolean_unsat1() {
    addConstraint("ITE_unsat1");
    Assertions.assertEquals(solver.check(), Status.UNSATISFIABLE);
  }

  @Test
  public void if_then_else_boolean_unsat2() {
    addConstraint("ITE_unsat2");
    Assertions.assertEquals(solver.check(), Status.UNSATISFIABLE);
  }
}
