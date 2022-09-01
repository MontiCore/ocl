package de.monticore.ocl2smt;

import com.microsoft.z3.*;
import com.sun.tools.javac.util.Pair;
import de.monticore.cd2smt.context.CDContext;
import de.monticore.cd2smt.context.SMTClass;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.ocl._ast.ASTOCLArtifact;
import de.monticore.ocl.ocl._ast.ASTOCLConstraint;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
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

  public List<Pair<Optional<String>,BoolExpr>> ocl2smt(ASTOCLArtifact astoclArtifact) {
    List<Pair<Optional<String>,BoolExpr>> constraints = new ArrayList<>();
    for (ASTOCLConstraint constraint : astoclArtifact.getOCLConstraintList()) {
      constraints.add(convertConstr(constraint));
    }
    return constraints;
  }

  protected Pair<Optional<String>,BoolExpr> convertConstr(ASTOCLConstraint constraint) {
    if (constraint instanceof ASTOCLInvariant) {
      return convertInv((ASTOCLInvariant) constraint);
    } else {
      assert false;
      Log.error("the conversion of  ASTOCLConstraint of type   ASTOCLMethodSignature " + "and ASTOCLConstructorSignature in SMT is not implemented");
      return null;
    }
  }

  protected Pair<Optional<String>,BoolExpr> convertInv(ASTOCLInvariant invariant) {
    if (invariant.isPresentName()){
      return new Pair(Optional.of(invariant.getName()),convertExpr(invariant.getExpression()));
    }
    return new Pair(Optional.empty(), convertExpr(invariant.getExpression()));
  }

  protected Optional<BoolExpr> convertBoolExprOpt(ASTExpression node) {
    BoolExpr result;
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
    } else if (node instanceof ASTExistsExpression) {
      result = convertExist((ASTExistsExpression) node);
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
      assert false;
    }
    return result.get();
  }

  protected Optional<ArithExpr<? extends ArithSort>> convertExprArithOpt(ASTExpression node) {
    ArithExpr<? extends ArithSort> result;
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
    } else if (node instanceof ASTFieldAccessExpression) {
      res = convertFieldAcc((ASTFieldAccessExpression) node);
    } else if (node instanceof ASTIfThenElseExpression) {
      res = convertIfTEl((ASTIfThenElseExpression) node);
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

  /*------------------------------------quantified expressions----------------------------------------------------------*/
  Expr<? extends Sort>[] openScope(List<ASTInDeclaration> inDeclarations) {
    List<Expr<? extends Sort>> result = new ArrayList<>();
    for (ASTInDeclaration decl : inDeclarations) {
      for (ASTInDeclarationVariable var : decl.getInDeclarationVariableList()) {
        result.addAll(convertInDecVar(var, decl.getMCType()));
      }
    }
    return (Expr<? extends Sort>[]) result.toArray(new Expr[0]);
  }

  protected BoolExpr convertForAll(ASTForallExpression node) {
    //declare Variable from scope
    Expr<? extends Sort>[] exprs = openScope(node.getInDeclarationList());

    BoolExpr result = cdcontext.getContext().mkForall(exprs, convertBoolExpr(node.getExpression()),
            0, null, null, null, null);

    // Delete Variables from "scope"
    closeScope(node.getInDeclarationList());
    return result;
  }

  protected BoolExpr convertExist(ASTExistsExpression node) {
    //declare variables from scope
    Expr<? extends Sort>[] expr = openScope(node.getInDeclarationList());

    BoolExpr result = cdcontext.getContext().mkExists(expr,
            convertBoolExpr(node.getExpression()), 0, null, null, null, null);

    // Delete Variables from "scope"
    closeScope(node.getInDeclarationList());
    return result;
  }

  /*----------------------------------control expressions----------------------------------------------------------*/
  protected Expr<? extends Sort> convertIfTEl(ASTIfThenElseExpression node) {
    return cdcontext.getContext().mkITE(convertBoolExpr(node.getCondition()), convertExpr(node.getThenExpression()),
            convertExpr(node.getElseExpression()));
  }

  //-----------------------------------general----------------------------------------------------------------------*/
  protected Expr<? extends Sort> convertName(ASTNameExpression node) {
    assert varNames.containsKey(node.getName());
    return varNames.get(node.getName());
  }

  protected Expr<? extends Sort> convertBracket(ASTBracketExpression node) {
    return convertExpr(node.getExpression());
  }

  protected Expr<? extends Sort> convertFieldAcc(ASTFieldAccessExpression node) {
    Expr<? extends Sort> obj = convertExpr(node.getExpression());
    Optional<SMTClass> smtClassOptional = cdcontext.getSMTClass(obj);
    assert smtClassOptional.isPresent();
    return cdcontext.getContext().mkApp(cdcontext.getAttributeFunc(smtClassOptional.get(), node.getName()), obj);
  }


  protected List<Expr<? extends Sort>> convertInDecVar(ASTInDeclarationVariable node, ASTMCType type) {
    List<Expr<? extends Sort>> result = new ArrayList<>();

    Expr<? extends Sort> expr = cdcontext.getContext().mkConst(node.getName(), typeConverter.convertType(type));
    varNames.put(node.getName(), expr);
    result.add(expr);

    return result;
  }

  protected void closeScope(List<ASTInDeclaration> inDeclarations) {
    for (ASTInDeclaration decl : inDeclarations) {
      for (ASTInDeclarationVariable var : decl.getInDeclarationVariableList()) {
        assert varNames.containsKey(var.getName());
        varNames.remove(var.getName());
      }
    }
  }

}
