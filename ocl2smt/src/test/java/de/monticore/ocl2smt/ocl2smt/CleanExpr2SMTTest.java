package de.monticore.ocl2smt.ocl2smt;

import org.junit.jupiter.api.AfterEach;

/**
 * FIXME: This Class only exists because the clean-up process is not possible for all sub-classes of
 * ExpressionAbstractTest!
 */
public abstract class CleanExpr2SMTTest extends ExpressionAbstractTest {
  @AfterEach
  public void cleanUp() {
    if (ocl2SMTGenerator != null) {
      ocl2SMTGenerator.closeCtx();
    }
  }
}
