package de.monticore.ocl2smt.evaluation;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.OCL2SMTAbstractTest;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.ocldiff.OCLDiffGenerator;
import de.monticore.ocl2smt.ocldiff.invariantDiff.OCLInvDiffResult;
import de.monticore.ocl2smt.util.OCL_Loader;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Use finite strategies which currently do not work well")
public class Evaluation2Test extends OCL2SMTAbstractTest {

  private final String RELATIVE_MODEL_PATH = "src/test/resources/de/monticore/ocl2smt/evaluation2";
  private final String TARGET_DIR = "target/generated-test/evaluation2/";
  private ASTCDCompilationUnit newAst;
  private ASTCDCompilationUnit oldAst;
  private Set<ASTOCLCompilationUnit> newOcl;
  private Set<ASTOCLCompilationUnit> oldOCl;

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
    loadModels();
  }

  @Test
  public void checkConsistencyOldVersionTest() {

    ASTODArtifact witness =
        OCLDiffGenerator.oclWitnessComp(
            oldAst, oldOCl, new HashSet<>(), new HashSet<>(), 100, false);

    Assertions.assertNotNull(witness);
    IOHelper.printOD(witness, Path.of(TARGET_DIR + "/witness/old"));
  }

  @Test
  public void checkConsistencyNewVersionTest() {

    ASTODArtifact witness =
        OCLDiffGenerator.oclWitnessComp(
            newAst, newOcl, new HashSet<>(), new HashSet<>(), 100, false);

    Assertions.assertNotNull(witness);
    IOHelper.printOD(witness, Path.of(TARGET_DIR + "/witness/new"));
  }

  @Test
  public void evaluateOCLDiff() {
    OCLInvDiffResult res;
    CD2SMTMill.init(
        ClassStrategy.Strategy.SSCOMB,
        InheritanceData.Strategy.SE,
        AssociationStrategy.Strategy.DEFAULT);
    long start = System.currentTimeMillis();

    res = OCLDiffGenerator.oclDiff(newAst, oldOCl, newOcl, new HashSet<>(), new HashSet<>(), false);

    // print the results
    IOHelper.printInvDiffResult(res, Path.of(TARGET_DIR + "diff_V01"));
    double duration = (double) (System.currentTimeMillis() - start) / 1000;
    Log.info("| duration: " + duration, "Diff( V0 ,V1)");
  }

  private void loadModels() {
    oldOCl = new HashSet<>();
    newOcl = new HashSet<>();
    oldAst = parseCD("/v0/JavaProjectV0.cd");
    newAst = parseCD("/v1/JavaProjectV1.cd");
    oldOCl.add(parseOCl("/v0/JavaProjectV0.cd", "/v0/JavaProjectV0.ocl"));
    newOcl.add(parseOCl("/v1/JavaProjectV1.cd", "/v1/JavaProjectV1.ocl"));
  }

  protected ASTCDCompilationUnit parseCD(String cdFileName) {
    try {
      return OCL_Loader.loadAndCheckCD(Paths.get(RELATIVE_MODEL_PATH, cdFileName).toFile());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected ASTOCLCompilationUnit parseOCl(String cdFileName, String oclFileName) {
    try {
      return OCL_Loader.loadAndCheckOCL(
          Paths.get(RELATIVE_MODEL_PATH, oclFileName).toFile(),
          Paths.get(RELATIVE_MODEL_PATH, cdFileName).toFile());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
