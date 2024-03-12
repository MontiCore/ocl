package de.monticore.ocl2smt.ocl2smt.oclExpr2smt;

import com.microsoft.z3.Context;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory.CDExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3ExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3TypeFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeFactorry.TypeFactory;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;
import java.util.function.Function;

public class MCExprConverter extends OCLExprConverter<Z3ExprAdapter> {

  private Function<String, ASTMCType> typeOfNamedVariable;

  public static MCExprConverter getInstance(ASTCDCompilationUnit cd, Context ctx) {

    CD2SMTGenerator cd2SMTGenerator =
        new CD2SMTGenerator(
            ClassStrategy.Strategy.DS,
            InheritanceData.Strategy.ME,
            AssociationStrategy.Strategy.DEFAULT);
    cd2SMTGenerator.cd2smt(cd, ctx);

    Z3TypeFactory tFactory = new Z3TypeFactory(cd2SMTGenerator);
    CDExprFactory<Z3ExprAdapter> cdExprFactory = new Z3ExprFactory(tFactory, cd2SMTGenerator);

    return new MCExprConverter(cdExprFactory, tFactory);
  }

  private MCExprConverter(CDExprFactory<Z3ExprAdapter> eFactory, TypeFactory typeFactory) {
    super(eFactory, typeFactory);
  }

  public Z3ExprAdapter convertExpr(
      ASTExpression expr, Function<String, ASTMCType> typeOfNamedVariable) {
    this.typeOfNamedVariable = typeOfNamedVariable;
    return convertExpr(expr);
  }

  @Override
  protected Z3ExprAdapter convert(ASTNameExpression node) {
    if (varNames.containsKey(node.getName())) {
      return varNames.get(node.getName());
    } else {
      ASTMCType type = typeOfNamedVariable.apply(node.getName());
      if (type != null) {
        TypeAdapter tAdapter = tFactory.adapt(type);
        return mkConst(node.getName(), tAdapter);
      } else {
        Log.error("cannot resolve variable " + node.getName());
        return null;
      }
    }
  }
}
