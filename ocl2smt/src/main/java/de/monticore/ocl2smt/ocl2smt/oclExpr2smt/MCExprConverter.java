package de.monticore.ocl2smt.ocl2smt.oclExpr2smt;

import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory.CDExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.*;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeFactorry.TypeFactory;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;
import java.util.Optional;
import java.util.function.Function;

public class MCExprConverter extends OCLExprConverter<Z3ExprAdapter> {

  protected Function<ASTNameExpression, ASTMCType> typeOfNamedVariable;
  protected CD2SMTGenerator cd2SMTGenerator;

  public static MCExprConverter getInstance(ASTCDCompilationUnit cd, Context ctx) {
    CD2SMTGenerator cd2SMTGenerator =
        new CD2SMTGenerator(
            ClassStrategy.Strategy.DS,
            InheritanceData.Strategy.ME,
            AssociationStrategy.Strategy.DEFAULT);
    cd2SMTGenerator.cd2smt(cd, ctx);

    Z3TypeFactory tFactory = new Z3TypeFactory(cd2SMTGenerator);
    CDExprFactory<Z3ExprAdapter> cdExprFactory = new Z3ExprFactory(tFactory, cd2SMTGenerator);

    return new MCExprConverter(cdExprFactory, tFactory, cd2SMTGenerator);
  }

  protected MCExprConverter(
      CDExprFactory<Z3ExprAdapter> eFactory,
      TypeFactory typeFactory,
      CD2SMTGenerator cd2SMTGenerator) {
    super(eFactory, typeFactory);
    this.cd2SMTGenerator = cd2SMTGenerator;
  }

  public Z3ExprAdapter convertExpr(
      ASTExpression expr, Function<ASTNameExpression, ASTMCType> typeOfNamedVariable) {
    this.typeOfNamedVariable = typeOfNamedVariable;
    return convertExpr(expr);
  }

  @Override
  protected Z3ExprAdapter convert(ASTNameExpression node) {
    if (varNames.containsKey(node.getName())) {
      return varNames.get(node.getName());
    } else {
      ASTMCType type = typeOfNamedVariable.apply(node);
      if (type != null) {
        TypeAdapter tAdapter = tFactory.adapt(type);
        return mkConst(node.getName(), tAdapter);
      } else {
        Log.error("cannot resolve variable " + node.getName());
        return null;
      }
    }
  }

  public Optional<ASTODArtifact> buildOD(Model model, String odName) {
    return cd2SMTGenerator.smt2od(model, false, odName);
  }
}
