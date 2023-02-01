package de.monticore.ocl2smt.ocl2smt.expressionconverter;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.expressions.commonexpressions._ast.ASTBracketExpression;
import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.oclexpressions._ast.ASTIfThenElseExpression;
import de.monticore.ocl.oclexpressions._ast.ASTImpliesExpression;
import de.monticore.ocl.oclexpressions._ast.ASTOCLAtPreQualification;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTStrategy;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.ocl2smt.util.SMTSet;
import de.monticore.ocl2smt.util.TypeConverter;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.se_rwth.commons.logging.Log;
import java.util.Optional;

public class FullOCLExpressionConverter extends OCLExpressionConverter {
  protected OCL2SMTStrategy strategy = new OCL2SMTStrategy();

  public Expr<? extends Sort> thisObj;

  public FullOCLExpressionConverter(ASTCDCompilationUnit ast, Context ctx) {
    super(ast, ctx);
  }

  public void enterPreCond() {
    strategy.enterPreCond();
  }

  public void exitPreCond() {
    strategy.exitPreCond();
  }

  @Override
  protected Optional<Expr<? extends Sort>> convertGenExprOpt(ASTExpression node) {
    Expr<? extends Sort> res;
    if (node instanceof ASTLiteralExpression) {
      res = constConverter.convert((ASTLiteralExpression) node);
    } else if (node instanceof ASTBracketExpression) {
      res = convertBracket((ASTBracketExpression) node);
    } else if (node instanceof ASTNameExpression) {
      res = convertName((ASTNameExpression) node);
    } else if (node instanceof ASTFieldAccessExpression) {
      res = convertFieldAcc((ASTFieldAccessExpression) node);
    } else if (node instanceof ASTIfThenElseExpression) {
      res = convertIfTEl((ASTIfThenElseExpression) node);
    } else if (node instanceof ASTImpliesExpression) {
      res = convertImpl((ASTImpliesExpression) node);
    } else if (node instanceof ASTOCLAtPreQualification) {
      res = convertAt((ASTOCLAtPreQualification) node);
    } else {
      return Optional.empty();
    }
    return Optional.of(res);
  }

  protected Expr<? extends Sort> convertAt(ASTOCLAtPreQualification node) {
    strategy.enterPre();
    return convertExpr(node.getExpression());
  }

  @Override
  protected Expr<? extends Sort> convertName(ASTNameExpression node) {
    boolean isPre = strategy.isPreStrategy();
    strategy.exitPre();
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
    boolean isPre = strategy.isPreStrategy();
    strategy.exitPre();
    String name = node.getName();
    if (isPre) {
      name = OCL2SMTStrategy.mkPre(name);
    }
    return convertFieldAccessHelper(node.getExpression(), name);
  }

  @Override
  protected Expr<? extends Sort> convertFieldAcc(ASTFieldAccessExpression node) {
    boolean isPre = strategy.isPreStrategy();
    strategy.exitPre();
    String attributeName = node.getName();
    if (isPre) {
      attributeName = OCL2SMTStrategy.mkPre(attributeName);
    }
    Expr<? extends Sort> obj = convertExpr(node.getExpression());
    OCLType type = constConverter.getType(obj);
    return OCLHelper.getAttribute(obj, type, attributeName, cd2smtGenerator);
  }

  private Optional<Expr<? extends Sort>> getContextAttribute(
      ASTNameExpression node, boolean isPre) {
    // TODO::update to takeCare when the attribute is inherited
    String attributeName = node.getName();
    if (isPre) {
      attributeName = OCL2SMTStrategy.mkPre(node.getName());
    }
    return Optional.ofNullable(
        OCLHelper.getAttribute(
            thisObj, constConverter.getType(thisObj), attributeName, cd2smtGenerator));
  }

  /** this function is use to get a Linked object of the Context */
  private Optional<Expr<? extends Sort>> getContextLink(ASTNameExpression node, boolean isPre) {
    String role = node.getName();
    if (isPre) {
      role = OCL2SMTStrategy.mkPre(role);
    }
    // TODO::update to takeCare when the assoc is inherited
    ASTCDAssociation association =
        OCLHelper.getAssociation(constConverter.getType(thisObj), role, getCD());
    if (association == null) {
      return Optional.empty();
    }

    // declare the linked object
    OCLType type2 = OCLHelper.getOtherType(association, constConverter.getType(thisObj));
    String name = strategy.mkObjName(node.getName(), isPre);
    Expr<? extends Sort> expr = constConverter.declObj(type2, name);

    // add association constraints to the general constraints
    genConstraints.add(
        OCLHelper.evaluateLink(association, thisObj, expr, cd2smtGenerator, constConverter));

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
    boolean isPre = strategy.isPreStrategy();
    String role = node.getName();
    strategy.exitPre();
    if (isPre) {
      role = OCL2SMTStrategy.mkPre(node.getName());
    }

    return convertSimpleFieldAccessAssoc(thisObj, role);
  }
}
