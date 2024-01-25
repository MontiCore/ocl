package de.monticore.ocl2smt.ocl2smt.expressionconverter;

import static de.monticore.ocl2smt.helpers.OCLHelper.mkPre;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.oclexpressions._ast.ASTOCLAtPreQualification;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocl2smt.expr.ExprBuilder;
import de.monticore.ocl2smt.ocl2smt.expr.ExprMill;
import de.monticore.ocl2smt.ocl2smt.expr.ExpressionKind;
import de.monticore.ocl2smt.util.OCLMethodResult;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.ocl2smt.util.TypeConverter;
import de.monticore.types.mcbasictypes._ast.ASTMCReturnType;
import de.se_rwth.commons.logging.Log;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

/** This class convert All OCL-Expressions including @Pre-Expressions in SMT */
public class FullOCLExpressionConverter extends OCLExprConverter {
  private boolean isPreStrategy = false;
  private boolean isPreCond = false;

  private ExprBuilder thisObj;

  private OCLMethodResult result;

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

  public void setThisObj(ExprBuilder thisObj) {
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

  public FullOCLExpressionConverter(ASTCDCompilationUnit ast, Context ctx) {
    super(ast, ctx);
  }

  @Override
  public ExprBuilder convertExpr(ASTExpression node) {
    ExprBuilder res = super.convertExpr(node);
    if (res.getKind() != ExpressionKind.NULL) {
      return res;
    } else if (node instanceof ASTOCLAtPreQualification) {
      res = convertAt((ASTOCLAtPreQualification) node);
    }
    return res;
  }

  protected ExprBuilder convertAt(ASTOCLAtPreQualification node) {
    enterPre();
    return convertExpr(node.getExpression());
  }

  @Override
  protected ExprBuilder convert(ASTNameExpression node) {
    boolean isPre = isPreStrategy();
    exitPre();
    ExprBuilder res = null;

    if (varNames.containsKey(node.getName())) {
      res = varNames.get(node.getName());
    }
    if (thisObj != null) {
      Optional<ExprBuilder> attr = getContextAttribute(node, isPre);
      if (attr.isPresent()) {
        res = attr.get();
      }
      Optional<ExprBuilder> obj = getContextLink(node, isPre);
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
    return res;
  }

  @Override
  protected ExprBuilder convertFieldAccessSet(ASTFieldAccessExpression node) {
    boolean isPre = isPreStrategy();
    exitPre();
    String name = node.getName();
    if (isPre) {
      name = OCLHelper.mkPre(name);
    }
    return convertFieldAccessSetHelper(node.getExpression(), name);
  }

  @Override
  protected ExprBuilder convert(ASTFieldAccessExpression node) {
    boolean isPre = isPreStrategy();
    exitPre();
    String attributeName = node.getName();
    if (isPre) {
      attributeName = OCLHelper.mkPre(attributeName);
    }
    ExprBuilder obj = convertExpr(node.getExpression());
    Pair<ExprBuilder, ExprBuilder> res = convertFieldAccessSetHelper(obj, attributeName);
    if (!TypeConverter.hasOptionalType(node)) {
      genConstraints.add(res.getRight());
    }
    return res.getLeft();
  }

  private Optional<ExprBuilder> getContextAttribute(ASTNameExpression node, boolean isPre) {
    /*  // TODO::update to takeCare when the attribute is inherited
     String attributeName = node.getName();
     if (isPre) {
       attributeName = OCLHelper.mkPre(node.getName());
     }
     return Optional.ofNullable(
         OCLHelper.getAttribute(thisObj.expr(), getType(thisObj), attributeName, cd2smtGenerator));/*
    */
    return Optional.empty();
  }

  /** this function is used to get a Linked object of the Context */
  private Optional<ExprBuilder> getContextLink(ASTNameExpression node, boolean isPre) {
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
    ExprBuilder expr = declVariable(type2, name);

    // add association constraints to the general constraints
    genConstraints.add(
        ExprMill.exprBuilder(ctx)
            .mkBool(
                OCLHelper.evaluateLink(
                    association, thisObj, role, expr, cd2smtGenerator, this::getType)));

    return Optional.of(expr);
  }

  public ExprBuilder convertSet(ASTExpression node) {
    /* Optional<ExprBuilder> res = convertSetOpt(node);
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

  protected ExprBuilder convertNameSet(ASTNameExpression node) {
    ExprBuilder res = null;
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
  }
}
