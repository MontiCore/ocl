package de.monticore.ocl2smt.ocl2smt.oclExpr2smt;

import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl.types3.OCLTypeTraverserFactory;
import de.monticore.ocl.oclexpressions._ast.ASTOCLAtPreQualification;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory.CDExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeFactorry.TypeFactory;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.Type4Ast;
import de.se_rwth.commons.logging.Log;
import java.util.List;

/** This class convert All OCL-Expressions including @Pre-Expressions in SMT */
public class FullOCLExprConverter<EXPR extends ExprAdapter<?>> extends OCLExprConverter<EXPR> {
  private boolean isPreStrategy = false;
  private boolean isPreCond = false;
  private EXPR thisObj;
  private EXPR result;

  public FullOCLExprConverter(CDExprFactory<EXPR> cdFactory, TypeFactory typeFactory) {
    super(cdFactory, typeFactory);
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

    // case result
    if (node.getName().equals("result")) {
      SymTypeExpression typeExpr = deriveType(node);
      TypeAdapter type = tFactory.adapt(typeExpr);
      result = mkConst(node.getName(), type);
      return result;
    }

    // case it is a link as ASTName
    String role = isPreStrategy() ? OCLHelper.mkPre(node.getName()) : node.getName();
    EXPR res = eFactory.getLink(thisObj, role);

    exitPreStrategy();
    return res;
  }

  @Override
  protected EXPR convert(ASTFieldAccessExpression node) {
    String role = isPreStrategy() ? OCLHelper.mkPre(node.getName()) : node.getName();
    exitPreStrategy();

    EXPR obj = convertExpr(node.getExpression());
    return eFactory.getLink(obj, role);
  }

  public void setThisObj(EXPR thisObj) {
    this.thisObj = thisObj;
  }

  public void setParams(List<String> names, List<EXPR> right) {
    for (int i = 0; i < names.size(); i++) {
      varNames.put(names.get(i), right.get(i));
    }
  }

  public static SymTypeExpression deriveType(ASTExpression node) {
    Type4Ast type4Ast = new Type4Ast();
    OCLTraverser typeMapTraverser = new OCLTypeTraverserFactory().createTraverser(type4Ast);
    node.accept(typeMapTraverser);
    SymTypeExpression typeExpr = type4Ast.getTypeOfExpression(node);
    if (typeExpr == null) {
      Log.error("Unable to derive the type of the expression " + IOHelper.print(node));
      assert false;
    }

    return typeExpr;
  }
}
