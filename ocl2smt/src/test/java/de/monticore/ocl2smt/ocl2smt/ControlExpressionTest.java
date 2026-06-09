/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.Params;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ControlExpressionTest extends CleanExpr2SMTTest {

  @BeforeEach
  public void setup() throws IOException {
    super.initLogger();
    super.initMills();
    parse("MinAuction.cd", "ControlExpr.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
    solver = ocl2SMTGenerator.getCD2SMTGenerator().getContext().mkSolver();
    if (CD2SMTGenerator.isSeedEnabled()) {
      // Set the random seed for determinism
      Params p = ocl2SMTGenerator.getCD2SMTGenerator().getContext().mkParams();
      p.add("random_seed", CD2SMTGenerator.getSeed()); // Choose your seed value
      solver.setParameters(p);
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"ITE_UNSAT1", "ITE_UNSAT2", "Cond_UNSAT"})
  public void testControlExprUNSAT(String value) {
    addConstraint(value);
    assertEquals(Status.UNSATISFIABLE, solver.check());
  }

  @ParameterizedTest
  @ValueSource(strings = {"ITE_SAT", "Cond_SAT"})
  public void testControlExprSAT(String value) {
    addConstraint(value);
    assertEquals(Status.SATISFIABLE, solver.check());
  }
}
