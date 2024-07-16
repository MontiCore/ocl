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
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Evaluation3Test extends OCL2SMTAbstractTest {

  private final String RELATIVE_MODEL_PATH = "src/test/resources/de/monticore/ocl2smt/evaluation3";
  private final String TARGET_DIR = "target/generated-test/evaluation3/";
  private ASTCDCompilationUnit newAst;
  private Set<ASTOCLCompilationUnit> newOcl;
  private Set<ASTOCLCompilationUnit> oldOCl;

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  @Test
  public void testWitness() {
    loadModels("v0", "v0");
    Log.info("Testing Consistency of the first model", this.getClass().getName());
    OCLDiffGenerator.oclWitness(newAst, oldOCl, new HashSet<>(), new HashSet<>(), false);
  }

  @Test
  public void evaluationV0_V1() {
    Log.info("Computing diff(version_0 vs version_1) \n\n", this.getClass().getName());
    evaluateOCLDiff("v0", "v1");
  }

  @Test
  public void evaluationV0_V2() {
    Log.info("Computing diff(version_0 vs version_2) \n\n", this.getClass().getName());
    evaluateOCLDiff("v0", "v2");
  }

  @Test
  public void evaluationV0_V3() {
    Log.info("Computing diff(version_0 vs version_3) \n\n", this.getClass().getName());
    evaluateOCLDiff("v0", "v3");
  }

  @Test
  public void evaluationV1_V2() {
    Log.info("Computing diff(version_1 vs version_2) \n\n", this.getClass().getName());
    evaluateOCLDiff("v1", "v2");
  }

  @Test
  public void evaluationV1_V3() {
    Log.info("Computing diff(version_1 vs version_3) \n\n", this.getClass().getName());
    evaluateOCLDiff("v1", "v3");
  }

  @Test
  public void evaluationV2_V3() {
    Log.info("Computing diff(version_2 vs version_3) \n\n", this.getClass().getName());
    evaluateOCLDiff("v2", "v3");
  }

  public void evaluateOCLDiff(String prevVersion, String currVersion) {

    loadModels(prevVersion, currVersion);
    OCLInvDiffResult res;

    long start = System.currentTimeMillis();
    CD2SMTMill.init(
        ClassStrategy.Strategy.DS,
        InheritanceData.Strategy.ME,
        AssociationStrategy.Strategy.DEFAULT);

    res =
        OCLDiffGenerator.oclDiffComp(
            newAst, oldOCl, newOcl, new HashSet<>(), new HashSet<>(), 1000, false);

    // print the results
    IOHelper.printInvDiffResult(
        res, Path.of(TARGET_DIR + "diff" + prevVersion + "_" + currVersion));
    double duration = (double) (System.currentTimeMillis() - start) / 1000;
    Log.info("| duration: " + duration, "Diff( " + currVersion + "," + currVersion + ")");
  }

  private void loadModels(String prevVersion, String currVersion) {
    oldOCl = new HashSet<>();
    newOcl = new HashSet<>();
    newAst = parseCD("/User.cd");
    oldOCl.add(parseOCl("User.cd", "/" + prevVersion + "/Invariants.ocl"));
    newOcl.add(parseOCl("/User.cd", "/" + currVersion + "/Invariants.ocl"));
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
