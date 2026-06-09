package de.monticore.ocl2smt.ocldiff;

import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPDiffResult;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.odbasis._ast.ASTODNamedObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OCLDIffOPConstraintTest extends OCLDiffAbstractTest {
  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  @Test
  public void TestBuildPreCD() throws IOException {
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/PrePost.cd");
    OCLHelper.buildPreCD(ast);
    ASTCDClass company = (ASTCDClass) CDHelper.getASTCDType("Company", ast.getCDDefinition());
    assertTrue(containsAttribute(company, OCLHelper.mkPre("name")));
    assertTrue(containsAttribute(company, OCLHelper.mkPre("employees")));
    assertTrue(
        containsAssoc(
            ast, "Person", OCLHelper.mkPre("person"), "Company", OCLHelper.mkPre("company")));
  }

  @Test
  public void testOPConstraintWitness() throws IOException {
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/PrePost.cd");
    Set<ASTOCLCompilationUnit> posOCl = new LinkedHashSet<>();
    posOCl.add(parseOCl("/post-pre-conditions/PrePost.cd", "/post-pre-conditions/Witness.ocl"));

    Set<OCLOPWitness> witnessList = OCLDiffGenerator.oclOPWitness(ast, posOCl, false);
    assertEquals(1, witnessList.size());
    OCLOPWitness witness = witnessList.iterator().next();

    // check preCD
    ASTODNamedObject preObj = getThisObj(witness.getPreOD());
    List<ASTODNamedObject> preLinks = getLinkedObjects(preObj, witness.getPreOD());

    assertEquals(1, preLinks.size());
    assertEquals("\"oldCompany\"", getAttribute(preLinks.get(0), "name"));
    assertEquals("4", getAttribute(preLinks.get(0), "employees"));
    // int preAge = Integer.parseInt(getAttribute(preObj, "age"));
    // assertTrue(preAge >= 18);

    // CheckPostCD
    ASTODNamedObject postObj = getThisObj(witness.getPostOD());
    List<ASTODNamedObject> postLinks = getLinkedObjects(postObj, witness.getPostOD());

    assertEquals(1, postLinks.size());
    assertEquals("\"newCompany\"", getAttribute(postLinks.get(0), "name"));
    assertEquals("1", getAttribute(postLinks.get(0), "employees"));
    // int postAge = Integer.parseInt(getAttribute(preObj, "age"));
    // assertTrue(postAge >= 18);

    // checkDiff
    assertEquals(
        "3", getAttribute(getObject(witness.getPostOD(), preLinks.get(0).getName()), "employees"));
    Path of = Path.of(TARGET_DIR + "OPConstraintWitness");
    IOHelper.printOD(witness.getPostOD(), of);
    IOHelper.printOD(witness.getPreOD(), of);
  }

  @Test
  public void testOpConstraintDiff() throws IOException {
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/PrePost.cd");

    Set<ASTOCLCompilationUnit> newOCL = new LinkedHashSet<>();
    newOCL.add(parseOCl("/post-pre-conditions/PrePost.cd", "/post-pre-conditions/New.ocl"));

    Set<ASTOCLCompilationUnit> oldOCL = new LinkedHashSet<>();
    oldOCL.add(parseOCl("/post-pre-conditions/PrePost.cd", "/post-pre-conditions/Old.ocl"));

    ASTOCLMethodSignature method = IOHelper.getMethodSignature(newOCL, "Person.increaseSalary");

    OCLOPDiffResult diff = OCLDiffGenerator.oclOPDiffV1(ast, oldOCL, newOCL, method, false);

    assertNotNull(diff);
    ASTODNamedObject preThisObj = getThisObj(diff.getDiffWitness().iterator().next().getPreOD());
    ASTODNamedObject postThisObj = getThisObj(diff.getDiffWitness().iterator().next().getPostOD());

    // check if the post-condition holds
    double preSalary = Integer.parseInt(getAttribute(preThisObj, "salary"));
    double postSalary = Integer.parseInt(getAttribute(postThisObj, "salary"));
    assertEquals(preSalary + 100, postSalary);

    // check if the invariant hold
    // double postAge = Integer.parseInt(getAttribute(postThisObj, "age"));
    // assertTrue(postAge >= 18);
    // check if the diff is correct (result = false)
    String result =
        diff.getDiffWitness()
            .iterator()
            .next()
            .getPostOD()
            .getObjectDiagram()
            .getStereotype()
            .getValue("result");

    assertEquals(result, "false");

    IOHelper.printOPDiff(diff, Path.of(TARGET_DIR + "/OpConstraintDiff"));
  }
}
