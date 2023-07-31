package de.monticore.ocl2smt.ocldiff.evaluation;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.ocldiff.OCLDiffAbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class NewEvaluationTest extends OCLDiffAbstractTest {
  private ASTCDCompilationUnit ast;

  private ASTOCLCompilationUnit ocl;

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  @Test
  public void testArtifactModel() throws IOException {
    ast = parseCD("newEvaluation/ArtifactModel.cd");
    ocl = parseOCl("newEvaluation/ArtifactModel.cd", "newEvaluation/ArtifactModel.ocl");
  }

  @Test
  public void testJavaProject() throws IOException {
    // ast = parseCD("newEvaluation/JavaProject.cd");
    ocl = parseOCl("newEvaluation/JavaProject.cd", "newEvaluation/JavaProject.ocl");
    //   OCLInvDiffResult witness = OCLDiffGenerator.oclDiff(ast, Set.of(ocl),new HashSet<>(),new HashSet<>(),new HashSet<>(),true);
    // OCLInvDiffResult witnessFinite = OCLDiffGenerator.oclDiffFinite(ast, Set.of(ocl),new HashSet<>(),new HashSet<>(),new HashSet<>(),1000,true);
    // IOHelper.printInvDiffResult(witness, Path.of("target/generated-test/newEvaluation"));
  }

  @Test
  @Disabled
  public void testMCGrammar() throws IOException {
    ast = parseCD("newEvaluation/MCGrammar.cd");
    ocl = parseOCl("newEvaluation/MCGrammar.cd", "newEvaluation/ArtifactModel.ocl");
  }
}
