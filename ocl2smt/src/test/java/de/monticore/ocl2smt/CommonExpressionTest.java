package de.monticore.ocl2smt;

import com.microsoft.z3.BoolExpr;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommonExpressionTest extends ExpressionAbstractTest {
  protected static List<BoolExpr> res = new ArrayList<>();

  @BeforeEach
  public void setup() throws IOException {
    Log.init();
    OCLMill.init();
    CD4CodeMill.init();
    parse("MinAuction.cd", "CommonExpr.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buidlContext());
    ocl2SMTGenerator.ocl2smt(oclAST.getOCLArtifact()).forEach(b -> res.add(b.getValue()));
  }

  @Test
  public void testComparisonConverter() {
    Assertions.assertEquals(res.get(12).getSExpr(), "(< 10 3)");
    Assertions.assertEquals(res.get(13).getSExpr(), "(> 10 4)");
    Assertions.assertEquals(res.get(14).getSExpr(), "(<= 10 4)");
    Assertions.assertEquals(res.get(15).getSExpr(), "(>= 10 4)");
    Assertions.assertEquals(res.get(16).getSExpr(), "(= 10 4)");
    Assertions.assertEquals(res.get(17).getSExpr(), "(not (= 10 4))");
  }

  @Test
  public void testArithmeticExpressionConverter() {
    Assertions.assertEquals(res.get(8).getSExpr(), "(= (+ 10 12) 22)");
    Assertions.assertEquals(res.get(9).getSExpr(), "(= (div 10 5) 2)");
    Assertions.assertEquals(res.get(10).getSExpr(), "(= (* 10 5) 50)");
    Assertions.assertEquals(res.get(11).getSExpr(), "(= (mod 10 2) 0)");
    Assertions.assertEquals(res.get(18).getSExpr(), "(= (- 10 12) (* (- 1) 2))");
  }

  @Test
  public void testLogicExpressionConverter() {
    Assertions.assertEquals(res.get(0), ocl2SMTGenerator.cd2smtGenerator.getContext().mkBool(true));
    Assertions.assertEquals(res.get(1), ocl2SMTGenerator.cd2smtGenerator.getContext().mkFalse());
    Assertions.assertEquals(res.get(2).getSExpr(), "(not true)");
    Assertions.assertEquals(res.get(3).getSExpr(), "(not false)");
    Assertions.assertEquals(res.get(4).getSExpr(), "(and false false)");
    Assertions.assertEquals(res.get(5).getSExpr(), "(and false true)");
    Assertions.assertEquals(res.get(6).getSExpr(), "(or true false)");
    Assertions.assertEquals(res.get(7).getSExpr(), "(or true true)");
  }
}
