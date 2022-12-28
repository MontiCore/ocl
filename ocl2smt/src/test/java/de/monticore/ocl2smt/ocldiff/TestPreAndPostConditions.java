package de.monticore.ocl2smt.ocldiff;

import de.monticore.cd4analysis.prettyprint.CD4AnalysisFullPrettyPrinter;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.helpers.Helper;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import de.monticore.odbasis._ast.ASTODArtifact;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TestPreAndPostConditions extends OCLDiffAbstractTest {
  @Test
  @Disabled
  public void TestAddPreAttribute() throws IOException {
    CD4AnalysisFullPrettyPrinter printer = new CD4AnalysisFullPrettyPrinter();
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/pre-post.cd");
    System.out.println(printer.prettyprint(ast));
    Helper.buildPreCD(ast);
    System.out.println(printer.prettyprint(ast));
  }

  @Test
  public void testPrePostConditionWitness() throws IOException {
    //Context ctx = buildContext() ;
    ASTCDCompilationUnit ast =  parseCD("/post-pre-conditions/pre-post.cd");

    Set<ASTOCLCompilationUnit> in = new HashSet<>();
    in.add(parseOCl("/post-pre-conditions/pre-post.cd", "/post-pre-conditions/neg.ocl"));

    ASTODArtifact witness = OCLDiffGenerator.oclWitness(ast,in,notin,false);
    printDiff(diff);
  }
  @Test
  public void testPostPreConditions() throws IOException {
    //Context ctx = buildContext() ;
   ASTCDCompilationUnit ast =  parseCD("/post-pre-conditions/pre-post.cd");

    Set<ASTOCLCompilationUnit> in = new HashSet<>();
    in.add(parseOCl("/post-pre-conditions/pre-post.cd", "/post-pre-conditions/pos.ocl"));

      Set<ASTOCLCompilationUnit> notin = new HashSet<>();
      notin.add(parseOCl("/post-pre-conditions/pre-post.cd", "/post-pre-conditions/neg.ocl"));

  Pair<ASTODArtifact, Set<ASTODArtifact>> diff = OCLDiffGenerator.oclDiffOp(ast,in,notin,false);
  printDiff(diff);
  }
}
