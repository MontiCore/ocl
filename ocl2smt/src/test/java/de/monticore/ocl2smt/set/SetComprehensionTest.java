package de.monticore.ocl2smt.set;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.OCL2SMTGenerator;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SetComprehensionTest extends ExpressionAbstractTest {
  @BeforeEach
  public void setup() throws IOException {
    Log.init();
    OCLMill.init();
    CD4CodeMill.init();
    parse(
        "/setExpressions/setComprehension/SetComp.cd",
        "/setExpressions/setComprehension/SetComp.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
  }

  @Test
  public void test_Set_Comp_1() {
    testInv("Test1");
  }

  @Test
  public void test_Set_Comp_2() {
    testUnsatInv("Test2");
  }

  @Test
  public void test_Set_Comp_3() {
    testInv("Test3");
  }

  @Test
  public void test_Set_Comp_4() {
    testUnsatInv("Test4");
  }

  @Test
  public void test_Set_Comp_5() {
    testUnsatInv("Test5");
  }

  @Test
  public void test_Set_Comp_7() {
    testInv("Test7");
  }

  @Test
  public void test_Set_Comp_8() {
    testInv("Test8");
  }

  @Test
  public void test_Set_Comp_9() {
    testUnsatInv("Test9");
  }

  @Test
  public void test_Set_Comp_10() {
    testInv("Test10");
  }

  @Test
  public void test_Set_Comp_11() {
    testUnsatInv("Test11");
  }

  @Test
  public void test_Set_Comp_12() {
    testInv("Test12");
  }

  @Test
  public void test_Set_Comp_13() {
    testInv("Test13");
  }

  @Disabled
  @Test
  public void test_Set_String() {
    testInv("String");
  }
}
