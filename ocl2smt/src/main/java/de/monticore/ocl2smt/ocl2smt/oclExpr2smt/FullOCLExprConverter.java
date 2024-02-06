package de.monticore.ocl2smt.ocl2smt.oclExpr2smt;

import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.oclexpressions._ast.ASTOCLAtPreQualification;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory.CDExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprFactory.ExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeFactorry.TypeFactory;
import java.util.List;

/** This class convert All OCL-Expressions including @Pre-Expressions in SMT */
public class FullOCLExprConverter<EXPR extends ExprAdapter<?, TYPE>, TYPE>
    extends OCLExprConverter<EXPR, TYPE> {
  private boolean isPreStrategy = false;
  private boolean isPreCond = false;
  private EXPR thisObj;
  private EXPR result;

  public FullOCLExprConverter(
      ExprFactory<EXPR, TYPE> factory,
      CDExprFactory<EXPR> cdFactory,
      TypeFactory<TYPE> typeFactory) {
    super(factory, cdFactory, typeFactory);
  }

  public void enterPreStrategy() {
    isPreStrategy = true;
  }

  public void exitPreStrategy() {
    if (!isPreCond) {
      this.isPreStrategy = false;
    }
  }

  @Override
  public void reset() {
    super.reset();
    isPreStrategy = false;
    isPreCond = false;
    thisObj = null;
    result = null;
  }

  public EXPR getResult() {
    return result;
  }

  public void enterPreCond() {
    isPreCond = true;
    isPreStrategy = true;
  }

  public void exitPreCond() {
    isPreCond = false;
    isPreStrategy = false;
  }

  public boolean isPreStrategy() {
    return isPreStrategy;
  }

  @Override
  public EXPR convertExpr(ASTExpression node) {
    if (node instanceof ASTOCLAtPreQualification) {
      return convertAt((ASTOCLAtPreQualification) node);
    } else {
      return super.convertExpr(node);
    }
  }

  protected EXPR convertAt(ASTOCLAtPreQualification node) {
    enterPreStrategy();
    return convertExpr(node.getExpression());
  }

  @Override
  protected EXPR convert(ASTNameExpression node) {

    // case params
    if (varNames.containsKey(node.getName())) {
      return varNames.get(node.getName());
    }

    String role = isPreStrategy() ? OCLHelper.mkPre(node.getName()) : node.getName();
    EXPR res = cdFactory.getLink(thisObj, role);

    result = node.getName().equals("result") ? res : null;

    exitPreStrategy();
    return res;
  }

  @Override
  protected EXPR convert(ASTFieldAccessExpression node) {
    String role = isPreStrategy() ? OCLHelper.mkPre(node.getName()) : node.getName();
    exitPreStrategy();

    EXPR obj = convertExpr(node.getExpression());
    return cdFactory.getLink(obj, role);
  }

  public void setThisObj(EXPR thisObj) {
    this.thisObj = thisObj;
  }

  public void setParams(List<String> names, List<EXPR> right) {
    for (int i = 0; i < names.size(); i++) {
      varNames.put(names.get(i), right.get(i));
    }
  }
}
