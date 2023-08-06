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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
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
    Assertions.assertTrue(containsAttribute(company, OCLHelper.mkPre("name")));
    Assertions.assertTrue(containsAttribute(company, OCLHelper.mkPre("employees")));
    Assertions.assertTrue(
        containsAssoc(
            ast, "Person", OCLHelper.mkPre("person"), "Company", OCLHelper.mkPre("company")));
  }

  @Test
  public void testOPConstraintWitness() throws IOException {
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/PrePost.cd");

    Set<ASTOCLCompilationUnit> posOCl = new HashSet<>();
    posOCl.add(parseOCl("/post-pre-conditions/PrePost.cd", "/post-pre-conditions/Witness.ocl"));

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
    Path of = Path.of(TARGET_DIR + "OPConstraintWitness");
    IOHelper.printOD(witness.getPostOD(), of);
    IOHelper.printOD(witness.getPreOD(), of);
  }

  @Test
  public void testOpConstraintDiff() throws IOException {
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/PrePost.cd");

    Set<ASTOCLCompilationUnit> newOCL = new HashSet<>();
    newOCL.add(parseOCl("/post-pre-conditions/PrePost.cd", "/post-pre-conditions/New.ocl"));

    Set<ASTOCLCompilationUnit> oldOCL = new HashSet<>();
    oldOCL.add(parseOCl("/post-pre-conditions/PrePost.cd", "/post-pre-conditions/Old.ocl"));

    ASTOCLMethodSignature method = IOHelper.getMethodSignature(newOCL, "Person.increaseSalary");

    OCLOPDiffResult diff = OCLDiffGenerator.oclOPDiffV1(ast, oldOCL, newOCL, method, false);

    Assertions.assertNotNull(diff);
    ASTODNamedObject preThisObj = getThisObj(diff.getDiffWitness().iterator().next().getPreOD());
    ASTODNamedObject postThisObj = getThisObj(diff.getDiffWitness().iterator().next().getPostOD());

    // check if the post-condition holds
    double preSalary = Integer.parseInt(getAttribute(preThisObj, "salary"));
    double postSalary = Integer.parseInt(getAttribute(postThisObj, "salary"));
    Assertions.assertEquals(preSalary + 100, postSalary);

    // check if the invariant hold
    double postAge = Integer.parseInt(getAttribute(postThisObj, "age"));
    Assertions.assertTrue(postAge >= 18);
    // check if the diff is correct (result = false)
    String result =
        diff.getDiffWitness()
            .iterator()
            .next()
            .getPostOD()
            .getObjectDiagram()
            .getStereotype()
            .getValue("result");

    Assertions.assertEquals(result, "false");

    IOHelper.printOPDiff(diff, Path.of(TARGET_DIR + "/OpConstraintDiff"));
  }
}
