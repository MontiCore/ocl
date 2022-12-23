package de.monticore.ocl2smt;

import static org.gradle.internal.impldep.org.junit.Assert.assertFalse;
import static org.gradle.internal.impldep.org.testng.Assert.assertEquals;
import static org.gradle.internal.impldep.org.testng.Assert.assertTrue;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.odbasis._ast.*;
import java.io.IOException;
import java.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OCLDiffTest extends OCLDiffAbstractTest {

  @Test
  public void testOCLDiffOneCD() throws IOException {

    Pair<ASTODArtifact, Set<ASTODArtifact>> diff =
        computeDiffOneCD("Auction.cd", "pos.ocl", "neg.ocl");
    printDiff(diff);
    Assertions.assertEquals(4, diff.getRight().size());

    assertTrue(checkLink("obj_False", "obj_False", diff.getLeft()));
    assertTrue(checkLink("obj_Min_Ident_1", "obj_Ident_Between_2_And_19", diff.getLeft()));
    assertTrue(checkLink("obj_MaxIdent_7", "obj_Ident_Between_2_And_19", diff.getLeft()));
    assertTrue(checkLink("obj_Auction_Names", "obj_No_Auction_Facebook", diff.getLeft()));
    assertTrue(checkLink("obj_MaxIdent_7", "obj_MaxIdent_9", diff.getLeft()));
    assertFalse(checkLink("obj_MaxIdent_7", "obj_No_Auction_Facebook", diff.getLeft()));
  }

  @Test
  public void testOclDiff2CD_NoDiff() throws IOException {
    Pair<ASTODArtifact, Set<ASTODArtifact>> diff =
        computeDiff2CD(
            "2CDDiff/posCD.cd", "2CDDiff/negCD.cd", "2CDDiff/posOCL.ocl", "2CDDiff/negOCL.ocl");

    assertTrue(diff.getRight().isEmpty());
    assertEquals(countLinks(diff.getLeft()), 3);
    assertTrue(checkLink("obj_Pos1", "obj_Cardinality_right", diff.getLeft()));
    assertTrue(checkLink("obj_Pos2", "obj_Cardinality_right", diff.getLeft()));
    assertTrue(checkLink("obj_Pos3", "obj_Cardinality_left", diff.getLeft()));
  }

  @Test
  public void testOCLDiffPartial() throws IOException {
    ASTCDCompilationUnit cdAST = parseCD("Partial/Partial.cd");
    Set<ASTOCLCompilationUnit> oclSet = new HashSet<>();
    oclSet.add(parseOCl("Partial/Partial.cd", "Partial/partial.ocl"));

    ASTODArtifact od = OCLDiffGenerator.oclWitness(cdAST, oclSet, true);
    printOD(od);

    for (ASTODElement element : od.getObjectDiagram().getODElementList()){
      if (element instanceof  ASTODNamedObject){
        ASTODNamedObject obj = (ASTODNamedObject) element ;
        assertTrue(obj.getODAttributeList().size()<=3);
      }
    }
  }
}
