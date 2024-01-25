package de.monticore.ocl2smt.ocl2smt.expr2smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.oclexpressions._ast.ASTExistsExpression;
import de.monticore.ocl.oclexpressions._ast.ASTForallExpression;
import de.monticore.ocl2smt.ocl2smt.expr.ExpressionKind;
import de.monticore.ocl2smt.ocl2smt.expr2smt.oclExprFactory.OCLExprFactory;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.ocl2smt.util.TypeConverter;

import java.util.*;
import java.util.stream.Collectors;

import static de.monticore.cd2smt.Helper.CDHelper.getASTCDType;

public class Z3OCLExprFactory implements OCLExprFactory<Z3ExprAdapter> {
  TypeConverter typeConverter ;
  Z3ExprFactory exprFactory ;
  protected Map<String, Z3ExprAdapter> varNames;
  protected Map<Z3ExprAdapter, OCLType> varTypes;
  Z3OCLExprFactory(TypeConverter typeConverter,Z3ExprFactory exprFactory){
    varNames = new HashMap<>();
    varTypes = new HashMap<>();
    this.typeConverter = typeConverter ;
    this.exprFactory = exprFactory;
  }
  Context ctx ;
  @Override
  public Z3ExprAdapter mkConst(String name, OCLType type) {

      Z3ExprAdapter expr = new Z3ExprAdapter();
    expr.setExpr(ctx.mkConst(name, typeConverter.deriveSort(type)));
    expr.setKind(ExpressionKind.UNINTERPRETED);
      varNames.put(name, expr);
      varTypes.put(expr, type);
      return expr;

  }



}
