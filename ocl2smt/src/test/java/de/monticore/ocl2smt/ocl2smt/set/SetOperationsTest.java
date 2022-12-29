/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt.set;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl2smt.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SetOperationsTest extends ExpressionAbstractTest {
  @BeforeEach
  public void setup() throws IOException {
    Log.init();
    OCLMill.init();
    CD4CodeMill.init();
    parse("/setExpressions/setOperations/SetOp.cd", "/setExpressions/setOperations/SetOp.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
  }

  @Test
  public void test_isin_set() {
    testInv("All_Person_in_All_Auctions");
  }

  @Test
  public void test_IsIn_notIn() {
    testInv("IsIn_notIn");
  }

  @Test
  public void test_notin_set() {
    testInv("One_Person_in_Any_Auctions");
  }

  @Test
  public void isin_notIn_unsat() {
    testUnsatInv("Counter_Example");
  }

  @Test
  public void test_Notin_isInT() {
    testInv("Notin_isIn");
  }

  @Test
  public void test_SetUnion_sat() {
    testInv("Set_Union_Sat");
  }

  @Test
  public void test_Set_Union_one_side_sat() {
    testInv("Set_Union_one_side_sat");
  }

  @Test
  public void test_Set_Union_one_side_Unsat() {
    testUnsatInv("Set_Union_one_side_Unsat");
  }

  @Test
  public void test_SetIntersect_sat() {
    testInv("Set_Intersection_Sat");
  }

  @Test
  public void test_Set_Intersection_Unsat() {
    testUnsatInv("Set_Intersection_Unsat");
  }

  @Test
  public void test_Set_Minus_sat() {
    testInv("Set_Minus_sat");
  }

  @Test
  public void test_Set_Minus_Unsat() {
    testUnsatInv("Set_Minus_Unsat");
  }
}
