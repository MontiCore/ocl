package de.monticore.ocl2smt.evaluation;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.ocldiff.OCLDiffAbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PerformanceTest extends OCLDiffAbstractTest {
  private List<ASTCDCompilationUnit> ast;
  private List<ASTOCLCompilationUnit> ocl;

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  @Test
  public void testPerformance() {
    //TODO: implement
  }

}
