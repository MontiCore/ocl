package de.monticore.ocl2smt.ocldiff;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPDiffResult;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.odbasis._ast.*;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.util.*;
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
    OCLHelper.buildPreCD(ast);
    ASTCDClass company = getClass(ast, "Company");
    Assertions.assertTrue(containsAttribute(company, OCLHelper.mkPre("name")));
    Assertions.assertTrue(containsAttribute(company, OCLHelper.mkPre("employees")));
    Assertions.assertTrue(
        containsAssoc(
            ast,
            "Person",
            OCLHelper.mkPre("person"),
            "Company",
            OCLHelper.mkPre("company")));
  }

  @Test
  public void testOPConstraintWitness() throws IOException {
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/pre-post.cd");

    Set<ASTOCLCompilationUnit> posOCl = new HashSet<>();
    posOCl.add(parseOCl("/post-pre-conditions/pre-post.cd", "/post-pre-conditions/witness.ocl"));

    Set<OCLOPWitness> witnessList = OCLDiffGenerator.oclOPWitness(ast, posOCl, false);
    Assertions.assertEquals(witnessList.size(), 1);
    OCLOPWitness witness = witnessList.iterator().next();

    // check preCD
    ASTODNamedObject preObj = getThisObj(witness.getPreOD());
    List<ASTODNamedObject> preLinks = getLinkedObjects(preObj, witness.getPreOD());

    Assertions.assertEquals(1, preLinks.size());
    Assertions.assertEquals("\"oldCompany\"", getAttribute(preLinks.get(0), "name"));
    Assertions.assertEquals("4", getAttribute(preLinks.get(0), "employees"));

    // CheckPostCD
    ASTODNamedObject postObj = getThisObj(witness.getPostOD());
    List<ASTODNamedObject> postLinks = getLinkedObjects(postObj, witness.getPostOD());

    Assertions.assertEquals(1, postLinks.size());
    Assertions.assertEquals("\"newCompany\"", getAttribute(postLinks.get(0), "name"));
    Assertions.assertEquals("1", getAttribute(postLinks.get(0), "employees"));

    // checkDiff
    Assertions.assertEquals(
        "3", getAttribute(getObject(witness.getPostOD(), preLinks.get(0).getName()), "employees"));
    printOD(witness.getPostOD());
    printOD(witness.getPreOD());
  }

  @Test
  public void testOpConstraintDiff() throws IOException {
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/pre-post.cd");

    Set<ASTOCLCompilationUnit> newOCL = new HashSet<>();
    newOCL.add(parseOCl("/post-pre-conditions/pre-post.cd", "/post-pre-conditions/new.ocl"));

    Set<ASTOCLCompilationUnit> oldOCL = new HashSet<>();
    oldOCL.add(parseOCl("/post-pre-conditions/pre-post.cd", "/post-pre-conditions/old.ocl"));

    ASTOCLMethodSignature method = getMethodSignature(newOCL, "Person.increaseSalary");

    OCLOPDiffResult diff = OCLDiffGenerator.oclOPDiff(ast, oldOCL, newOCL, method, false);

    assert diff != null;
    ASTODNamedObject preThisObj = getThisObj(diff.getDiffWitness().iterator().next().getPreOD());
    ASTODNamedObject postThisObj = getThisObj(diff.getDiffWitness().iterator().next().getPostOD());

    double preSalary = Integer.parseInt(getAttribute(preThisObj, "salary"));
    double postSalary = Integer.parseInt(getAttribute(postThisObj, "salary"));
    Assertions.assertEquals(preSalary + 100, postSalary);

    String result =
        diff.getDiffWitness()
            .iterator()
            .next()
            .getPostOD()
            .getObjectDiagram()
            .getStereotype()
            .getValue("result");

    Assertions.assertEquals(result, "false");
    printOPDiff(diff);
  }
}
