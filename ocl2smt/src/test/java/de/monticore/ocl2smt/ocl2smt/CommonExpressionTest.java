/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.BoolExpr;
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
    super.initLogger();
    super.initMills();
    parse("MinAuction.cd", "CommonExpr.ocl");
    ocl2SMTGenerator = new OCL2SMTGenerator(cdAST, buildContext());
    ocl2SMTGenerator.inv2smt(oclAST.getOCLArtifact()).forEach(b -> res.add(b.getValue()));
  }

  @Test
  public void testComparisonConverter() {
    Assertions.assertEquals(res.get(12).getSExpr(), "false");
    Assertions.assertEquals(res.get(13).getSExpr(), "true");
    Assertions.assertEquals(res.get(14).getSExpr(), "false");
    Assertions.assertEquals(res.get(15).getSExpr(), "true");
    Assertions.assertEquals(res.get(16).getSExpr(), "false");
    Assertions.assertEquals(res.get(17).getSExpr(), "true");
  }

  @Test
  public void testArithmeticExpressionConverter() {
    Assertions.assertEquals(res.get(8).getSExpr(), "true");
    Assertions.assertEquals(res.get(9).getSExpr(), "true");
    Assertions.assertEquals(res.get(10).getSExpr(), "true");
    Assertions.assertEquals(res.get(11).getSExpr(), "true");
    Assertions.assertEquals(res.get(18).getSExpr(), "true");
  }

  @Test
  public void testLogicExpressionConverter() {
    Assertions.assertEquals(
        res.get(0), ocl2SMTGenerator.getCD2SMTGenerator().getContext().mkBool(true));
    Assertions.assertEquals(
        res.get(1), ocl2SMTGenerator.getCD2SMTGenerator().getContext().mkFalse());
    Assertions.assertEquals(res.get(2).getSExpr(), "false");
    Assertions.assertEquals(res.get(3).getSExpr(), "true");
    Assertions.assertEquals(res.get(4).getSExpr(), "false");
    Assertions.assertEquals(res.get(5).getSExpr(), "false");
    Assertions.assertEquals(res.get(6).getSExpr(), "true");
    Assertions.assertEquals(res.get(7).getSExpr(), "true");
  }
}
