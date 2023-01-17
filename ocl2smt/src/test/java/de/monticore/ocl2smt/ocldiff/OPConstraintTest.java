package de.monticore.ocl2smt.ocldiff;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTStrategy;
import de.monticore.ocl2smt.util.OPDiffResult;
import de.monticore.odbasis._ast.*;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.util.*;
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
    OCL2SMTStrategy.buildPreCD(ast);
    ASTCDClass company = getClass(ast, "Company");
    Assertions.assertTrue(containsAttribute(company, OCL2SMTStrategy.mkPre("name")));
    Assertions.assertTrue(containsAttribute(company, OCL2SMTStrategy.mkPre("employees")));
    Assertions.assertTrue(
        containsAssoc(
            ast,
            "Person",
            OCL2SMTStrategy.mkPre("person"),
            "Company",
            OCL2SMTStrategy.mkPre("company")));
  }

  @Test
  public void testOPConstraintWitness() throws IOException {
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/pre-post.cd");

    Set<ASTOCLCompilationUnit> posOCl = new HashSet<>();
    posOCl.add(parseOCl("/post-pre-conditions/pre-post.cd", "/post-pre-conditions/neg.ocl"));

    OPDiffResult witness = OCLOPDiff.oclWitness(ast, posOCl, false);
    Assertions.assertNotNull(witness);

    // check preCD
  //  ASTODNamedObject preObj = getThisObj(witness.getPreOD());
  //  List<ASTODNamedObject> preLinks = getLinkedObjects(preObj, witness.getPreOD());

   // Assertions.assertEquals(1, preLinks.size());
   // Assertions.assertEquals("\"oldCompany\"", getAttribute(preLinks.get(0), "name"));
   // Assertions.assertEquals("4", getAttribute(preLinks.get(0), "employees"));

    // CheckPostCD
   // ASTODNamedObject postObj = getThisObj(witness.getPostOD());
   // List<ASTODNamedObject> postLinks = getLinkedObjects(postObj, witness.getPostOD());

   // Assertions.assertEquals(1, postLinks.size());
   // Assertions.assertEquals("\"newCompany\"", getAttribute(postLinks.get(0), "name"));
   // Assertions.assertEquals("1", getAttribute(postLinks.get(0), "employees"));

    // checkDiff
  //  Assertions.assertEquals(
   //     "3", getAttribute(getObject(witness.getPostOD(), preLinks.get(0).getName()), "employees"));
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
