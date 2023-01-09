package de.monticore.ocl2smt.ocldiff;

import de.monticore.cd4analysis.prettyprint.CD4AnalysisFullPrettyPrinter;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.helpers.Helper;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OPConstraintTest extends OCLDiffAbstractTest {
  @BeforeEach
  public void setUp() {
    Log.init();
    OCLMill.init();
    CD4CodeMill.init();
  }

  @Test
  public void TestBuildPreCD() throws IOException {
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/pre-post.cd");
    Helper.buildPreCD(ast);
    ASTCDClass company = getClass(ast,"Company");
    Assertions.assertTrue(containsAttribute(company,"name__pre"));
    Assertions.assertTrue(containsAttribute(company,"employees__pre"));
    Assertions.assertTrue(containsAssoc(ast,"Person","person__pre","Company","company__pre"));
  }

  @Test
  public void testOPConstraintWitness() throws IOException {
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/pre-post.cd");

    Set<ASTOCLCompilationUnit> posOCl = new HashSet<>();
    posOCl.add(parseOCl("/post-pre-conditions/pre-post.cd", "/post-pre-conditions/neg.ocl"));

    ASTODArtifact witness = OCLOPDiff.oclWitness(ast, posOCl, false);
    Assertions.assertNotNull(witness);
    printOD(witness);
  }

}
