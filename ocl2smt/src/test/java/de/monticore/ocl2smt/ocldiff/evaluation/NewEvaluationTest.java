package de.monticore.ocl2smt.ocldiff.evaluation;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.ocldiff.OCLDiffAbstractTest;
import de.monticore.ocl2smt.ocldiff.OCLDiffGenerator;
import de.monticore.odbasis._ast.ASTODArtifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Disabled
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
    ast = parseCD("newEvaluation/JavaProject.cd");
    ocl = parseOCl("newEvaluation/JavaProject.cd", "newEvaluation/JavaProject.ocl");
    ASTODArtifact witness =
            OCLDiffGenerator.oclWitnessFinite(
                    ast, Set.of(ocl), new HashSet<>(), new HashSet<>(), 1000, false);
    /* ASTODArtifact witness2 =
    OCLDiffGenerator.oclWitness(
            ast, Set.of(ocl), new HashSet<>(), new HashSet<>(), false);*/

    IOHelper.printOD(witness, Path.of("target/generated-test/newEvaluation"));
  }

  @Test
  @Disabled
  public void testMCGrammar() throws IOException {
    ast = parseCD("newEvaluation/MCGrammar.cd");
    ocl = parseOCl("newEvaluation/MCGrammar.cd", "newEvaluation/ArtifactModel.ocl");
  }
}
