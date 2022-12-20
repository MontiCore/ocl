package de.monticore.ocl2smt;

import static org.gradle.internal.impldep.org.testng.Assert.assertEquals;
import static org.gradle.internal.impldep.org.testng.Assert.assertTrue;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.odbasis._ast.*;
import de.monticore.odlink._ast.ASTODLink;
import java.io.IOException;
import java.util.*;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

public class OCLDiffTest extends OCLDiffAbstractTest {

  @Test
  public void test_ocl_diff() throws IOException {
    ASTCDCompilationUnit ast = parseCD("Auction.cd");

    Set<ASTOCLCompilationUnit> pocl = new HashSet<>();
    pocl.add(parseOCl("Auction.cd", "PosConstraint1.ocl"));
    pocl.add(parseOCl("Auction.cd", "PosConstraint2.ocl"));

    Set<ASTOCLCompilationUnit> nocl = new HashSet<>();
    nocl.add(parseOCl("Auction.cd", "negConstraint2.ocl"));
    nocl.add(parseOCl("Auction.cd", "negConstraint1.ocl"));

    // make ocldiff
    Pair<ASTODArtifact, Set<ASTODArtifact>> diff = OCLDiffGenerator.oclDiff(ast, pocl, nocl);
    List<ASTODArtifact> satOds = new ArrayList<>(diff.getRight());
    ASTODArtifact unsatOD = diff.getLeft();
    // print ods
    satOds.forEach(this::printOD);
    printOD(unsatOD);

    // get trace links
    List<String> unsatInvNameList = getUnsatInvNameList(unsatOD);
    org.junit.jupiter.api.Assertions.assertEquals(4, satOds.size());

    List<String> unsatInvLines = getUnsatInvLines(unsatInvNameList, unsatOD);
    assertTrue(unsatInvLines.contains("5"));
    assertTrue(unsatInvLines.contains("11"));
    assertTrue(unsatInvLines.contains("15"));
    assertTrue(unsatInvLines.contains("20"));
  }

  @Test
  public void testOdPartial() throws IOException {
    ASTCDCompilationUnit cdAST = parseCD("Partial/Partial.cd");
    Set<ASTOCLCompilationUnit> oclSet = new HashSet<>();
    oclSet.add(parseOCl("Partial/Partial.cd", "Partial/partial.ocl"));

    ASTODArtifact od = OCLDiffGenerator.oclWitness(cdAST, oclSet, true);
    printOD(od);

    od.getObjectDiagram()
        .getODElementList()
        .forEach(
            p -> {
              assert !(p instanceof ASTODNamedObject)
                  || (((ASTODNamedObject) p).getODAttributeList().size() <= 3);
            });
  }

  @Test
  public void testCDOCLDiff() throws IOException {
    ASTCDCompilationUnit posCD = parseCD("2CDDiff/posCD.cd");
    ASTCDCompilationUnit negCD = parseCD("2CDDiff/negCD.cd");
    Set<ASTOCLCompilationUnit> posOCL = new HashSet<>();
    Set<ASTOCLCompilationUnit> negOCL = new HashSet<>();
    posOCL.add(parseOCl("2CDDiff/posCD.cd", "2CDDiff/posOCL.ocl"));
    negOCL.add(parseOCl("2CDDiff/negCD.cd", "2CDDiff/negOCL.ocl"));
    printOD(OCLDiffGenerator.oclWitness(posCD, posOCL, false));
    Pair<ASTODArtifact, Set<ASTODArtifact>> diff =
        OCLDiffGenerator.CDOCLDiff(posCD, negCD, posOCL, negOCL, false);
    assertTrue(diff.getRight().isEmpty());
    assertEquals(
        diff.getLeft().getObjectDiagram().getODElementList().stream()
                .filter(x -> (x instanceof ASTODLink))
                .count()
            ,2);
  }
}
