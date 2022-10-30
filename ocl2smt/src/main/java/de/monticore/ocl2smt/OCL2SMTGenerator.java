package de.monticore.ocl2smt;

import com.microsoft.z3.*;


import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;


import de.monticore.cd2smt.Helper.SMTNameHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.oclexpressions._ast.*;

import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Log;


import java.util.*;

public class OCL2SMTGenerator {
  protected final Context ctx;
  public final CD2SMTGenerator cd2smtGenerator;

  protected final LiteralExpressionsConverter literalExpressionsConverter;
  protected final TypeConverter typeConverter;

  protected final Map<String, Expr<? extends Sort>> varNames = new HashMap<>();


  public OCL2SMTGenerator(ASTCDCompilationUnit astcdCompilationUnit) {
    cd2smtGenerator = new CD2SMTGenerator();
    cd2smtGenerator.cd2smt(astcdCompilationUnit,buildContext());
    this.ctx = cd2smtGenerator.getContext();
    this.literalExpressionsConverter = new LiteralExpressionsConverter(ctx);
    this.typeConverter = new TypeConverter(cd2smtGenerator);

  }

  public List<IdentifiableBoolExpr> ocl2smt(ASTOCLArtifact astoclArtifact) {
    List<IdentifiableBoolExpr> constraints = new ArrayList<>();
    for (ASTOCLConstraint constraint : astoclArtifact.getOCLConstraintList()) {
      constraints.add(convertConstr(constraint));
    }
    return constraints;
  }

  protected IdentifiableBoolExpr convertConstr(ASTOCLConstraint constraint) {
    if (constraint instanceof ASTOCLInvariant) {
      return convertInv((ASTOCLInvariant) constraint);
    } else {
      assert false;
      Log.error("the conversion of  ASTOCLConstraint of type   ASTOCLMethodSignature " + "and ASTOCLConstructorSignature in SMT is not implemented");
      return null;
    }
  }

  protected IdentifiableBoolExpr convertInv(ASTOCLInvariant invariant) {
    List<Expr<? extends  Sort>> expr  = new ArrayList<>();
    SourcePosition srcPos = invariant.get_SourcePositionStart();
    assert srcPos.getFileName().isPresent();
    //convert parameter declaration  in context
     invariant.getOCLContextDefinitionList().forEach( node -> {
       if (node.isPresentOCLParamDeclaration()){
         expr.add(convertParDec(node.getOCLParamDeclaration()));
       }
     });
    //check if parameter was declared
    BoolExpr inv;
    if (expr.size() >0){
    inv = ctx.mkForall(expr.toArray(new Expr[0]),(BoolExpr)convertExpr(invariant.getExpression()),
              0, null, null, null, null);
    }
    else {
       inv = convertBoolExpr(invariant.getExpression());
    }
    Optional<String> name = invariant.isPresentName() ? Optional.ofNullable(invariant.getName()): Optional.empty();
    return IdentifiableBoolExpr.buildIdentifiable(inv,srcPos,name);
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
    if (result.isEmpty()) {
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
    if (result.isEmpty()) {
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
    } else if (node instanceof ASTImpliesExpression) {
      res = convertImpl((ASTImpliesExpression) node);
    } else {
      return Optional.empty();
    }
    return Optional.of(res);
  }

  protected Expr<? extends Sort> convertExpr(ASTExpression node) {
    Expr<? extends Sort> res;
    Log.info("I have got a " + node.getClass().getName(), this.getClass().getName());
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
    return ctx.mkMul(ctx.mkInt(-1), convertExprArith(node.getExpression()));
  }

  protected ArithExpr<? extends ArithSort> convertPlusPref(ASTPlusPrefixExpression node) {
    return convertExprArith(node.getExpression());
  }

  protected ArithExpr<? extends ArithSort> convertMul(ASTMultExpression node) {
    return ctx.mkMul(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected ArithExpr<? extends ArithSort> convertDiv(ASTDivideExpression node) {
    return ctx.mkDiv(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected IntExpr convertMod(ASTModuloExpression node) {
    return ctx.mkMod((IntExpr) convertExprArith(node.getLeft()), (IntExpr) convertExprArith(node.getRight()));
  }

  protected ArithExpr<? extends ArithSort> convertPlus(ASTPlusExpression node) {
    return ctx.mkAdd(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected ArithExpr<ArithSort> convertMinus(ASTMinusExpression node) {
    return ctx.mkSub(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }
//---------------------------------------Logic---------------------------------

  protected BoolExpr convertNotBool(ASTBooleanNotExpression node) {
    return ctx.mkNot(convertBoolExpr(node.getExpression()));
  }

  protected BoolExpr convertNotBool(ASTLogicalNotExpression node) {
    return ctx.mkNot(convertBoolExpr(node.getExpression()));
  }

  protected BoolExpr convertAndBool(ASTBooleanAndOpExpression node) {
    return ctx.mkAnd(convertBoolExpr(node.getLeft()), convertBoolExpr(node.getRight()));
  }

  protected BoolExpr convertORBool(ASTBooleanOrOpExpression node) {
    return ctx.mkOr(convertBoolExpr(node.getLeft()), convertBoolExpr(node.getRight()));
  }

  //--------------------------comparison----------------------------------------------
  protected BoolExpr convertLThan(ASTLessThanExpression node) {
    return ctx.mkLt(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convertLEq(ASTLessEqualExpression node) {
    return ctx.mkLe(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convertGT(ASTGreaterThanExpression node) {
    return ctx.mkGt(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convertGEq(ASTGreaterEqualExpression node) {
    return ctx.mkGe(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected BoolExpr convertEq(ASTEqualsExpression node) {
    return ctx.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight()));
  }

  protected BoolExpr convertNEq(ASTNotEqualsExpression node) {
    return ctx.mkNot(ctx.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight())));
  }

  /*------------------------------------quantified expressions----------------------------------------------------------*/
 Map<  Expr<? extends  Sort>,Optional<ASTExpression> > openScope(List<ASTInDeclaration> inDeclarations) {
    Map<Expr<? extends Sort>, Optional<ASTExpression>> variableList = new HashMap<>();
    for (ASTInDeclaration decl : inDeclarations) {
      List<Expr<? extends  Sort>> temp = convertInDecl(decl);
      if (decl.isPresentExpression()){
        temp.forEach(t-> variableList.put(t, Optional.of( decl.getExpression())));
      }else {
        temp.forEach(t-> variableList.put(t, Optional.empty()));
      }
    }
    return    variableList;
  }

  protected BoolExpr  convertInDeclConstraints(Map<Expr<? extends Sort>, Optional<ASTExpression>> var){
    //get all the InPart in InDeclarations
    Map<Expr<? extends Sort>, ASTExpression > inParts = new HashMap<>();
    var.forEach((key, value) -> value.ifPresent(s -> inParts.put(key, s)));


    List<BoolExpr> constraintList = new ArrayList<>();

    for (Map.Entry<Expr<? extends Sort>, ASTExpression > expr: inParts.entrySet()){
      if (!(expr.getValue() instanceof  ASTFieldAccessExpression)){
        Log.error("cannot convert ASTInDeclaration, in part is not a ASTFieldAccessExpression");
      }
      assert expr.getValue() instanceof ASTFieldAccessExpression;
      Association association = convertFieldAccAssoc( (ASTFieldAccessExpression) expr.getValue());
      constraintList.add(association.evaluate(expr.getKey()));
    }
    BoolExpr result = ctx.mkTrue() ;

    for (BoolExpr constr: constraintList){
      result = ctx.mkAnd(result,constr);
    }
    return result;
  }

  protected BoolExpr convertForAll(ASTForallExpression node) {
    //declare Variable from scope
    Map<Expr<? extends Sort>, Optional<ASTExpression>> var = openScope(node.getInDeclarationList());

    BoolExpr constraint = convertInDeclConstraints(var);

    BoolExpr  result = ctx.mkForall(var.keySet().toArray(new Expr[0]), ctx.mkImplies(constraint , convertBoolExpr(node.getExpression())),
              1, null, null, null, null);

    // Delete Variables from "scope"
    closeScope(node.getInDeclarationList());

    return result;
  }

  protected BoolExpr convertExist(ASTExistsExpression node) {
    //declare Variable from scope
    Map<Expr<? extends Sort>, Optional<ASTExpression>> var = openScope(node.getInDeclarationList());

    BoolExpr constraint = convertInDeclConstraints(var);

    BoolExpr  result = ctx.mkExists(var.keySet().toArray(new Expr[0]), ctx.mkAnd(constraint , convertBoolExpr(node.getExpression())),
            0, null, null, null, null);

    // Delete Variables from "scope"
    closeScope(node.getInDeclarationList());

    return result;
  }

  /*----------------------------------control expressions----------------------------------------------------------*/
  protected Expr<? extends Sort> convertIfTEl(ASTIfThenElseExpression node) {
    return ctx.mkITE(convertBoolExpr(node.getCondition()), convertExpr(node.getThenExpression()),
            convertExpr(node.getElseExpression()));
  }
  protected Expr<? extends Sort> convertImpl(ASTImpliesExpression node) {
    return ctx.mkImplies(convertBoolExpr(node.getLeft()),convertBoolExpr(node.getRight()));
  }

  //-----------------------------------general----------------------------------------------------------------------*/
  protected Expr<? extends Sort> convertName(ASTNameExpression node) {
    assert varNames.containsKey(node.getName());
    return varNames.get(node.getName());
  }

  protected Expr<? extends Sort> convertBracket(ASTBracketExpression node) {
    return convertExpr(node.getExpression());
  }
  protected Expr<? extends Sort>  convertFieldAcc(ASTFieldAccessExpression node) {
    Expr<? extends Sort> obj = convertExpr(node.getExpression());
    ASTCDType myType =CDHelper.getASTCDType(SMTNameHelper.sort2CDTypeName(obj.getSort()),cd2smtGenerator.getClassDiagram().getCDDefinition());
    ASTCDAttribute myAttribute = CDHelper.getAttribute(myType,node.getName());
    return cd2smtGenerator.getAttribute(myType,myAttribute,obj);
  }

  protected  Association  convertFieldAccAssoc(ASTFieldAccessExpression node) {
    //get the object and convert it into smt expression
    Expr<? extends Sort> obj = convertExpr(node.getExpression());
    ASTCDType myType = CDHelper.getASTCDType(SMTNameHelper.sort2CDTypeName(obj.getSort()), cd2smtGenerator.getClassDiagram().getCDDefinition());
    ASTCDAssociation association = CDHelper.getAssociation(myType, node.getName(),cd2smtGenerator.getClassDiagram().getCDDefinition());
    Sort leftSort = cd2smtGenerator.getSort(CDHelper.getASTCDType(association.getLeftQualifiedName().getQName(), cd2smtGenerator.getClassDiagram().getCDDefinition()));

    return leftSort.equals(obj.getSort()) ? obj2 -> cd2smtGenerator.evaluateLink(association, obj, obj2)
            : obj2 -> cd2smtGenerator.evaluateLink(association, obj2, obj);
  }

  protected List<Expr<? extends Sort>> convertInDecVar(ASTInDeclarationVariable node, ASTMCType type) {
    List<Expr<? extends Sort>> result = new ArrayList<>();

    Expr<? extends Sort> expr = ctx.mkConst(node.getName(), typeConverter.convertType(type,cd2smtGenerator.getClassDiagram().getCDDefinition()));
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



  protected Expr<? extends  Sort> convertParDec(ASTOCLParamDeclaration node){
    ASTMCType type = node.getMCType();
    Expr<? extends Sort> expr = ctx.mkConst(node.getName(), typeConverter.convertType(type,cd2smtGenerator.getClassDiagram().getCDDefinition()));
    varNames.put(node.getName(), expr);
    return  expr;
  }

  protected List <Expr<? extends Sort>> convertInDecl( ASTInDeclaration node){
    List<Expr<? extends Sort>> result = new ArrayList<>();
      for (ASTInDeclarationVariable var : node.getInDeclarationVariableList()) {
         if (node.isPresentMCType()){
           result.addAll(convertInDecVar(var,node.getMCType()));
         }else {
           Log.error("ASTInDeclExpression Without  Type not yet Supported");
         }
      }
      return result;
  }


@FunctionalInterface
 public  interface Association {
    BoolExpr evaluate ( Expr<? extends Sort> right );
 }

  public  Context buildContext() {
    Map<String, String> cfg = new HashMap<>();
    cfg.put("model", "true");
    return new Context(cfg);
  }

}
