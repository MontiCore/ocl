package de.monticore.ocl2smt.ocl2smt.oclExpr2smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.oclexpressions._ast.ASTTypeIfExpression;
import de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory.CDExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3ExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3TypeAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3TypeFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeFactorry.TypeFactory;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TypeIfMcExprConverter extends MCExprConverter {
  public TypeIfMcExprConverter(
      CDExprFactory<Z3ExprAdapter> eFactory,
      TypeFactory typeFactory,
      CD2SMTGenerator cd2SMTGenerator) {
    super(eFactory, typeFactory, cd2SMTGenerator);
  }

  public static TypeIfMcExprConverter getInstance(ASTCDCompilationUnit cd, Context ctx) {
    CD2SMTGenerator cd2SMTGenerator =
        new CD2SMTGenerator(
            ClassStrategy.Strategy.SSCOMB,
            InheritanceData.Strategy.SE,
            AssociationStrategy.Strategy.DEFAULT);
    cd2SMTGenerator.cd2smt(cd, ctx);

    Z3TypeFactory tFactory = new Z3TypeFactory(cd2SMTGenerator);
    CDExprFactory<Z3ExprAdapter> cdExprFactory = new Z3ExprFactory(tFactory, cd2SMTGenerator);

    return new TypeIfMcExprConverter(cdExprFactory, tFactory, cd2SMTGenerator);
  }

  public Z3ExprAdapter convertExpr(
      ASTExpression expr, Function<ASTNameExpression, ASTMCType> typeOfNamedVariable) {
    this.typeOfNamedVariable = typeOfNamedVariable;

    Z3ExprAdapter res = convertExpr(expr);

    List<BoolExpr> constraints = new ArrayList<>();
    cd2SMTGenerator.getClassConstraints().forEach(c -> constraints.add(c.getValue()));
    cd2SMTGenerator.getAssociationsConstraints().forEach(c -> constraints.add(c.getValue()));
    cd2SMTGenerator.getInheritanceConstraints().forEach(c -> constraints.add(c.getValue()));

    List<Z3ExprAdapter> genConstraints =
        constraints.stream()
            .map(e -> new Z3ExprAdapter(e, (Z3TypeAdapter) tFactory.mkBoolType()))
            .collect(Collectors.toList());
    res.addGenConstraint(genConstraints);

    return res;
  }

  @Override
  public Z3ExprAdapter convertExpr(ASTExpression node) {
    if (node instanceof ASTTypeIfExpression) {
      return convert((ASTTypeIfExpression) node);
    }
    return super.convertExpr(node);
  }

  protected Z3ExprAdapter convert(ASTTypeIfExpression node) {
    mkConst(node.getName(), tFactory.adapt(node.getNameSymbol().getType()));
    Z3ExprAdapter expr = varNames.get(node.getName());
    expr.setTypeCast((Z3TypeAdapter) tFactory.adapt(node.getMCType()));
    Z3ExprAdapter thenExpr = convertExpr(node.getThenExpression().getExpression());

    Z3ExprAdapter cond = eFactory.instanceOf(expr, tFactory.adapt(node.getMCType()));

    Z3ExprAdapter elseExpr = convertExpr(node.getElseExpression());

    return eFactory.mkIte(cond, thenExpr, elseExpr);
  }

  public Optional<ASTODArtifact> buildOD(Model model, String odName) {
    return cd2SMTGenerator.smt2od(model, false, odName);
  }
}
