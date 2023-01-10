package de.monticore.ocl2smt.ocldiff;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.helpers.OCLCDHelper;
import de.monticore.ocl2smt.util.OPDiffResult;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
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
    OCLCDHelper.buildPreCD(ast);
    ASTCDClass company = getClass(ast, "Company");
    Assertions.assertTrue(containsAttribute(company, "name__pre"));
    Assertions.assertTrue(containsAttribute(company, "employees__pre"));
    Assertions.assertTrue(containsAssoc(ast, "Person", "person__pre", "Company", "company__pre"));
  }

  @Test
  public void testOPConstraintWitness() throws IOException {
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/pre-post.cd");

    Set<ASTOCLCompilationUnit> posOCl = new HashSet<>();
    posOCl.add(parseOCl("/post-pre-conditions/pre-post.cd", "/post-pre-conditions/witness.ocl"));

    OPDiffResult witness = OCLOPDiff.oclWitness(ast, posOCl, false);
    Assertions.assertNotNull(witness);
    printOD(witness.getPostOD());
    printOD(witness.getPreOD());
  }

  @Test
  public void testOpConstraintDiff() throws IOException {
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/pre-post.cd");

    Set<ASTOCLCompilationUnit> in = new HashSet<>();
    in.add(parseOCl("/post-pre-conditions/pre-post.cd", "/post-pre-conditions/pos.ocl"));

    Set<ASTOCLCompilationUnit> notin = new HashSet<>();
    notin.add(parseOCl("/post-pre-conditions/pre-post.cd", "/post-pre-conditions/neg.ocl"));

    Pair<ASTODArtifact, Set<OPDiffResult>> diff = OCLOPDiff.oclDiffOp(ast, in, notin, false);
    Assertions.assertEquals(countLinks(diff.getLeft()), 0);
    printOPDiff(diff);
  }
}
