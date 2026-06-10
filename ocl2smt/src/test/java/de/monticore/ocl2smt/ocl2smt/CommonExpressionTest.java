/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.BoolExpr;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    assertEquals("false", res.get(12).getSExpr());
    assertEquals("true", res.get(13).getSExpr());
    assertEquals("false", res.get(14).getSExpr());
    assertEquals("true", res.get(15).getSExpr());
    assertEquals("false", res.get(16).getSExpr());
    assertEquals("true", res.get(17).getSExpr());
  }

  @Test
  public void testArithmeticExpressionConverter() {
    assertEquals("true", res.get(8).getSExpr());
    assertEquals("true", res.get(9).getSExpr());
    assertEquals("true", res.get(10).getSExpr());
    assertEquals("true", res.get(11).getSExpr());
    assertEquals("true", res.get(18).getSExpr());
  }

  @Test
  public void testLogicExpressionConverter() {
    assertEquals(
        res.get(0), ocl2SMTGenerator.getCD2SMTGenerator().getContext().mkBool(true));
    assertEquals(
        res.get(1), ocl2SMTGenerator.getCD2SMTGenerator().getContext().mkFalse());
    assertEquals("false", res.get(2).getSExpr());
    assertEquals("true", res.get(3).getSExpr());
    assertEquals("false", res.get(4).getSExpr());
    assertEquals("false", res.get(5).getSExpr());
    assertEquals("true", res.get(6).getSExpr());
    assertEquals("true", res.get(7).getSExpr());
  }
}
