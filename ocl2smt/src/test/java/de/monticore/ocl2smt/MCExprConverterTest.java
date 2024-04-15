package de.monticore.ocl2smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Status;
import de.monticore.cd.facade.MCQualifiedNameFacade;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl2smt.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.oclExpr2smt.MCExprConverter;
import de.monticore.od4report.OD4ReportMill;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MCExprConverterTest extends ExpressionAbstractTest {
  @BeforeEach
  public void setup() {
    initMills();
    initLogger();
  }

  @Test
  public void MCExprToSMTTest() throws IOException {

    parse("mcExpr2smt/MyAuto.cd", "mcExpr2smt/MyAuto.ocl");
    Context ctx = new Context();
    MCExprConverter exprConverter = MCExprConverter.getInstance(cdAST, ctx);

    Z3ExprAdapter expr = exprConverter.convertExpr(getExpression(), z->this.getExprTypes(z.getName()));

    Assertions.assertTrue(expr.isBoolExpr()); // a.speed > a.speed ;
    Log.println(expr.toString());

    solver = ctx.mkSolver();

    Assertions.assertEquals(solver.check((BoolExpr) expr.getExpr()), Status.UNSATISFIABLE);
  }

  public ASTMCType getExprTypes(String t) {
    return OD4ReportMill.mCQualifiedTypeBuilder()
        .setMCQualifiedName(MCQualifiedNameFacade.createQualifiedName("Auto"))
        .build();
  }

  private ASTExpression getExpression() { // a.speed > a.speed ;
    return ((ASTOCLInvariant) oclAST.getOCLArtifact().getOCLConstraint(0)).getExpression();
  }
}
