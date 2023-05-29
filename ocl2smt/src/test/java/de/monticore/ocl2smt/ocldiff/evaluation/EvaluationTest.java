package de.monticore.ocl2smt.ocldiff.evaluation;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.ocldiff.OCLDiffAbstractTest;
import de.monticore.ocl2smt.ocldiff.OCLDiffGenerator;
import de.monticore.ocl2smt.ocldiff.invariantDiff.OCLInvDiffResult;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPDiffResult;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class EvaluationTest extends OCLDiffAbstractTest {
  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  private List<ASTCDCompilationUnit> ast;

  private List<ASTOCLCompilationUnit> ocl;

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
  @Disabled
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

          res = OCLDiffGenerator.oclDiff(ast.get(j), Set.of(ocl.get(i)), Set.of(ocl.get(j)), false);

          // print the results
          printResult(res, "Evaluation/OCL_V" + i + "" + (j));

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
  @Disabled
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
                  ast.get(i), ast.get(j), Set.of(ocl.get(i)), Set.of(ocl.get(j)), false);

          // print the results
          printResult(res, "Evaluation/Diff_V" + i + "" + (j));

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
  @Disabled
  public void evaluateOpDiff() throws IOException {
    Log.info(
        "------------------------------------------------------------------------------------\n \n \n ",
        "Evaluation");
    loadModels();
    // loadChanges();
    OCLOPDiffResult res;

    long start = System.currentTimeMillis();

    Assertions.assertNotNull(getMethodSignature(Set.of(ocl.get(6)), "JavaSourceFile.compile"));

    res =
        OCLDiffGenerator.oclOPDiffV1(
            ast.get(6),
            Set.of(ocl.get(5)),
            Set.of(ocl.get(6)),
            getMethodSignature(Set.of(ocl.get(6)), "JavaSourceFile.compile"),
            false);

    // print the results
    printOPDiff(res, "Evaluation/Op_V" + 5 + "" + (6));

    Log.info(
        "| duration: " + (double) (System.currentTimeMillis() - start) / 1000,
        "Diff( V" + 5 + " ,V" + (6) + ")");

    Assertions.assertNotNull(getMethodSignature(Set.of(ocl.get(5)), "JavaSourceFile.compile"));

    res =
        OCLDiffGenerator.oclOPDiffV1(
            ast.get(5),
            Set.of(ocl.get(6)),
            Set.of(ocl.get(5)),
            getMethodSignature(Set.of(ocl.get(5)), "JavaSourceFile.compile"),
            false);

    // print the results
    printOPDiff(res, "Evaluation/Op_V" + 6 + "" + (5));

    Log.info(
        "| duration: " + (double) (System.currentTimeMillis() - start) / 1000,
        "Diff( V" + 6 + " ,V" + (5) + ")");

    Log.info("\n \n \n", "");
    Log.info(
        "------------------------------------------------------------------------------------",
        "Evaluation");
  }
}
