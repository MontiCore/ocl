package de.monticore.ocl2smt.ocldiff;

import de.monticore.cd4analysis.prettyprint.CD4AnalysisFullPrettyPrinter;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.helpers.Helper;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OPConstraintTest extends OCLDiffAbstractTest {
  @BeforeEach
  public void setUp() {
    Log.init();
    OCLMill.init();
    CD4CodeMill.init();
  }

  @Test
  public void TestBuildPreCD() throws IOException {
    CD4AnalysisFullPrettyPrinter printer = new CD4AnalysisFullPrettyPrinter();
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/pre-post.cd");
    System.out.println(printer.prettyprint(ast));
    Helper.buildPreCD(ast);
    System.out.println(printer.prettyprint(ast));
  }

  @Test
  public void testOPConstraintWitness() throws IOException {
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/pre-post.cd");

    Set<ASTOCLCompilationUnit> posOCl = new HashSet<>();
    posOCl.add(parseOCl("/post-pre-conditions/pre-post.cd", "/post-pre-conditions/neg.ocl"));

    ASTODArtifact witness = OCLOPDiff.oclWitness(ast, posOCl, false);
    Assertions.assertNotNull(witness);
    printOD(witness);
  }
}
