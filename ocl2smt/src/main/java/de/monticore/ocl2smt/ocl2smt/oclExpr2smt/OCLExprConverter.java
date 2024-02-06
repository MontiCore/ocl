package de.monticore.ocl2smt.ocl2smt.oclExpr2smt;

import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTArguments;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.literals.mccommonliterals._ast.*;
import de.monticore.literals.mcliteralsbasis._ast.ASTLiteral;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory.CDExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprFactory.ExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeFactorry.TypeFactory;
import de.monticore.ocl2smt.visitors.SetGeneratorCollector;
import de.monticore.ocl2smt.visitors.SetVariableCollector;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

// todo  cd2SMTGenerator = CD2SMTMill.cd2SMTGenerator(); place that on the right position
// todo cd2SMTGenerator.cd2smt(cdAst, ctx);
/** This class convert All OCL-Expressions except @Pre-Expressions in SMT */
public class OCLExprConverter<EXPR extends ExprAdapter<?, TYPE>, TYPE> {
  protected Map<String, EXPR> varNames;
  protected ExprFactory<EXPR, TYPE> eFactory;
  protected CDExprFactory<EXPR> cdFactory;
  protected TypeFactory<TYPE> tFactory;

  public OCLExprConverter(
      ExprFactory<EXPR, TYPE> factory,
      CDExprFactory<EXPR> cdFactory,
      TypeFactory<TYPE> typeFactory) {

    this.eFactory = factory;
    this.tFactory = typeFactory;
    this.cdFactory = cdFactory;

    varNames = new HashMap<>();
  }

  public EXPR convertExpr(ASTExpression node) {
    EXPR result = null;

    if (node instanceof ASTLiteralExpression) {
      result = convert(((ASTLiteralExpression) node).getLiteral());
    } else if (node instanceof ASTBooleanAndOpExpression) {
      EXPR left = convertExpr(((ASTBooleanAndOpExpression) node).getLeft());
      EXPR right = convertExpr(((ASTBooleanAndOpExpression) node).getRight());
      result = eFactory.mkAnd(left, right);
    } else if (node instanceof ASTBooleanOrOpExpression) {
      EXPR left = convertExpr(((ASTBooleanOrOpExpression) node).getLeft());
      EXPR right = convertExpr(((ASTBooleanOrOpExpression) node).getRight());
      result = eFactory.mkOr(left, right);
    } else if (node instanceof ASTBooleanNotExpression) {
      result = eFactory.mkNot(convertExpr(node));
    } else if (node instanceof ASTLogicalNotExpression) {
      result = eFactory.mkNot(convertExpr(((ASTLogicalNotExpression) node).getExpression()));
    } else if (node instanceof ASTLessEqualExpression) {
      EXPR left = convertExpr(((ASTLessEqualExpression) node).getLeft());
      EXPR right = convertExpr(((ASTLessEqualExpression) node).getRight());
      result = eFactory.mkLeq(left, right);
    } else if (node instanceof ASTLessThanExpression) {
      EXPR left = convertExpr(((ASTLessThanExpression) node).getLeft());
      EXPR right = convertExpr(((ASTLessThanExpression) node).getRight());
      result = eFactory.mkLt(left, right);
    } else if (node instanceof ASTEqualsExpression) {
      EXPR left = convertExpr(((ASTEqualsExpression) node).getLeft());
      EXPR right = convertExpr(((ASTEqualsExpression) node).getRight());
      result = eFactory.mkEq(left, right);
    } else if (node instanceof ASTNotEqualsExpression) {
      EXPR left = convertExpr(((ASTNotEqualsExpression) node).getLeft());
      EXPR right = convertExpr(((ASTNotEqualsExpression) node).getRight());
      result = eFactory.mkNeq(left, right);
    } else if (node instanceof ASTGreaterEqualExpression) {
      EXPR left = convertExpr(((ASTGreaterEqualExpression) node).getLeft());
      EXPR right = convertExpr(((ASTGreaterEqualExpression) node).getRight());
      result = eFactory.mkGe(left, right);
    } else if (node instanceof ASTGreaterThanExpression) {
      EXPR left = convertExpr(((ASTGreaterThanExpression) node).getLeft());
      EXPR right = convertExpr(((ASTGreaterThanExpression) node).getRight());
      result = eFactory.mkGt(left, right);
    } else if (node instanceof ASTImpliesExpression) {
      EXPR left = convertExpr(((ASTImpliesExpression) node).getLeft());
      EXPR right = convertExpr(((ASTImpliesExpression) node).getRight());
      result = eFactory.mkImplies(left, right);
    } else if (node instanceof ASTCallExpression) {
      result = convertMethodCall((ASTCallExpression) node);
    } else if (node instanceof ASTEquivalentExpression) {
      EXPR left = convertExpr(((ASTEquivalentExpression) node).getLeft());
      EXPR right = convertExpr(((ASTEquivalentExpression) node).getRight());
      result = eFactory.mkEq(left, right);
    } else if (node instanceof ASTMinusPrefixExpression) {
      EXPR subExpr = convertExpr(((ASTMinusPrefixExpression) node).getExpression());
      result = eFactory.mkMinusPrefix(subExpr);
    } else if (node instanceof ASTPlusPrefixExpression) {
      EXPR subExpr = convertExpr(((ASTPlusPrefixExpression) node).getExpression());
      result = eFactory.mkPlusPrefix(subExpr);
    } else if ((node instanceof ASTPlusExpression)) {
      EXPR left = convertExpr(((ASTPlusExpression) node).getLeft());
      EXPR right = convertExpr(((ASTPlusExpression) node).getRight());
      result = eFactory.mkPlus(left, right);
    } else if (node instanceof ASTMinusExpression) {
      EXPR left = convertExpr(((ASTMinusExpression) node).getLeft());
      EXPR right = convertExpr(((ASTMinusExpression) node).getRight());
      result = eFactory.mkSub(left, right);
    } else if (node instanceof ASTDivideExpression) {
      EXPR left = convertExpr(((ASTDivideExpression) node).getLeft());
      EXPR right = convertExpr(((ASTDivideExpression) node).getRight());
      result = eFactory.mkDiv(left, right);
    } else if (node instanceof ASTMultExpression) {
      EXPR left = convertExpr(((ASTMultExpression) node).getLeft());
      EXPR right = convertExpr(((ASTMultExpression) node).getRight());
      result = eFactory.mkMul(left, right);
    } else if (node instanceof ASTModuloExpression) {
      EXPR left = convertExpr(((ASTModuloExpression) node).getLeft());
      EXPR right = convertExpr(((ASTModuloExpression) node).getRight());
      result = eFactory.mkMod(left, right);
    } else if (node instanceof ASTBracketExpression) {
      result = convertExpr(((ASTBracketExpression) node).getExpression());
    } else if (node instanceof ASTNameExpression) {
      result = convert((ASTNameExpression) node);
    } else if (node instanceof ASTFieldAccessExpression) {
      result = convert((ASTFieldAccessExpression) node);
    } else if (node instanceof ASTIfThenElseExpression) {
      EXPR cond = convertExpr(((ASTIfThenElseExpression) node).getCondition());
      EXPR expr1 = convertExpr(((ASTIfThenElseExpression) node).getThenExpression());
      EXPR expr2 = convertExpr(((ASTIfThenElseExpression) node).getElseExpression());
      result = eFactory.mkIte(cond, expr1, expr2);
    } else if (node instanceof ASTConditionalExpression) {
      EXPR cond = convertExpr(((ASTConditionalExpression) node).getCondition());
      EXPR expr1 = convertExpr(((ASTConditionalExpression) node).getTrueExpression());
      EXPR expr2 = convertExpr(((ASTConditionalExpression) node).getFalseExpression());
      result = eFactory.mkIte(cond, expr1, expr2);
    } else if (node instanceof ASTForallExpression) {
      result = convertForAll((ASTForallExpression) node);
    } else if (node instanceof ASTExistsExpression) {
      result = convertExist((ASTExistsExpression) node);
    } else if (node instanceof ASTSetInExpression) {
      EXPR set1 = convertExpr(((ASTSetInExpression) node).getSet());
      EXPR set2 = convertExpr(((ASTSetInExpression) node).getElem());
      result = eFactory.mkContains(set1, set2);
    } else if (node instanceof ASTSetNotInExpression) {
      EXPR set1 = convertExpr(((ASTSetNotInExpression) node).getSet());
      EXPR set2 = convertExpr(((ASTSetNotInExpression) node).getElem());
      result = eFactory.mkNot(eFactory.mkContains(set1, set2));
    } else if (node instanceof ASTOCLTransitiveQualification) {
      result = convertTransClo((ASTOCLTransitiveQualification) node);
    } else if (node instanceof ASTUnionExpression) {
      EXPR set1 = convertExpr(((ASTUnionExpression) node).getLeft());
      EXPR set2 = convertExpr(((ASTUnionExpression) node).getRight());
      result = eFactory.mkSetUnion(set1, set2);
    } else if (node instanceof ASTIntersectionExpression) {
      EXPR set1 = convertExpr(((ASTIntersectionExpression) node).getLeft());
      EXPR set2 = convertExpr(((ASTIntersectionExpression) node).getRight());
      result = eFactory.mkSetIntersect(set1, set2);
    } else if (node instanceof ASTSetMinusExpression) {
      EXPR set1 = convertExpr(((ASTSetMinusExpression) node).getLeft());
      EXPR set2 = convertExpr(((ASTSetMinusExpression) node).getRight());
      result = eFactory.mkSetMinus(set1, set2);
    } else if (node instanceof ASTSetComprehension) {
      result = convertSetComp((ASTSetComprehension) node);
    } else if (node instanceof ASTSetEnumeration) {
      result = convertSetEnum((ASTSetEnumeration) node);
    } else {
      Log.error("conversion of Set of the type " + node.getClass().getName() + " not implemented");
    }
    return result;
  }

  private EXPR convert(ASTLiteral node) {
    EXPR result = null;
    if (node instanceof ASTBooleanLiteral) {
      result = eFactory.mkBool(((ASTBooleanLiteral) node).getValue());
    } else if (node instanceof ASTStringLiteral) {
      result = eFactory.mkString(((ASTStringLiteral) node).getValue());
    } else if (node instanceof ASTNatLiteral) {
      result = eFactory.mkInt(((ASTNatLiteral) node).getValue());
    } else if (node instanceof ASTBasicDoubleLiteral) {
      result = eFactory.mkDouble(((ASTBasicDoubleLiteral) node).getValue());
    } else if (node instanceof ASTCharLiteral) {
      result = eFactory.mkChar(((ASTCharLiteral) node).getValue());
    }
    return result;
  }

  protected EXPR convertMethodCall(ASTCallExpression node) {

    if (node.getExpression() instanceof ASTFieldAccessExpression) {
      ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();
      String methodName = ((ASTFieldAccessExpression) node.getExpression()).getName();
      ASTArguments args = node.getArguments();

      EXPR callerExpr = convertExpr(caller);
      switch (methodName) {
        case "before":
          return eFactory.mkLt(callerExpr, convertExpr(args.getExpression(0)));
        case "after":
          return eFactory.mkGt(callerExpr, convertExpr(args.getExpression(0)));
        case "contains":
          return eFactory.mkContains(callerExpr, convertExpr(args.getExpression(0)));
        case "endsWith":
          return eFactory.mkSuffixOf(convertExpr(args.getExpression(0)), callerExpr);
        case "startsWith":
          return eFactory.mkPrefixOf(convertExpr(args.getExpression(0)), callerExpr);
        case "replace":
          return eFactory.mkReplace(
              callerExpr, convertExpr(args.getExpression(0)), convertExpr(args.getExpression(1)));
        case "containsAll":
          return eFactory.containsAll(callerExpr, convertExpr(args.getExpression(0)));
        case "isEmpty":
          return eFactory.mkIsEmpty(callerExpr);
        case "isPresent":
          return eFactory.mkNot(eFactory.mkIsEmpty(callerExpr));
        case "get":
          return cdFactory.unWrap(callerExpr);
      }
    }
    Log.error("Conversion of ASTMethodeCall  not fully implemented");
    return null;
  }

  public EXPR mkConst(String name, TypeAdapter<TYPE> type) {
    EXPR expr = eFactory.mkConst(name, type);
    varNames.put(name, expr);
    return expr;
  }

  /*------------------------------------quantified expressions----------------------------------------------------------*/
  protected EXPR convertForAll(ASTForallExpression node) {
    Pair<List<EXPR>, EXPR> quanParams = openScope(node.getInDeclarationList());

    EXPR body = eFactory.mkImplies(quanParams.getRight(), convertExpr(node.getExpression()));
    EXPR result = cdFactory.mkForall(quanParams.getLeft(), body);

    closeScope(node.getInDeclarationList());
    return result;
  }

  protected EXPR convertExist(ASTExistsExpression node) {
    Pair<List<EXPR>, EXPR> quanParams = openScope(node.getInDeclarationList());

    EXPR body = eFactory.mkAnd(quanParams.getRight(), convertExpr(node.getExpression()));
    EXPR result = cdFactory.mkExists(quanParams.getLeft(), body);

    closeScope(node.getInDeclarationList());
    return result;
  }

  private Pair<List<EXPR>, EXPR> openScope(List<ASTInDeclaration> inDeclarations) {
    List<EXPR> paramList = new ArrayList<>();
    EXPR constraint = eFactory.mkBool(true);

    for (ASTInDeclaration decl : inDeclarations) {
      for (ASTInDeclarationVariable varDecl : decl.getInDeclarationVariableList()) {

        TypeAdapter<TYPE> type =
            decl.isPresentMCType()
                ? tFactory.adapt(decl.getMCType())
                : tFactory.adapt(varDecl.getSymbol().getType());

        EXPR var = mkConst(varDecl.getName(), type);
        paramList.add(var);
        if (decl.isPresentExpression()) {
          EXPR mySet = convertExpr(decl.getExpression());
          constraint = eFactory.mkAnd(eFactory.mkContains(mySet, var), constraint);
        }
      }
    }

    return new ImmutablePair<>(paramList, constraint);
  }

  protected void closeScope(List<ASTInDeclaration> inDeclarations) {
    for (ASTInDeclaration decl : inDeclarations) {
      for (ASTInDeclarationVariable var : decl.getInDeclarationVariableList()) {
        assert varNames.containsKey(var.getName());
        varNames.remove(var.getName());
      }
    }
  }

  protected EXPR convert(ASTNameExpression node) {
    if (varNames.containsKey(node.getName())) {
      return varNames.get(node.getName());
    } else {
      Log.error("Conversion of ASTNameExpression not fully implemented");
      /*   SymTypeExpression typeExpr = tFactory.deriveType(node);
      TypeAdapter<TYPE> type = tFactory.adapt(typeExpr);
      return mkConst(node.getName(), type);*/
      return null;
    }
  }

  protected EXPR convert(ASTFieldAccessExpression node) {
    EXPR obj = convertExpr(node.getExpression());
    return cdFactory.getLink(obj, node.getName());
  }

  protected EXPR convertSetComp(ASTSetComprehension node) {
    Set<String> setCompVarNames = openSetCompScope(node);

    Function<EXPR, EXPR> setComp = convertSetCompLeftSide(node.getLeft(), setCompVarNames);
    EXPR filter = convertSetCompRightSide(node.getSetComprehensionItemList());

    closeSetCompScope(setCompVarNames);
    return setComp.apply(filter);
  }

  /***
   * declare all the variable-declaration that are present in the set comprehension.
   * @return the set of all variable of the set comprehension scope.
   */
  private Set<String> openSetCompScope(ASTSetComprehension node) {

    // collect all variables in the set comp
    OCLTraverser traverser = OCLMill.traverser();
    SetVariableCollector varCollector = new SetVariableCollector();
    SetGeneratorCollector generatorCollector = new SetGeneratorCollector();
    traverser.add4SetExpressions(varCollector);
    traverser.add4SetExpressions(generatorCollector);
    node.accept(traverser);

    // convert set variables to Expr: int x  = 10
    for (ASTSetVariableDeclaration var : varCollector.getAllVariables()) {
      TypeAdapter<TYPE> varType = tFactory.adapt(var.getMCType());
      mkConst(var.getName(), varType);
    }

    // convert variable by generator-declaration : int x in {1,2}
    for (ASTGeneratorDeclaration gen : generatorCollector.getAllGenrators()) {
      TypeAdapter<TYPE> type;
      if (gen.isPresentMCType()) {
        type = tFactory.adapt(gen.getMCType());
      } else {
        type = tFactory.adapt(gen.getSymbol().getType());
      }
      mkConst(gen.getName(), type);
    }

    Set<String> setCompScopeVar = new HashSet<>(varCollector.getAllVariableNames());
    setCompScopeVar.addAll(generatorCollector.getAllVariableNames());

    return setCompScopeVar;
  }

  private void closeSetCompScope(Set<String> setCompVarNames) {
    for (String name : setCompVarNames) {
      varNames.remove(name);
    }
    setCompVarNames.clear();
  }

  protected EXPR convertSetEnum(ASTSetEnumeration node) {
    TypeAdapter<TYPE> type = null;
    Function<EXPR, EXPR> setFunc = obj -> eFactory.mkBool(false);

    for (ASTSetCollectionItem item : node.getSetCollectionItemList()) {

      if (item instanceof ASTSetValueItem) {
        EXPR value = convertExpr(((ASTSetValueItem) item).getExpression());
        Function<EXPR, EXPR> finalFunc = setFunc;
        setFunc = obj -> eFactory.mkOr(finalFunc.apply(obj), eFactory.mkEq(value, obj));
        type = value.getType();
      } else {
        ASTSetValueRange range = (ASTSetValueRange) item;
        EXPR expr1 = convertExpr(range.getUpperBound());
        EXPR expr2 = convertExpr(range.getLowerBound());
        EXPR low = eFactory.mkIte(eFactory.mkLt(expr1, expr2), expr1, expr2);
        EXPR up = eFactory.mkIte(eFactory.mkLt(expr1, expr2), expr2, expr1);

        Function<EXPR, EXPR> func =
            obj -> eFactory.mkAnd(eFactory.mkLeq(low, obj), eFactory.mkLeq(obj, up));
        Function<EXPR, EXPR> finalFunc = setFunc;
        setFunc = obj -> eFactory.mkOr(finalFunc.apply(obj), func.apply(obj));
        type = expr1.getType();
      }
    }
    EXPR setElem = mkConst("elem", type);

    return eFactory.mkSet(setFunc, setElem);
  }

  /***
   * convert the right side of a set comprehension(filters)  into a BoolExpr
   */
  protected EXPR convertSetCompRightSide(List<ASTSetComprehensionItem> filters) {
    EXPR filter = eFactory.mkBool(true);

    // example: x isin {2,3}
    for (ASTSetComprehensionItem item : filters) {
      EXPR subFilter = eFactory.mkBool(true);
      if (item.isPresentGeneratorDeclaration()) {
        EXPR set = convertExpr(item.getGeneratorDeclaration().getExpression());
        String exprName = item.getGeneratorDeclaration().getName();
        subFilter = eFactory.mkContains(set, varNames.get(exprName));
      }

      // example: x == y
      if (item.isPresentExpression()) {
        subFilter = convertExpr(item.getExpression());
      }

      // example: int i = 10
      if (item.isPresentSetVariableDeclaration()) {
        ASTSetVariableDeclaration var = item.getSetVariableDeclaration();
        if (var.isPresentExpression()) {
          subFilter = eFactory.mkEq(varNames.get(var.getName()), convertExpr(var.getExpression()));
        }
      }
      filter = eFactory.mkAnd(filter, subFilter);
    }
    return filter;
  }

  protected Function<EXPR, EXPR> convertSetCompLeftSide(
      ASTSetComprehensionItem node, Set<String> declVars) {
    List<EXPR> vars =
        declVars.stream().map(expr -> varNames.get(expr)).collect(Collectors.toList());
    EXPR filter;
    EXPR expr;

    if (node.isPresentGeneratorDeclaration()) {
      expr = varNames.get(node.getGeneratorDeclaration().getName());
      EXPR set = convertExpr(node.getGeneratorDeclaration().getExpression());
      filter = eFactory.mkContains(set, expr);
    } else if (node.isPresentSetVariableDeclaration()) {
      expr = varNames.get(node.getSetVariableDeclaration().getName());

      if (node.isPresentExpression()) {
        filter = eFactory.mkEq(expr, convertExpr(node.getExpression()));
      } else {
        filter = eFactory.mkBool(true);
      }
    } else { // expression is present

      EXPR temp = convertExpr(node.getExpression());

      expr = mkConst("var", temp.getType());
      filter = eFactory.mkEq(expr, temp);
      vars.add(expr);
    }
    EXPR setElem = mkConst("elem", expr.getType());
    return bool ->
        eFactory.mkSet(
            obj ->
                cdFactory.mkExists(
                    vars, eFactory.mkAnd(eFactory.mkEq(obj, expr), eFactory.mkAnd(filter, bool))),
            setElem);
  }

  // a.auction**
  protected EXPR convertTransClo(ASTOCLTransitiveQualification node) {

    ASTFieldAccessExpression fieldAcc;
    if (node.getExpression() instanceof ASTFieldAccessExpression) {
      fieldAcc = (ASTFieldAccessExpression) node.getExpression();
    } else {
      Log.error("conversion of transitive closure not completed yet");
      assert false;
      fieldAcc = null;
    }
    if (!node.isTransitive()) {
      return convertExpr(node);
    }
    EXPR obj = convertExpr(fieldAcc.getExpression());
    return cdFactory.getLinkTransitive(obj, fieldAcc.getName());
  }

  /***
   *Helper function to build a quantified formulas.
   * Quantification of CDType expressions (Auction, person...) must be perform by ge CD2SMTGenerator +
   * according to the actual Strategy.
   * But quantification of Expressions with primitive types (Bool, Int,..) must be perform directly.
   */

  public void reset() {
    varNames.clear();
  }
}
