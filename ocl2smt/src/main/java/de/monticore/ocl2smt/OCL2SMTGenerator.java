package de.monticore.ocl2smt;

import com.microsoft.z3.*;
import com.sun.tools.javac.util.Pair;
import de.monticore.cd2smt.context.CDContext;
import de.monticore.cd2smt.context.SMTAssociation;
import de.monticore.cd2smt.context.SMTClass;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.ocl._ast.*;
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
    List<Expr<? extends  Sort>> expr  = new ArrayList<>();
    //convert parameter declaredin context
    for (ASTOCLContextDefinition node : invariant.getOCLContextDefinitionList()){
      if (node.isPresentOCLParamDeclaration()){
        expr.add(convertParDec(node.getOCLParamDeclaration()));
      }
    }
    BoolExpr inv;
    if (expr.size() >0){
    inv = cdcontext.getContext().mkForall(expr.toArray(new Expr[0]),(BoolExpr)convertExpr(invariant.getExpression()),
              0, null, null, null, null);
    }
    else {
       inv = (BoolExpr)convertExpr(invariant.getExpression());
    }

    if (invariant.isPresentName()){
      return new Pair(Optional.of(invariant.getName()),inv);
    }
    return new Pair(Optional.empty(), inv);
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
    } else if (node instanceof ASTImpliesExpression) {
      res = convertImpl((ASTImpliesExpression) node);
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
 Map<  Expr<? extends  Sort>,Optional<ASTExpression> > openScope(List<ASTInDeclaration> inDeclarations) {
    Map<Expr<? extends Sort>, Optional<ASTExpression>> variable = new HashMap<>();
    for (ASTInDeclaration decl : inDeclarations) {
      List<Expr<? extends  Sort>> temp = convertInDecl(decl);
      if (decl.isPresentExpression()){
        temp.forEach(t-> variable.put(t, Optional.of( decl.getExpression())));
      }else {
        temp.forEach(t-> variable.put(t, Optional.empty()));
      }
    }
    return    variable;
  }

  protected BoolExpr convertForAll(ASTForallExpression node) {
    //declare Variable from scope
    Map<Expr<? extends Sort>, Optional<ASTExpression>> var = openScope(node.getInDeclarationList());

    //get all the InPart in InDeclarations
    Map<Expr<? extends Sort>, ASTExpression > inParts = new HashMap<>();
    var.forEach((key, value) -> value.ifPresent(s -> inParts.put(key, s)));

    BoolExpr result ;
    //there are no In part (a.person) in  InDeclarations
    if (inParts.size() == 0){
       result = cdcontext.getContext().mkForall(var.keySet().toArray(new Expr[0]),  convertBoolExpr(node.getExpression()),
              0, null, null, null, null);

    }else {

      BoolExpr constraints  = cdcontext.getContext().mkTrue();
       for (Map.Entry<Expr<? extends Sort>, ASTExpression > expr: inParts.entrySet()){
         if (!(expr.getValue() instanceof  ASTFieldAccessExpression)){
           Log.error("cannot convert ASTIn Declaration, in part is not a ASTFieldAccessExpression");
         }
         assert expr.getValue() instanceof ASTFieldAccessExpression;
         Pair<Expr<? extends Sort>, BoolExpr> navigation = convertFieldAcc(expr.getValue(),expr.getKey());
         constraints = cdcontext.getContext().mkAnd(constraints,navigation.snd);
       }
       result = cdcontext.getContext().mkForall(var.keySet().toArray(new Expr[0]), cdcontext.getContext().mkImplies(constraints , convertBoolExpr(node.getExpression())),
              0, null, null, null, null);
    }

    // Delete Variables from "scope"
    closeScope(node.getInDeclarationList());
    return result;
  }

  protected BoolExpr convertExist(ASTExistsExpression node) {
    //declare Variable from scope
    Map<Expr<? extends Sort>, Optional<ASTExpression>> var = openScope(node.getInDeclarationList());

    //get all the InPart in InDeclarations
    Map<Expr<? extends Sort>, ASTExpression > inParts = new HashMap<>();
    var.forEach((key, value) -> value.ifPresent(s -> inParts.put(key, s)));

    BoolExpr result ;
    //there are no In part (a.person) in  InDeclarations
    if (inParts.size() == 0){
      result = cdcontext.getContext().mkExists(var.keySet().toArray(new Expr[0]),  convertBoolExpr(node.getExpression()),
              0, null, null, null, null);

    }else {

      BoolExpr constraints  = cdcontext.getContext().mkTrue();
      for (Map.Entry<Expr<? extends Sort>, ASTExpression > expr: inParts.entrySet()){
        if (!(expr.getValue() instanceof  ASTFieldAccessExpression)){
          Log.error("cannot convert ASTInDeclaration, in part is not a ASTFieldAccessExpression");
        }
        assert expr.getValue() instanceof ASTFieldAccessExpression;
        Pair<Expr<? extends Sort>, BoolExpr> navigation = convertFieldAcc( expr.getValue(),expr.getKey());
        constraints = cdcontext.getContext().mkAnd(constraints,navigation.snd);
      }
      result = cdcontext.getContext().mkExists(var.keySet().toArray(new Expr[0]), cdcontext.getContext().mkImplies(constraints , convertBoolExpr(node.getExpression())),
              0, null, null, null, null);
    }

    // Delete Variables from "scope"
    closeScope(node.getInDeclarationList());
    return result;
  }

  /*----------------------------------control expressions----------------------------------------------------------*/
  protected Expr<? extends Sort> convertIfTEl(ASTIfThenElseExpression node) {
    return cdcontext.getContext().mkITE(convertBoolExpr(node.getCondition()), convertExpr(node.getThenExpression()),
            convertExpr(node.getElseExpression()));
  }
  protected Expr<? extends Sort> convertImpl(ASTImpliesExpression node) {
    return cdcontext.getContext().mkImplies(convertBoolExpr(node.getLeft()),convertBoolExpr(node.getRight()));
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

  protected Pair<Expr<? extends Sort>, BoolExpr>  convertFieldAcc(ASTExpression node, Expr<? extends  Sort> obj) {

    if ( !(node instanceof ASTFieldAccessExpression)){
      return new Pair<>(convertExpr(node),cdcontext.getContext().mkTrue());
    }
      ASTFieldAccessExpression fieldAccessExpr = (ASTFieldAccessExpression)node;
      Pair<Expr<? extends Sort>, BoolExpr> res = convertFieldAcc(fieldAccessExpr.getExpression(),obj);

      //test if it is an Attribute navigation
      Optional<SMTClass> smtClassOptional = cdcontext.getSMTClass(res.fst);
      assert smtClassOptional.isPresent();
      if (cdcontext.containsAttribute(smtClassOptional.get(),fieldAccessExpr.getName())){
        FuncDecl<? extends  Sort> func = cdcontext.getAttributeFunc(smtClassOptional.get(), fieldAccessExpr.getName());
        return new Pair<>(cdcontext.getContext().mkApp(func,res.fst), res.snd);
      }
      //case Association navigation
      else {
        SMTAssociation smtAssociation = cdcontext.getAssocFunc(smtClassOptional.get(),fieldAccessExpr.getName());
        BoolExpr constraint = cdcontext.getContext().mkTrue() ;


        if (smtAssociation.getAssocFunc().getDomain()[0].equals(res.fst.getSort())){
              constraint = cdcontext.getContext().mkAnd(constraint,
                      cdcontext.getContext().mkApp(smtAssociation.getAssocFunc(), res.fst, obj));
        }
        else {
              constraint = cdcontext.getContext().mkAnd(constraint,
                      cdcontext.getContext().mkApp(smtAssociation.getAssocFunc(), res.fst, obj));
          }

     return new Pair<>(obj,cdcontext.getContext().mkAnd(res.snd, constraint));
      }



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



  protected Expr<? extends  Sort> convertParDec(ASTOCLParamDeclaration node){
    ASTMCType type = node.getMCType();
    Expr<? extends Sort> expr = cdcontext.getContext().mkConst(node.getName(), typeConverter.convertType(type));
    varNames.put(node.getName(), expr);
    return  expr;
  }

  protected List <Expr<? extends Sort>> convertInDecl( ASTInDeclaration node){
    List<Expr<? extends Sort>> result = new ArrayList<>();
      for (ASTInDeclarationVariable var : node.getInDeclarationVariableList()) {
         if (node.isPresentMCType()){
           result.addAll(convertInDecVar(var,node.getMCType()));
         }else {
           result.addAll(convertInDecVar(var,getType((ASTFieldAccessExpression) node.getExpression())));
         }
      }
      return result;
  }

  protected ASTMCType getType(ASTFieldAccessExpression node){
    Expr<? extends Sort> obj = convertExpr(node.getExpression());
    Optional<SMTClass> smtClassOptional = cdcontext.getSMTClass(obj);
  //TODO: implement the function
    assert smtClassOptional.isPresent();
    assert  false  ;
    Log.error("the function getType was not implemented yet");
    return  null ;
  }



}
