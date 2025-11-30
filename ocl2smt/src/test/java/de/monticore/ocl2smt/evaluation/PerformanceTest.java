package de.monticore.ocl2smt.evaluation;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.ocldiff.OCLDiffAbstractTest;
import de.monticore.ocl2smt.ocldiff.OCLDiffGenerator;
import de.monticore.ocl2smt.ocldiff.invariantDiff.OCLInvDiffResult;
import de.monticore.ocl2smt.util.OCL_Loader;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PerformanceTest extends OCLDiffAbstractTest {
  private List<ASTCDCompilationUnit> ast;
  private List<ASTOCLCompilationUnit> ocl;

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  @Tag("slow")
  @Test
  public void testPerformance() {
    PerformanceCDBuilder cdBuilder = new PerformanceCDBuilder();
    PerformanceOCLBuilder oclBuilder = new PerformanceOCLBuilder();

    final int starSize = 10;
    final int chainSize = 5;

    Optional<ASTCDCompilationUnit> cd = cdBuilder.buildCD(starSize,chainSize);
    Assertions.assertTrue(cd.isPresent());

    try {
      OCL_Loader.loadAndCheckCD(cd.get());
    } catch (Exception e) {
      Log.error("Unable to parse CD");
      Assertions.fail();
    }
    Assertions.assertFalse(cd.get().getCDDefinition().getCDClassesList().isEmpty());

    System.out.println(CD4CodeMill.prettyPrint(cd.get(),false));

    Optional<ASTOCLCompilationUnit> ocl = oclBuilder.buildOCL(starSize,chainSize,false);
    Assertions.assertTrue(ocl.isPresent());

    try {
      OCL_Loader.loadAndCheckOCL(ocl.get(),cd.get());
    } catch (Exception e) {
      Log.error("Unable to parse OCL Model");
      Assertions.fail();
    }

    System.out.println(OCLMill.prettyPrint(ocl.get(),false));

    Optional<ASTOCLCompilationUnit> ocl2 = oclBuilder.buildOCL(starSize,chainSize,true);
    Assertions.assertTrue(ocl2.isPresent());

    try {
      OCL_Loader.loadAndCheckOCL(ocl2.get(),cd.get());
    } catch (Exception e) {
      Log.error("Unable to parse OCL Model");
      Assertions.fail();
    }

    System.out.println(OCLMill.prettyPrint(ocl2.get(),false));

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

}
