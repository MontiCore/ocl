package de.monticore.ocl2smt;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl2smt.ocl2smt.CleanExpr2SMTTest;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ContextDeclTest extends CleanExpr2SMTTest {

  @BeforeEach
  public void setup() throws IOException {
    Log.init();
    OCLMill.init();
    CD4CodeMill.init();
    CD2SMTMill.initDefault();
    parse("MinAuction.cd", "Context.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
  }

  @ParameterizedTest
  @ValueSource(strings = {"Context1"})
  public void testContextDeclUnSat(String inv) {
    testUnsatInv(Set.of(inv), "context");
  }

  @Test
  public void testInvariants() {
    testUnsatInv(Set.of("Context3", "Context2"), "context");
  }
}
