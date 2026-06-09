package de.monticore.ocl2smt.evaluation;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.ocldiff.OCLDiffAbstractTest;
import de.monticore.ocl2smt.ocldiff.OCLDiffGenerator;
import de.monticore.ocl2smt.ocldiff.invariantDiff.OCLInvDiffResult;
import de.monticore.ocl2smt.util.OCL_Loader;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PerformanceTest extends OCLDiffAbstractTest {
  private List<ASTCDCompilationUnit> ast;
  private List<ASTOCLCompilationUnit> ocl;

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  @Tag("slow")
  @ParameterizedTest
  @MethodSource("sizes")
  public void testPerformance(int chainSize) {
    PerformanceCDBuilder cdBuilder = new PerformanceCDBuilder();
    PerformanceOCLBuilder oclBuilder = new PerformanceOCLBuilder();

    final int starSize = 10;

    // build the CD
    Optional<ASTCDCompilationUnit> cd = cdBuilder.buildCD(starSize,chainSize);
    assertTrue(cd.isPresent());

    try {
      OCL_Loader.loadAndCheckCD(cd.get());
    } catch (Exception e) {
      Log.error("Unable to parse CD");
      Assertions.fail();
    }
    assertFalse(cd.get().getCDDefinition().getCDClassesList().isEmpty());

    // build the first OCL model
    Optional<ASTOCLCompilationUnit> ocl = oclBuilder.buildOCL(starSize,chainSize,false);
    assertTrue(ocl.isPresent());

    try {
      OCL_Loader.loadAndCheckOCL(ocl.get(),cd.get());
    } catch (Exception e) {
      Log.error("Unable to parse OCL Model");
      Assertions.fail();
    }

    // build the second OCL model
    Optional<ASTOCLCompilationUnit> ocl2 = oclBuilder.buildOCL(starSize,chainSize,true);
    assertTrue(ocl2.isPresent());

    try {
      OCL_Loader.loadAndCheckOCL(ocl2.get(),cd.get());
    } catch (Exception e) {
      Log.error("Unable to parse OCL Model");
      Assertions.fail();
    }

    // compute SemDiff and measure the runtime
    OCLInvDiffResult res;

    long start = System.currentTimeMillis();
    CD2SMTMill.init(
        ClassStrategy.Strategy.DS,
        InheritanceData.Strategy.ME,
        AssociationStrategy.Strategy.DEFAULT);

    res =
        OCLDiffGenerator.oclDiffComp(
            cd.get(), Set.of(ocl.get()), Set.of(ocl2.get()) , new HashSet<>(), new HashSet<>(),
            1000,
            false);

    // print the results
    IOHelper.printInvDiffResult(
        res, Path.of(TARGET_DIR + "diff_star" + starSize + "_chain" + chainSize));
    double duration = (double) (System.currentTimeMillis() - start) / 1000;
    Log.info("| duration: " + duration, "Diff( " + starSize + "," + chainSize + ")");

  }

  @Test
  public void testMotivatingExample(){

    try {
      ASTCDCompilationUnit cd = parseCD("motivatingExample/BankManagementSystem.cd");
      ASTOCLCompilationUnit oldOCL = parseOCl("motivatingExample/BankManagementSystem.cd",
          "/motivatingExample/old.ocl");
      ASTOCLCompilationUnit newOCL = parseOCl("motivatingExample/BankManagementSystem.cd",
          "/motivatingExample/new.ocl");


    OCLInvDiffResult res;

    long start = System.currentTimeMillis();
    CD2SMTMill.init(
        ClassStrategy.Strategy.SS,
        InheritanceData.Strategy.SE,
        AssociationStrategy.Strategy.ONE2ONE);

    res =
        OCLDiffGenerator.oclDiffComp(
            cd, Set.of(oldOCL), Set.of(newOCL) , new HashSet<>(), new HashSet<>(),
            1000,
            false);

      IOHelper.printInvDiffResult(
          res, Path.of(TARGET_DIR + "motivating"));

    } catch (IOException e){
      Log.error("Unable to parse models");
      Assertions.fail();
    }
  }

  static Stream<Integer> sizes() {
    return IntStream.rangeClosed(1, 5).boxed();
  }

}
