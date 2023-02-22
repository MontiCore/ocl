package de.monticore.ocl2smt.ocl2smt.expressionconverter;

import static de.monticore.ocl2smt.helpers.OCLHelper.mkPre;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.oclexpressions._ast.ASTOCLAtPreQualification;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.ocl2smt.util.SMTSet;
import de.monticore.ocl2smt.util.TypeConverter;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.se_rwth.commons.logging.Log;
import java.util.Optional;

/** This class convert All OCL-Expressions including @Pre-Expressions in SMT */
public class FullOCLExpressionConverter extends OCLExpressionConverter {
  private boolean isPreStrategy = false;
  private boolean isPreCond = false;

  private Expr<? extends Sort> thisObj;

  public void enterPre() {
    isPreStrategy = true;
  }

  public void exitPre() {
    if (!isPreCond) {
      this.isPreStrategy = false;
    }
  }

  public void setThisObj(Expr<? extends Sort> thisObj) {
    this.thisObj = thisObj;
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
  protected Expr<? extends Sort> convertName(ASTNameExpression node) {
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
      OCLType type = TypeConverter.buildOCLType((VariableSymbol) node.getDefiningSymbol().get());
      res = declVariable(type, node.getName());
    }

    return res;
  }

  @Override
  protected SMTSet convertFieldAccAssoc(ASTFieldAccessExpression node) {
    boolean isPre = isPreStrategy();
    exitPre();
    String name = node.getName();
    if (isPre) {
      name = OCLHelper.mkPre(name);
    }
    return convertFieldAccessHelper(node.getExpression(), name);
  }

  @Override
  protected Expr<? extends Sort> convertFieldAcc(ASTFieldAccessExpression node) {
    boolean isPre = isPreStrategy();
    exitPre();
    String attributeName = node.getName();
    if (isPre) {
      attributeName = OCLHelper.mkPre(attributeName);
    }
    Expr<? extends Sort> obj = convertExpr(node.getExpression());
    OCLType type = literalConverter.getType(obj);
    return OCLHelper.getAttribute(obj, type, attributeName, cd2smtGenerator);
  }

  private Optional<Expr<? extends Sort>> getContextAttribute(
      ASTNameExpression node, boolean isPre) {
    // TODO::update to takeCare when the attribute is inherited
    String attributeName = node.getName();
    if (isPre) {
      attributeName = OCLHelper.mkPre(node.getName());
    }
    return Optional.ofNullable(
        OCLHelper.getAttribute(
            thisObj, literalConverter.getType(thisObj), attributeName, cd2smtGenerator));
  }

  /** this function is used to get a Linked object of the Context */
  private Optional<Expr<? extends Sort>> getContextLink(ASTNameExpression node, boolean isPre) {
    String role = node.getName();
    if (isPre) {
      role = OCLHelper.mkPre(role);
    }
    // TODO::update to takeCare when the assoc is inherited
    ASTCDAssociation association =
        OCLHelper.getAssociation(literalConverter.getType(thisObj), role, getCD());
    if (association == null) {
      return Optional.empty();
    }

    // declare the linked object
    OCLType type2 = OCLHelper.getOtherType(association, literalConverter.getType(thisObj));
    String name = mkObjName(node.getName(), isPre);
    Expr<? extends Sort> expr = literalConverter.declObj(type2, name);

    // add association constraints to the general constraints
    genConstraints.add(
        OCLHelper.evaluateLink(association, thisObj, expr, cd2smtGenerator, literalConverter));

    return Optional.of(expr);
  }

  @Override
  protected SMTSet convertSet(ASTExpression node) {
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
    boolean isPre = isPreStrategy();
    String role = node.getName();
    exitPre();
    if (isPre) {
      role = OCLHelper.mkPre(node.getName());
    }

    return convertSimpleFieldAccessAssoc(thisObj, role);
  }

  public String mkObjName(String name, boolean isPre) {
    if (isPre) {
      return mkPre(name);
    }
    return name;
  }
}
