package de.monticore.ocl2smt.ocldiff;

import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPDiffResult;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.odbasis._ast.ASTODNamedObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OCLDIffOPConstraintTest extends OCLDiffAbstractTest {
  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
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
            ast, "Person", OCLHelper.mkPre("person"), "Company", OCLHelper.mkPre("company")));
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
    int preAge = Integer.parseInt(getAttribute(preObj, "age"));
    Assertions.assertTrue(preAge >= 18);

    // CheckPostCD
    ASTODNamedObject postObj = getThisObj(witness.getPostOD());
    List<ASTODNamedObject> postLinks = getLinkedObjects(postObj, witness.getPostOD());

    Assertions.assertEquals(1, postLinks.size());
    Assertions.assertEquals("\"newCompany\"", getAttribute(postLinks.get(0), "name"));
    Assertions.assertEquals("1", getAttribute(postLinks.get(0), "employees"));
    int postAge = Integer.parseInt(getAttribute(preObj, "age"));
   Assertions.assertTrue(postAge >= 18);

    // checkDiff
    Assertions.assertEquals(
        "3", getAttribute(getObject(witness.getPostOD(), preLinks.get(0).getName()), "employees"));
    printOD(witness.getPostOD(), "OPConstraintWitness");
    printOD(witness.getPreOD(), "OPConstraintWitness");
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

    Assertions.assertNotNull(diff);
    ASTODNamedObject preThisObj = getThisObj(diff.getOpDiffWitness().iterator().next().getPreOD());
    ASTODNamedObject postThisObj =
            getThisObj(diff.getOpDiffWitness().iterator().next().getPostOD());

    //check if the post condition hold
    double preSalary = Integer.parseInt(getAttribute(preThisObj, "salary"));
    double postSalary = Integer.parseInt(getAttribute(postThisObj, "salary"));
    Assertions.assertEquals(preSalary + 100, postSalary);

    //check if the diff is correct (result = false)
    String result =
            diff.getOpDiffWitness()
                    .iterator()
                    .next()
                    .getPostOD()
                    .getObjectDiagram()
                    .getStereotype()
                    .getValue("result");

    Assertions.assertEquals(result, "false");

    //check the trace of the invariant
    Assertions.assertNotNull(diff.getUnSatCore());
   Assertions.assertTrue( checkLink("OfLegalAgeNew","OfLegalAgeOld", diff.getUnSatCore()));

    //check the inv witness
    Assertions.assertEquals(1,diff.getInvDiffWitness().size());
    printOPDiff(diff, "/OpConstraintDiff");
  }
}
