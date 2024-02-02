package de.monticore.ocl2smt.ocl2smt.oclExpr2smt;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.oclexpressions._ast.ASTOCLAtPreQualification;
import de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory.CDExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprFactory.ExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeFactorry.TypeFactory;
import de.monticore.ocl2smt.util.OCLMethodResult;
import de.monticore.types.mcbasictypes._ast.ASTMCReturnType;

/** This class convert All OCL-Expressions including @Pre-Expressions in SMT */
public class FullOCLExprConverter<EXPR extends ExprAdapter<?, TYPE>, TYPE>
    extends OCLExprConverter<EXPR, TYPE> {
  private boolean isPreStrategy = false;
  private boolean isPreCond = false;

  private EXPR thisObj;

  private OCLMethodResult result;

  public FullOCLExprConverter(
      ExprFactory<EXPR, TYPE> factory,
      CDExprFactory<EXPR, TYPE> cdFactory,
      TypeFactory<TYPE> typeFactory) {
    super(factory, cdFactory, typeFactory);
  }

  public void enterPre() {
    isPreStrategy = true;
  }

  public void exitPre() {
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

  public void setThisObj(EXPR thisObj) {
    this.thisObj = thisObj;
  }

  public void setResultType(ASTMCReturnType type) {
    result = new OCLMethodResult();
    result.setType(type);
  }

  public OCLMethodResult getResult() {
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
    EXPR result = null;

    if (node instanceof ASTOCLAtPreQualification) {
      result = convertAt((ASTOCLAtPreQualification) node);
    }
    if (result == null) {
      result = super.convertExpr(node);
    }

    return result;
  }

  protected EXPR convertAt(ASTOCLAtPreQualification node) {
    enterPre();
    return convertExpr(node.getExpression());
  }

  @Override
  protected EXPR convert(ASTNameExpression node) {
    /*   boolean isPre = isPreStrategy();
    exitPre();
    T res = null;

    if (varNames.containsKey(node.getName())) {
      res = varNames.get(node.getName());
    }
    if (thisObj != null) {
      Optional<T> attr = getContextAttribute(node, isPre);
      if (attr.isPresent()) {
        res = attr.get();
      }
      Optional<T> obj = getContextLink(node, isPre);
      if (obj.isPresent()) {
        res = obj.get();
      }
    }

    if (res == null) {
      res = createVarFromSymbol(node);
    }
    if (node.getName().equals("result")) {
      result.setValue(res);
      result.setType(getType(res));
    }
    return res;*/
    return null;
  }
  /*
  @Override
  protected T convertFieldAccessSet(ASTFieldAccessExpression node) {
    boolean isPre = isPreStrategy();
    exitPre();
    String name = node.getName();
    if (isPre) {
      name = OCLHelper.mkPre(name);
    }
    return convertFieldAccessSetHelper(node.getExpression(), name);
  }

  @Override
  protected T convert(ASTFieldAccessExpression node) {
    boolean isPre = isPreStrategy();
    exitPre();
    String attributeName = node.getName();
    if (isPre) {
      attributeName = OCLHelper.mkPre(attributeName);
    }
    T obj = convertExpr(node.getExpression());
    Pair<T, T> res = convertFieldAccessSetHelper(obj, attributeName);
    if (!TypeConverter.hasOptionalType(node)) {
      genConstraints.add(res.getRight());
    }
    return res.getLeft();
  }

  private Optional<T> getContextAttribute(ASTNameExpression node, boolean isPre) {
    /*  // TODO::update to takeCare when the attribute is inherited
     String attributeName = node.getName();
     if (isPre) {
       attributeName = OCLHelper.mkPre(node.getName());
     }
     return Optional.ofNullable(
         OCLHelper.getAttribute(thisObj.expr(), getType(thisObj), attributeName, cd2smtGenerator));/*

    return Optional.empty();
  }*/

  /** this function is used to get a Linked object of the Context */
  /* private Optional<T> getContextLink(ASTNameExpression node, boolean isPre) {
    String role = node.getName();
    if (isPre) {
      role = OCLHelper.mkPre(role);
    }
    // TODO::update to takeCare when the assoc is inherited
    ASTCDAssociation association = OCLHelper.getAssociation(getType(thisObj), role, getCD());
    if (association == null) {
      return Optional.empty();
    }

    // declare the linked object
    OCLType type2 = OCLHelper.getOtherType(association, getType(thisObj), role, getCD());

    String name = mkObjName(node.getName(), isPre);
    T expr = declVariable(type2, name);

    // add association constraints to the general constraints
    genConstraints.add(
        ExprMill.exprBuilder(ctx)
            .mkBool(
                OCLHelper.evaluateLink(
                    association, thisObj, role, expr, cd2smtGenerator, this::getType)));

    return Optional.of(expr);
  }*/

  public EXPR convertSet(ASTExpression node) {
    /* Optional<T> res = convertSetOpt(node);
    if (res.isPresent()) {
      return res.get();
    } else {
      if (node instanceof ASTNameExpression) {
        return convertNameSet((ASTNameExpression) node);
      }
      Log.error("conversion of Set of the type " + node.getClass().getName() + " not implemented");
    }
    return null; */
    // todo fixme
    return null;
  }

  protected EXPR convertNameSet(ASTNameExpression node) {
    return null;
  }
  /*  T res = null;
    boolean isPre = isPreStrategy();
    String role = node.getName();
    exitPre();
    if (role.equals("result")) {
      FuncDecl<BoolSort> setFunc =
          ctx.mkFuncDecl(
              "result",
              cd2smtGenerator.getSort(
                  CDHelper.getASTCDType(result.getOclType().getName(), getCD())),
              ctx.mkBoolSort());
      res =
          ExprMill.exprBuilder(ctx)
              .mkSet(
                  x -> ExprMill.exprBuilder(ctx).mkBool((BoolExpr) ctx.mkApp(setFunc, x.expr())),
                  result.getOclType(),
                  this);
      result.setValue(setFunc);
    } else {

      if (isPre) {
        role = OCLHelper.mkPre(node.getName());
      }
      if (Optional.ofNullable(OCLHelper.getAssociation(getType(thisObj), role, getCD()))
          .isPresent()) {

        res = convertSimpleFieldAccessSet(thisObj, role);

      } else {
        Log.error("Cannot convert the Set " + thisObj + "." + role);
        assert false;
      }
    }
    return res;
  }

  public String mkObjName(String name, boolean isPre) {
    if (isPre) {
      return mkPre(name);
    }
    return name;
  }*/
}
