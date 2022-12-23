package de.monticore.ocl2smt;

import de.monticore.cd4analysis.prettyprint.CD4AnalysisFullPrettyPrinter;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl2smt.helpers.Helper;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class TestPreAndPostConditions extends OCLDiffTest {
  @Test
  public void TestAddPreAttribute() throws IOException {
    CD4AnalysisFullPrettyPrinter printer = new CD4AnalysisFullPrettyPrinter();
    ASTCDCompilationUnit ast = parseCD("/post-pre-conditions/post-pre.cd");
    System.out.println(printer.prettyprint(ast));
    Helper.buildPreAttribute(ast);
    System.out.println(printer.prettyprint(ast));
  }
}
