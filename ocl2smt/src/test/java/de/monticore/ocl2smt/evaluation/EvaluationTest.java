package de.monticore.ocl2smt.evaluation;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.ocldiff.OCLDiffAbstractTest;
import de.monticore.ocl2smt.ocldiff.OCLDiffGenerator;
import de.monticore.ocl2smt.ocldiff.invariantDiff.OCLInvDiffResult;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPDiffResult;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.*;

public class EvaluationTest extends OCLDiffAbstractTest {
  private List<ASTCDCompilationUnit> ast;
  private List<ASTOCLCompilationUnit> ocl;

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  // private List<String> changes;

  private void loadModels() throws IOException {
    ast = new ArrayList<>();
    ocl = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      ast.add(parseCD("evaluation/v" + i + "/ArtifactKindModelV" + i + ".cd"));
      ocl.add(
          parseOCl(
              "evaluation/v" + i + "/ArtifactKindModelV" + i + ".cd",
              "evaluation/v" + i + "/ArtifactKindModelV" + i + ".ocl"));
    }
  }

  @Test
  @Tag("non-terminating")
  public void evaluateOCLDiff() throws IOException {
    Log.info(
        "------------------------------------------------------------------------------------\n \n \n ",
        "Evaluation");
    loadModels();
    // loadChanges();
    OCLInvDiffResult res;

    for (int i = 0; i < 6; i++) {
      for (int j = 1; j < 7; j++) {
        if (i < j) {

          long start = System.currentTimeMillis();

          res =
              OCLDiffGenerator.oclDiff(
                  ast.get(j),
                  Set.of(ocl.get(i)),
                  Set.of(ocl.get(j)),
                  new HashSet<>(),
                  new HashSet<>(),
                  false);

          // print the results
          IOHelper.printInvDiffResult(res, Path.of(TARGET_DIR + "Evaluation/OCL_V" + i + "" + (j)));

          Log.info(
              "| duration: " + (double) (System.currentTimeMillis() - start) / 1000,
              "Diff( V" + i + " ,V" + (j) + ")");
        }
      }
    }
    Log.info("\n \n \n", "");
    Log.info(
        "------------------------------------------------------------------------------------",
        "Evaluation");
  }

  @Test
  @Tag("non-terminating")
  public void evaluateCDAndOCLDiff() throws IOException {
    Log.info(
        "------------------------------------------------------------------------------------\n \n \n ",
        "Evaluation");
    loadModels();
    // loadChanges();
    OCLInvDiffResult res;

    for (int i = 0; i < 6; i++) {
      for (int j = 1; j < 7; j++) {
        if (i < j) {

          long start = System.currentTimeMillis();

          res =
              OCLDiffGenerator.oclDiff(
                  ast.get(i),
                  ast.get(j),
                  Set.of(ocl.get(i)),
                  Set.of(ocl.get(j)),
                  new HashSet<>(),
                  new HashSet<>(),
                  false);

          // print the results
          IOHelper.printInvDiffResult(
              res, Path.of(TARGET_DIR + "Evaluation/Diff_V" + i + "" + (j)));

          Log.info(
              "| duration: " + (double) (System.currentTimeMillis() - start) / 1000,
              "Diff( V" + i + " ,V" + (j) + ")");
        }
      }
    }
    Log.info("\n \n \n", "");
    Log.info(
        "------------------------------------------------------------------------------------",
        "Evaluation");
  }

  @Test
  @Tag("non-terminating")
  public void evaluateOpDiff() throws IOException {
    Log.info(
        "------------------------------------------------------------------------------------\n \n \n ",
        "Evaluation");
    loadModels();
    // loadChanges();
    OCLOPDiffResult res;

    long start = System.currentTimeMillis();

    Assertions.assertNotNull(
        IOHelper.getMethodSignature(Set.of(ocl.get(6)), "JavaSourceFile.compile"));

    res =
        OCLDiffGenerator.oclOPDiffV1(
            ast.get(6),
            Set.of(ocl.get(5)),
            Set.of(ocl.get(6)),
            IOHelper.getMethodSignature(Set.of(ocl.get(6)), "JavaSourceFile.compile"),
            false);

    // print the results
    IOHelper.printOPDiff(res, Path.of(TARGET_DIR + "Evaluation/Op_V" + 5 + "" + (6)));

    Log.info(
        "| duration: " + (double) (System.currentTimeMillis() - start) / 1000,
        "Diff( V" + 5 + " ,V" + (6) + ")");

    Assertions.assertNotNull(
        IOHelper.getMethodSignature(Set.of(ocl.get(5)), "JavaSourceFile.compile"));

    res =
        OCLDiffGenerator.oclOPDiffV1(
            ast.get(5),
            Set.of(ocl.get(6)),
            Set.of(ocl.get(5)),
            IOHelper.getMethodSignature(Set.of(ocl.get(5)), "JavaSourceFile.compile"),
            false);

    // print the results
    IOHelper.printOPDiff(res, Path.of(TARGET_DIR + "Evaluation/Op_V" + 6 + "" + (5)));

    Log.info(
        "| duration: " + (double) (System.currentTimeMillis() - start) / 1000,
        "Diff( V" + 6 + " ,V" + (5) + ")");

    Log.info("\n \n \n", "");
    Log.info(
        "------------------------------------------------------------------------------------",
        "Evaluation");
  }
}
