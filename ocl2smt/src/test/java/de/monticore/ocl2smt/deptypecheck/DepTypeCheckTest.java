package de.monticore.ocl2smt.deptypecheck;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.BoolSort;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDCompilationUnitBuilder;
import de.monticore.cdbasis._ast.ASTCDDefinitionBuilder;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl2smt.ocl2smt.ExpressionAbstractTest;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3ExprFactory;
import de.monticore.ocl2smt.ocl2smt.oclExpr2smt.MCExprConverter;
import de.monticore.ocl2smt.visitors.NameExpressionCollector;
import de.monticore.statements.mcstatementsbasis._ast.ASTMCStatement;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.umlmodifier._ast.ASTModifierBuilder;
import org.checkerframework.dataflow.qual.TerminatesExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class DepTypeCheckTest extends ExpressionAbstractTest {
  @BeforeEach
  public void setup() {
    initMills();
    initLogger();
  }

  @Test
  public void natDepTypeCheck() throws IOException {
    ASTExpression xCondition = OCLMill.parser().parse_StringExpression("x>5").get();
    ASTExpression yCondition = OCLMill.parser().parse_StringExpression("y>1").get();

    Map<String,ASTExpression> condMap = new HashMap<>();
    condMap.put("x", xCondition);
    condMap.put("y", yCondition);
    Function<ASTNameExpression,ASTExpression> getCond = z -> condMap.get(z.getName());


    Function<ASTNameExpression,ASTMCType> getType = z -> OCLMill.mCPrimitiveTypeBuilder().setPrimitive(6).build(); // ... dafuq? gibts kein ENUM?
    ASTNameExpression zName = OCLMill.nameExpressionBuilder().setName("z").build();

    {
      // Check "int{z>6} z = x:int{x>5} + y:int{y>1}     --> Valid
      ASTExpression zCondition = OCLMill.parser().parse_StringExpression("z>6").get();
      ASTExpression zValue = OCLMill.parser().parse_StringExpression("x+y").get();
      assertTrue(isTypeCorrect(zName, zValue, zCondition, getCond, getType));
    }

    {
      // Check "int{z>10} z = x:int{x>5} + y:int{y>1}     --> Invalid and Counterexample
      ASTExpression zCondition = OCLMill.parser().parse_StringExpression("z>10").get();
      ASTExpression zValue = OCLMill.parser().parse_StringExpression("x+y").get();
      assertFalse(isTypeCorrect(zName, zValue, zCondition, getCond, getType));
    }
  }

  /**
   *
   * @param zName Name of the Variable that is typechecked. Is also used in "zCondition" parameter
   * @param zValue How the Variable-Value is computed
   * @param zCondition Type-Condition for the Variable
   * @param getCondition Conditions for other namedVariables that are used in "zValue"
   * @param getType get Types for Expressions
   * @return true iff type-condition is fulfilled
   */
  private boolean isTypeCorrect(ASTNameExpression zName, ASTExpression zValue, ASTExpression zCondition,
                                Function<ASTNameExpression,ASTExpression> getCondition,
                                Function<ASTNameExpression,ASTMCType> getType) {
    Context ctx = new Context();
    solver = ctx.mkSolver();

    ASTCDCompilationUnit cdAst = new ASTCDCompilationUnitBuilder()
        .setCDDefinition(
            new ASTCDDefinitionBuilder()
                .setName("EmptyCD")
                .setModifier(new ASTModifierBuilder().build())
                .build())
        .build();
    MCExprConverter exprConverter = MCExprConverter.getInstance(cdAst, ctx);

    Set<ASTNameExpression> names = null;

    {
      NameExpressionCollector namedExpr = new NameExpressionCollector();
      OCLTraverser trav = OCLMill.traverser();
      trav.add4ExpressionsBasis(namedExpr);
      zValue.accept(trav);

      names = namedExpr.getVariableNames();
      for(ASTNameExpression usedName:names){
        ASTExpression ConditionForName = getCondition.apply(usedName);
        Z3ExprAdapter nameCond = exprConverter.convertExpr(ConditionForName,getType);
        solver.add((BoolExpr) nameCond.getExpr());
      }
    }

    Z3ExprAdapter resultExpr = exprConverter.convertExpr(zName, getType);
    {
      Z3ExprAdapter expr = exprConverter.convertExpr(zValue, getType);
      Expr<BoolSort> equals = ctx.mkEq(resultExpr.getExpr(), expr.getExpr());
      solver.add(equals);
    }

    {
      Z3ExprAdapter expr = exprConverter.convertExpr(zCondition, getType);
      Expr<BoolSort> negated = ctx.mkNot((BoolExpr) expr.getExpr());
      solver.add(negated);
    }

    switch (solver.check()){
      case SATISFIABLE: {
        String value = "";
        names.add(zName);
        for(ASTNameExpression usedName:names){
          value += "\n\t" + usedName.getName() + "\t=\t" + solver.getModel().eval(exprConverter.convertExpr(usedName, getType).getExpr(), true);
        }
        System.err.println("Counterexample " + value);
        return false;
      }
      case UNSATISFIABLE: {
        return true;
      }
      default: throw new RuntimeException();
    }
  }
}
