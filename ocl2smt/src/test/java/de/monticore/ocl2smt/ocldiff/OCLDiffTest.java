/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocldiff;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceStrategy;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.ocldiff.invariantDiff.OCLInvDiffResult;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odbasis._ast.ASTODElement;
import de.monticore.odbasis._ast.ASTODNamedObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.gradle.internal.impldep.org.junit.Assert.assertFalse;
import static org.gradle.internal.impldep.org.testng.Assert.assertEquals;
import static org.gradle.internal.impldep.org.testng.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class OCLDiffTest extends OCLDiffAbstractTest {
  private final String TARGET_DIR = "target/generated-test/oclDiff/";

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  @ParameterizedTest
  @MethodSource("cd2smtStrategies")
  public void testOCLDiffOneCD(
          ClassStrategy.Strategy cs, InheritanceStrategy.Strategy is, AssociationStrategy.Strategy as)
      throws IOException {
    CD2SMTMill.init(cs, is, as);
    OCLInvDiffResult diff = computeDiffOneCD("Auction.cd", "old.ocl", "new.ocl");
    IOHelper.printInvDiffResult(diff, Path.of(TARGET_DIR + "OCLDiffOneCD"));
    assertEquals(4, diff.getDiffWitness().size());

    assertTrue(checkLink("obj_False", "obj_False", diff.getUnSatCore()));
    assertTrue(checkLink("obj_Min_Ident_1", "obj_Ident_Between_2_And_19", diff.getUnSatCore()));
    assertTrue(checkLink("obj_MaxIdent_7", "obj_Ident_Between_2_And_19", diff.getUnSatCore()));
    assertTrue(checkLink("obj_Auction_Names", "obj_No_Auction_Facebook", diff.getUnSatCore()));
    assertTrue(checkLink("obj_MaxIdent_7", "obj_MaxIdent_9", diff.getUnSatCore()));
    assertFalse(checkLink("obj_MaxIdent_7", "obj_No_Auction_Facebook", diff.getUnSatCore()));
  }

  @ParameterizedTest
  @MethodSource("cd2smtStrategies")
  public void testOclDiff2CD_NoDiff(
          ClassStrategy.Strategy cs, InheritanceData.Strategy is, AssociationStrategy.Strategy as)
          throws IOException {
    CD2SMTMill.init(cs, is, as);
    OCLInvDiffResult diff =
            computeDiff2CD(
                    "2CDDiff/nodiff/old.cd",
                    "2CDDiff/nodiff/new.cd",
                    "2CDDiff/nodiff/old.ocl",
                    "2CDDiff/nodiff/new.ocl");
    IOHelper.printInvDiffResult(diff, Path.of(TARGET_DIR + "OclDiff2CD_NoDiff"));
    assertTrue(diff.getDiffWitness().isEmpty());
    assertEquals(countLinks(diff.getUnSatCore()), 3);
    assertTrue(checkLink("obj_Pos1", "obj_Cardinality_right", diff.getUnSatCore()));
    assertTrue(checkLink("obj_Pos2", "obj_Cardinality_right", diff.getUnSatCore()));
    assertTrue(checkLink("obj_Pos3", "obj_Cardinality_left", diff.getUnSatCore()));
  }

  @ParameterizedTest
  @MethodSource("cd2smtStrategies")
  public void testOclDiff2CD_diff(
      ClassStrategy.Strategy cs, InheritanceData.Strategy is, AssociationStrategy.Strategy as)
      throws IOException {
    CD2SMTMill.init(cs, is, as);
    OCLInvDiffResult diff =
        computeDiff2CD(
            "2CDDiff/diff/old.cd",
            "2CDDiff/diff/new.cd",
            "2CDDiff/diff/old.ocl",
            "2CDDiff/diff/new.ocl");
    IOHelper.printInvDiffResult(diff, Path.of(TARGET_DIR + "OclDiff2CD_diff"));
    assertEquals(diff.getDiffWitness().size(), 1);
    assertEquals(
        diff.getDiffWitness().iterator().next().getObjectDiagram().getName(), "Cardinality_right");
    assertEquals(countLinks(diff.getUnSatCore()), 1);
    assertTrue(checkLink("obj_Pos1", "obj_Cardinality_left", diff.getUnSatCore()));
  }

  @ParameterizedTest
  @MethodSource("cd2smtStrategies")
  public void testOclDiff2CD_CDDiff(
      ClassStrategy.Strategy cs, InheritanceData.Strategy is, AssociationStrategy.Strategy as)
      throws IOException {
    CD2SMTMill.init(cs, is, as);
    OCLInvDiffResult diff =
        computeDiff2CD(
            "2CDDiff/cddiff/old.cd",
            "2CDDiff/cddiff/new.cd",
            "2CDDiff/cddiff/old.ocl",
            "2CDDiff/cddiff/new.ocl");
    IOHelper.printInvDiffResult(diff, Path.of(TARGET_DIR + "OclDiff2CD_CDDiff"));
    assertTrue(diff.getUnSatCore() == null);
    assertTrue(diff.getDiffWitness().size() >= 1);
  }

  @ParameterizedTest
  @MethodSource("cd2smtStrategies")
  public void testOCLDiffPartial(
      ClassStrategy.Strategy cs, InheritanceData.Strategy is, AssociationStrategy.Strategy as)
      throws IOException {
    CD2SMTMill.init(cs, is, as);
    ASTCDCompilationUnit cdAST = parseCD("Partial/Partial.cd");
    Set<ASTOCLCompilationUnit> oclSet = new HashSet<>();
    oclSet.add(parseOCl("Partial/Partial.cd", "Partial/partial.ocl"));

    ASTODArtifact od = OCLDiffGenerator.oclWitness(cdAST, oclSet, true);
    IOHelper.printOD(od, Path.of(TARGET_DIR + "OCLDiffPartial"));

    for (ASTODElement element : od.getObjectDiagram().getODElementList()) {
      if (element instanceof ASTODNamedObject) {
        ASTODNamedObject obj = (ASTODNamedObject) element;
        assertTrue(obj.getODAttributeList().size() <= 3);
      }
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"new", "old"})
  public void testMotivatingExample_Sat(String oclFile) throws IOException {
    ASTODArtifact diff =
        computeWitness(
            "motivatingExample/BankManagementSystem.cd", "motivatingExample/" + oclFile + ".ocl");

    assertNotEquals("UNSAT_CORE_OD", diff.getObjectDiagram().getName());
    IOHelper.printOD(diff, Path.of(TARGET_DIR + "MotivatingExample_" + oclFile));
  }
}
