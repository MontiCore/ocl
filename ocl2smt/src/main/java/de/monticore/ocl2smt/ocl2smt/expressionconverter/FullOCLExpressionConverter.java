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
import de.monticore.ocl2smt.util.OCLMethodResult;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.ocl2smt.util.SMTSet;
import de.monticore.ocl2smt.util.TypeConverter;
import de.monticore.types.mcbasictypes._ast.ASTMCReturnType;
import de.se_rwth.commons.logging.Log;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

/** This class convert All OCL-Expressions including @Pre-Expressions in SMT */
public class FullOCLExpressionConverter extends OCLExpressionConverter {
  private boolean isPreStrategy = false;
  private boolean isPreCond = false;

  private Expr<? extends Sort> thisObj;

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

  public void setThisObj(Expr<? extends Sort> thisObj) {
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
  protected Optional<Expr<? extends Sort>> convertGenExprOpt(ASTExpression node) {
    Optional<Expr<? extends Sort>> res = super.convertGenExprOpt(node);
    if (res.isPresent()) {
      return res;
    } else if (node instanceof ASTOCLAtPreQualification) {
      res = Optional.ofNullable(convertAt((ASTOCLAtPreQualification) node));
    } else {
      res = Optional.empty();
    }
    return res;
  }

  protected Expr<? extends Sort> convertAt(ASTOCLAtPreQualification node) {
    enterPre();
    return convertExpr(node.getExpression());
  }

  @Override
  protected Expr<? extends Sort> convert(ASTNameExpression node) {
    boolean isPre = isPreStrategy();
    exitPre();
    Expr<? extends Sort> res = null;

    if (varNames.containsKey(node.getName())) {
      res = varNames.get(node.getName());
    }
    if (thisObj != null) {
      Optional<Expr<? extends Sort>> attr = getContextAttribute(node, isPre);
      if (attr.isPresent()) {
        res = attr.get();
      }
      Optional<Expr<? extends Sort>> obj = getContextLink(node, isPre);
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
  protected SMTSet convertFieldAccessSet(ASTFieldAccessExpression node) {
    boolean isPre = isPreStrategy();
    exitPre();
    String name = node.getName();
    if (isPre) {
      name = OCLHelper.mkPre(name);
    }
    return convertFieldAccessSetHelper(node.getExpression(), name);
  }

  @Override
  protected Expr<? extends Sort> convert(ASTFieldAccessExpression node) {
    boolean isPre = isPreStrategy();
    exitPre();
    String attributeName = node.getName();
    if (isPre) {
      attributeName = OCLHelper.mkPre(attributeName);
    }
    Expr<? extends Sort> obj = convertExpr(node.getExpression());
    Pair<Expr<? extends Sort>, BoolExpr> res = convertFieldAccessSetHelper(obj, attributeName);
    if (!TypeConverter.isOptional(node)) {
      genConstraints.add(res.getRight());
    }
    return res.getLeft();
  }

  private Optional<Expr<? extends Sort>> getContextAttribute(
      ASTNameExpression node, boolean isPre) {
    // TODO::update to takeCare when the attribute is inherited
    String attributeName = node.getName();
    if (isPre) {
      attributeName = OCLHelper.mkPre(node.getName());
    }
    return Optional.ofNullable(
        OCLHelper.getAttribute(thisObj, getType(thisObj), attributeName, cd2smtGenerator));
  }

  /** this function is used to get a Linked object of the Context */
  private Optional<Expr<? extends Sort>> getContextLink(ASTNameExpression node, boolean isPre) {
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
    Expr<? extends Sort> expr = declObj(type2, name);

    // add association constraints to the general constraints
    genConstraints.add(
        OCLHelper.evaluateLink(association, thisObj, role, expr, cd2smtGenerator, this::getType));

    return Optional.of(expr);
  }

  @Override
  public SMTSet convertSet(ASTExpression node) {
    Optional<SMTSet> res = convertSetOpt(node);
    if (res.isPresent()) {
      return res.get();
    } else {
      if (node instanceof ASTNameExpression) {
        return convertNameSet((ASTNameExpression) node);
      }
      Log.error("conversion of Set of the type " + node.getClass().getName() + " not implemented");
    }
    return null;
  }

  protected SMTSet convertNameSet(ASTNameExpression node) {
    SMTSet res = null;
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
      res = new SMTSet(x -> (BoolExpr) ctx.mkApp(setFunc, x), result.getOclType(), this);
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
