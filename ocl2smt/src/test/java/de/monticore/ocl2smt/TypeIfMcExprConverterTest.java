package de.monticore.ocl2smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import com.microsoft.z3.Status;
import de.monticore.cd.facade.MCQualifiedNameFacade;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.oclExpr2smt.TypeIfMcExprConverter;
import de.monticore.od4report.OD4ReportMill;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TypeIfMcExprConverterTest extends ExpressionAbstractTest {
  TypeIfMcExprConverter exprConverter;

  @BeforeEach
  public void setup() throws IOException {
    initMills();
    initLogger();
    parse("mcExpr2smt/MyAuto.cd", "mcExpr2smt/MyAuto.ocl");
  }

  @Test
  public void testCommonExpr2smt() {
    checkExpr(0, "Auto", Status.UNSATISFIABLE);
  }

  @Test
  public void testTypeIfExpressionSat1() {
    Optional<Model> model = checkExpr(1, "Auto", Status.SATISFIABLE);
    Assertions.assertTrue(model.isPresent());
    buildAndPrintOD(exprConverter, model.get(), "OD1");
  }

  @Test
  public void testTypeIfExpressionSat2() {
    Optional<Model> model = checkExpr(2, "Auto", Status.SATISFIABLE);
    Assertions.assertTrue(model.isPresent());
    buildAndPrintOD(exprConverter, model.get(), "OD2");
  }

  @Test
  public void testTypeIfExpressionUnsat() {
    Optional<Model> model = checkExpr(3, "Auto", Status.UNSATISFIABLE);
    Assertions.assertFalse(model.isPresent());
  }

  private Optional<Model> checkExpr(int invPos, String varType, Status result) {
    CD2SMTMill.init(
        ClassStrategy.Strategy.SS,
        InheritanceData.Strategy.SE,
        AssociationStrategy.Strategy.DEFAULT);
    Context ctx = new Context();
    exprConverter = TypeIfMcExprConverter.getInstance(cdAST, ctx);
    Z3ExprAdapter expr =
        exprConverter.convertExpr(getExpression(invPos), z -> this.buildMCType(varType));

    Assertions.assertTrue(expr.isBoolExpr()); // a.speed > a.speed ;
    Log.println(expr.toString());
    solver = ctx.mkSolver();
    solver.add((BoolExpr) expr.getExpr());
    expr.getGenConstraint().forEach(c -> solver.add((BoolExpr) c.getExpr()));

    Status solverResult = solver.check();
    Assertions.assertEquals(solverResult, result);

    return Optional.ofNullable(solverResult == Status.SATISFIABLE ? solver.getModel() : null);
  }

  private void buildAndPrintOD(TypeIfMcExprConverter exprConverter, Model model, String odName) {

    Optional<ASTODArtifact> od = exprConverter.buildOD(model, odName);
    Assertions.assertTrue(od.isPresent());
    IOHelper.printOD(od.get(), Path.of("target/mc2smt/"));
  }

  private ASTMCType buildMCType(String name) {
    return OD4ReportMill.mCQualifiedTypeBuilder()
        .setMCQualifiedName(MCQualifiedNameFacade.createQualifiedName(name))
        .build();
  }

  private ASTExpression getExpression(int invPos) { // a.speed > a.speed ;
    return ((ASTOCLInvariant) oclAST.getOCLArtifact().getOCLConstraint(invPos)).getExpression();
  }
}
