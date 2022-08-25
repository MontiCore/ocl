package de.monticore.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.cd2smt.context.CDContext;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.ocl._ast.ASTOCLArtifact;
import de.monticore.ocl.ocl._ast.ASTOCLConstraint;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.oclexpressions._ast.ASTForallExpression;
import de.monticore.ocl.oclexpressions._ast.ASTInDeclaration;
import de.monticore.ocl.oclexpressions._ast.ASTInDeclarationVariable;
import de.se_rwth.commons.logging.Log;

import java.util.*;

public class OCL2SMTGenerator {
  protected final CDContext cdcontext;
  protected final LiteralExpressionsConverter literalExpressionsConverter;
  protected final TypeConverter typeConverter;

  protected final Map<String, Expr<? extends Sort>> varNames = new HashMap<>();

  public OCL2SMTGenerator(CDContext cdContext) {
    this.cdcontext = cdContext;
    this.literalExpressionsConverter = new LiteralExpressionsConverter(cdContext.getContext());
    this.typeConverter = new TypeConverter(cdContext);
  }

  public List<BoolExpr> ocl2smt(ASTOCLArtifact astoclArtifact) {
    List<BoolExpr> constraints = new ArrayList<>();
    for (ASTOCLConstraint constraint : astoclArtifact.getOCLConstraintList()) {
      constraints.add(convertConstr(constraint));
    }
    return constraints;
  }

  protected BoolExpr convertConstr(ASTOCLConstraint constraint) {
    if (constraint instanceof ASTOCLInvariant) {
      return convertInv((ASTOCLInvariant) constraint);
    } else {
      assert false;
      Log.error("the conversion of  ASTOCLConstraint of type   ASTOCLMethodSignature " + "and ASTOCLConstructorSignature in SMT is not implemented");
      return null;
    }
  }

  protected BoolExpr convertInv(ASTOCLInvariant invariant) {
    return (BoolExpr) convertExpr(invariant.getExpression());
  }

  protected Optional<BoolExpr> convertBoolExprOpt(ASTExpression node) {
    BoolExpr result = null;
    if (node instanceof ASTBooleanAndOpExpression) {
      result = convertAndBool((ASTBooleanAndOpExpression) node);
    } else if (node instanceof ASTBooleanOrOpExpression) {
      result = convertORBool((ASTBooleanOrOpExpression) node);
    } else if (node instanceof ASTBooleanNotExpression) {
      result = convertNotBool((ASTBooleanNotExpression) node);
    } else if (node instanceof ASTLogicalNotExpression) {
      result = convertNotBool((ASTLogicalNotExpression) node);
    } else if (node instanceof ASTLessEqualExpression) {
      result = convertLEq((ASTLessEqualExpression) node);
    } else if (node instanceof ASTLessThanExpression) {
      result = convertLThan((ASTLessThanExpression) node);
    } else if (node instanceof ASTEqualsExpression) {
      result = convertEq((ASTEqualsExpression) node);
    } else if (node instanceof ASTNotEqualsExpression) {
      result = convertNEq((ASTNotEqualsExpression) node);
    } else if (node instanceof ASTGreaterEqualExpression) {
      result = convertGEq((ASTGreaterEqualExpression) node);
    } else if (node instanceof ASTGreaterThanExpression) {
      result = convertGT((ASTGreaterThanExpression) node);
    } else if (node instanceof ASTForallExpression) {
      result = convertForAll((ASTForallExpression) node);
    } else {
      Optional<Expr<? extends Sort>> buf = convertGenExprOpt(node);
      if (buf.isPresent() && buf.get() instanceof BoolExpr) {
        result = (BoolExpr) buf.get();
      } else {
        return Optional.empty();
      }
    }

    return Optional.of(result);
  }

  protected BoolExpr convertBoolExpr(ASTExpression node) {
    Optional<BoolExpr> result = convertBoolExprOpt(node);
    if (!result.isPresent()) {
      Log.error("the conversion of expressions with the type " + node.getClass().getName() + "is   not totally  implemented");
      System.out.println("the conversion of expressions with the type " + node.getClass().getName() + "is   not totally  implemented");
      assert false;
    }
    return result.get();
  }

  protected Optional<ArithExpr<? extends ArithSort>> convertExprArithOpt(ASTExpression node) {
    ArithExpr<? extends ArithSort> result = null;
    if (node instanceof ASTMinusPrefixExpression) {
      result = convertMinPref((ASTMinusPrefixExpression) node);
    } else if (node instanceof ASTPlusPrefixExpression) {
      result = convertPlusPref((ASTPlusPrefixExpression) node);
    } else if (node instanceof ASTPlusExpression) {
      result = convertPlus((ASTPlusExpression) node);
    } else if (node instanceof ASTMinusExpression) {
      result = convertMinus((ASTMinusExpression) node);
    } else if (node instanceof ASTDivideExpression) {
      result = convertDiv((ASTDivideExpression) node);
    } else if (node instanceof ASTMultExpression) {
      result = convertMul((ASTMultExpression) node);
    } else if (node instanceof ASTModuloExpression) {
      result = convertMod((ASTModuloExpression) node);
    } else {
      Optional<Expr<? extends Sort>> buf = convertGenExprOpt(node);
      if (buf.isPresent() && buf.get() instanceof ArithExpr) {
        result = (ArithExpr<? extends ArithSort>) buf.get();
      } else {
        return Optional.empty();
      }
    }
    return Optional.of(result);
  }

  protected ArithExpr<? extends ArithSort> convertExprArith(ASTExpression node) {
    Optional<ArithExpr<? extends ArithSort>> result = convertExprArithOpt(node);
    if (!result.isPresent()) {
      Log.error("the conversion of expressions with the type " + node.getClass().getName() + "is   not totally  implemented");
      assert false;
    }
    return result.get();
  }

  protected Optional<Expr<? extends Sort>> convertGenExprOpt(ASTExpression node) {
    Expr<? extends Sort> res;
    if (node instanceof ASTLiteralExpression) {
      res = literalExpressionsConverter.convert((ASTLiteralExpression) node);
    } else if (node instanceof ASTBracketExpression) {
      res = convertBracket((ASTBracketExpression) node);
    } else if (node instanceof ASTNameExpression) {
      res = convertName((ASTNameExpression) node);
    } else {
      return Optional.empty();
    }
    return Optional.of(res);
  }

  protected Expr<? extends Sort> convertExpr(ASTExpression node) {
    Expr<? extends Sort> res;
    Log.warn("I have got a " + node.getClass().getName());
    res = convertGenExprOpt(node).orElse(null);
    if (res == null) {
      res = convertBoolExprOpt(node).orElse(null);
      if (res == null) {
        res = convertExprArith(node);
      }
    }
    return res;
  }

  //--------------------------------------Arithmetic -----------------------------------------------
  protected ArithExpr<? extends ArithSort> convertMinPref(ASTMinusPrefixExpression node) {
    return cdcontext.getContext().mkMul(cdcontext.getContext().mkInt(-1), convertExprArith(node.getExpression()));
  }

  protected ArithExpr<? extends ArithSort> convertPlusPref(ASTPlusPrefixExpression node) {
    return convertExprArith(node.getExpression());
  }

  protected ArithExpr<? extends ArithSort> convertMul(ASTMultExpression node) {
    return cdcontext.getContext().mkMul(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected ArithExpr<? extends ArithSort> convertDiv(ASTDivideExpression node) {
    return cdcontext.getContext().mkDiv(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected IntExpr convertMod(ASTModuloExpression node) {
    return cdcontext.getContext().mkMod((IntExpr) convertExprArith(node.getLeft()), (IntExpr) convertExprArith(node.getRight()));
  }

  protected ArithExpr<? extends ArithSort> convertPlus(ASTPlusExpression node) {
    return cdcontext.getContext().mkAdd(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected ArithExpr<ArithSort> convertMinus(ASTMinusExpression node) {
    return cdcontext.getContext().mkSub(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }
//---------------------------------------Logic---------------------------------

  protected BoolExpr convertNotBool(ASTBooleanNotExpression node) {
    return cdcontext.getContext().mkNot(convertBoolExpr(node.getExpression()));
  }

  protected BoolExpr convertNotBool(ASTLogicalNotExpression node) {
    return cdcontext.getContext().mkNot(convertBoolExpr(node.getExpression()));
  }

  protected BoolExpr convertAndBool(ASTBooleanAndOpExpression node) {
    return cdcontext.getContext().mkAnd(convertBoolExpr(node.getLeft()), convertBoolExpr(node.getRight()));
  }

  protected BoolExpr convertORBool(ASTBooleanOrOpExpression node) {
    return cdcontext.getContext().mkOr(convertBoolExpr(node.getLeft()), convertBoolExpr(node.getRight()));
  }

  //--------------------------comparison----------------------------------------------
  protected BoolExpr convertLThan(ASTLessThanExpression node) {
    return cdcontext.getContext().mkLt(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convertLEq(ASTLessEqualExpression node) {
    return cdcontext.getContext().mkLe(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convertGT(ASTGreaterThanExpression node) {
    return cdcontext.getContext().mkGt(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convertGEq(ASTGreaterEqualExpression node) {
    return cdcontext.getContext().mkGe(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convertEq(ASTEqualsExpression node) {
    return cdcontext.getContext().mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight()));
  }

  protected BoolExpr convertNEq(ASTNotEqualsExpression node) {
    return cdcontext.getContext().mkNot(cdcontext.getContext().mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight())));
  }

  protected BoolExpr convertForAll(ASTForallExpression node) {
    List<String> names = new ArrayList<>();

    // Declare Variables from "scope"
    for (ASTInDeclaration decl : node.getInDeclarationList()) {
      //get the type
      Sort mysort = typeConverter.convertType(decl.getMCType());
      for (ASTInDeclarationVariable var : decl.getInDeclarationVariableList()) {
        String name = var.getName();
        Expr<? extends Sort> expr = cdcontext.getContext().mkConst(name, mysort);
        varNames.put(name, expr);
        names.add(name);
      }
    }
    BoolExpr result = cdcontext.getContext().mkForall(names.stream().map(varNames::get).toArray(Expr[]::new), convertBoolExpr(node.getExpression()), 0, null, null, null, null);

    // Delete Variables from "scope"
    names.forEach(varNames::remove);

    return result;
  }

  protected Expr<? extends Sort> convertName(ASTNameExpression node) {
    assert varNames.containsKey(node.getName());
    return varNames.get(node.getName());
  }

  protected Expr<? extends Sort> convertBracket(ASTBracketExpression node) {
    return convertExpr(node.getExpression());
  }


}
