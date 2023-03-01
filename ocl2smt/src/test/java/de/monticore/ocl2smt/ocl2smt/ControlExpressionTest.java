/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.Status;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ControlExpressionTest extends ExpressionAbstractTest {

  @BeforeEach
  public void setup() throws IOException {
    Log.init();
    OCLMill.init();
    CD4CodeMill.init();
    parse("MinAuction.cd", "ControlExpr.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
    solver = ocl2SMTGenerator.getCD2SMTGenerator().getContext().mkSolver();
  }


  @ParameterizedTest
  @ValueSource(strings = {"ITE_UNSAT1","ITE_UNSAT2", "Cond_UNSAT"})
  public void  testControlExprUNSAT(String value){
    addConstraint(value);
    Assertions.assertEquals(solver.check(), Status.UNSATISFIABLE);
  }

  @ParameterizedTest
  @ValueSource(strings = {"ITE_SAT","Cond_SAT"})
  public void testControlExprSAT(String value) {
    addConstraint(value);
    Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
  }


}
