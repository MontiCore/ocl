package de.monticore.ocl2smt.ocl2smt.expressionconverter;

import com.microsoft.z3.*;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.ocl2smt.ocl2smt.expr.*;
import de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory.CDExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.oclExprFactory.OCLExprFactory;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.ocl2smt.util.TypeConverter;
import de.monticore.ocl2smt.visitors.SetGeneratorCollector;
import de.monticore.ocl2smt.visitors.SetVariableCollector;
import de.monticore.types.check.SymTypeExpression;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/** This class convert All OCL-Expressions except @Pre-Expressions in SMT */
public class OCLExprConverter<T extends ExprAdapter<?>> extends BasicExprConverter<T> {
  CDExprFactory<T> cdExprFactory;
  OCLExprFactory<T> oclFtory;
  CD2SMTGenerator cd2SMTGenerator;
  public TypeConverter typeConverter;

  protected Map<String, T> varNames;
  protected Map<T, OCLType> varTypes;
  protected Set<T> genConstraints;

  public OCLExprConverter(ASTCDCompilationUnit astcdCompilationUnit, Context ctx) {
    super(null);

    cd2SMTGenerator = CD2SMTMill.cd2SMTGenerator();

    cd2SMTGenerator.cd2smt(astcdCompilationUnit, ctx);
    // typeConverter = new TypeConverter(cd2SMTGenerator);
    // cdExprFactory = new Z3CDExprFactory(cd2SMTGenerator);

  }

  public OCLExprConverter(ASTCDCompilationUnit ast, OCL2SMTGenerator ocl2SMTGenerator) {
    super(null);

    cd2SMTGenerator = ocl2SMTGenerator.getCD2SMTGenerator();
    cd2SMTGenerator.cd2smt(ast, ocl2SMTGenerator.getCtx());
    typeConverter = new TypeConverter(cd2SMTGenerator);
    //  cdExprFactory = new Z3CDExprFactory(cd2SMTGenerator);
  }

  public ASTCDDefinition getCD() {
    return cd2SMTGenerator.getClassDiagram().getCDDefinition();
  }

  public Set<T> getGenConstraints() {
    return genConstraints;
  }

  public T mkConst(String name, OCLType oclType) {
    return oclFtory.mkConst(name, oclType);
  }

  /*------------------------------------quantified expressions----------------------------------------------------------*/
  private Pair<List<T>, T> openScope(List<ASTInDeclaration> inDeclarations) {
    List<T> variableList = new ArrayList<>();
    T constraint = factory.mkBool(true);

    for (ASTInDeclaration decl : inDeclarations) {
      for (ASTInDeclarationVariable varDecl : decl.getInDeclarationVariableList()) {

        OCLType type;
        if (decl.isPresentMCType()) {
          type = typeConverter.buildOCLType(decl.getMCType()); // todo delete one ?
        } else {
          type = typeConverter.buildOCLType(varDecl.getSymbol());
        }

        T var = mkConst(varDecl.getName(), type);

        if (decl.isPresentExpression()) {
          T mySet = convertExpr(decl.getExpression());
          factory.mkAnd(factory.mkContains(mySet, var), constraint);
        }
      }
    }

    return new ImmutablePair<>(variableList, constraint);
  }

  protected T convertForAll(ASTForallExpression node) {
    Pair<List<T>, T> quanParams = openScope(node.getInDeclarationList());

    T body = factory.mkImplies(quanParams.getRight(), convertExpr(node.getExpression()));
    T result = factory.mkForall(quanParams.getLeft(), body);

    closeScope(node.getInDeclarationList());
    return result;
  }

  protected T convertExist(ASTExistsExpression node) {
    Pair<List<T>, T> quanParams = openScope(node.getInDeclarationList());

    T body = factory.mkAnd(quanParams.getRight(), convertExpr(node.getExpression()));
    T result = factory.mkExists(quanParams.getLeft(), body);

    closeScope(node.getInDeclarationList());
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

  @Override
  protected T convert(ASTNameExpression node) {
    if (varNames.containsKey(node.getName())) {
      return varNames.get(node.getName());
    } else {
      return createVarFromSymbol(node);
    }
  }

  @Override
  protected T convert(ASTFieldAccessExpression node) {
    T obj = convertExpr(node.getExpression());
    return cdExprFactory.getLinkedObjects(obj, node.getName());
  }

  @Override
  public T convertExpr(ASTExpression node) {

    T result = null;
    SymTypeExpression typeExpression = TypeConverter.deriveType(node);
    if ((node instanceof ASTCallExpression) && typeExpression.isObjectType()) {
      result = convertMethodCall((ASTCallExpression) node);
    }
    if (node instanceof ASTForallExpression) {
      result = convertForAll((ASTForallExpression) node);
    } else if (node instanceof ASTExistsExpression) {
      result = convertExist((ASTExistsExpression) node);
    } else if (node instanceof ASTSetInExpression) {
      T set1 = convertExpr(((ASTSetInExpression) node).getSet());
      T set2 = convertExpr(((ASTSetInExpression) node).getElem());
      result = factory.mkContains(set1, set2);
    } else if (node instanceof ASTSetNotInExpression) {
      T set1 = convertExpr(((ASTSetNotInExpression) node).getSet());
      T set2 = convertExpr(((ASTSetNotInExpression) node).getElem());
      result = factory.mkNot(factory.mkContains(set1, set2));
    } else if (node instanceof ASTCallExpression && TypeConverter.hasBooleanType(node)) {
      result = this.convertMethodCall((ASTCallExpression) node);
    } else if (node instanceof ASTOCLTransitiveQualification) {
      result = convertTransClo((ASTOCLTransitiveQualification) node);
    } else if (node instanceof ASTUnionExpression) {
      T set1 = convertExpr(((ASTUnionExpression) node).getLeft());
      T set2 = convertExpr(((ASTUnionExpression) node).getRight());
      result = factory.mkSetUnion(set1, set2);
    } else if (node instanceof ASTIntersectionExpression) {
      T set1 = convertExpr(((ASTIntersectionExpression) node).getLeft());
      T set2 = convertExpr(((ASTIntersectionExpression) node).getRight());
      result = factory.mkSetIntersect(set1, set2);
    } else if (node instanceof ASTSetMinusExpression) {
      T set1 = convertExpr(((ASTSetMinusExpression) node).getLeft());
      T set2 = convertExpr(((ASTSetMinusExpression) node).getRight());
      result = factory.mkSetMinus(set1, set2);
    } else if (node instanceof ASTSetComprehension) {
      result = convertSetComp((ASTSetComprehension) node);
    } else if (node instanceof ASTSetEnumeration) {
      result = convertSetEnum((ASTSetEnumeration) node);
    }

    if (result != null) {
      return result;
    }

    return super.convertExpr(node);
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
      OCLType varType = typeConverter.buildOCLType(var.getMCType());
      oclFtory.mkConst(var.getName(), varType);
    }

    // convert variable by generator-declaration : int x in {1,2}
    for (ASTGeneratorDeclaration gen : generatorCollector.getAllGenrators()) {
      OCLType type;
      if (gen.isPresentMCType()) {
        type = typeConverter.buildOCLType(gen.getMCType());
      } else {
        type = typeConverter.buildOCLType(gen.getSymbol());
      }
      oclFtory.mkConst(gen.getName(), type);
    }

    Set<String> setCompScopeVar = new HashSet<>(varCollector.getAllVariableNames());
    setCompScopeVar.addAll(generatorCollector.getAllVariableNames());

    return setCompScopeVar;
  }

  protected T convertSetComp(ASTSetComprehension node) {
    Set<String> setCompVarNames = openSetCompScope(node);

    Function<T, T> setComp = convertSetCompLeftSide(node.getLeft(), setCompVarNames);
    T filter = convertSetCompRightSide(node.getSetComprehensionItemList());

    closeSetCompScope(setCompVarNames);
    return setComp.apply(filter);
  }

  private void closeSetCompScope(Set<String> setCompVarNames) {
    for (String name : setCompVarNames) {
      varNames.remove(name);
    }
    setCompVarNames.clear();
  }

  protected T convertSetEnum(ASTSetEnumeration node) {
    ExpressionKind kind = null;

    List<T> setItemValues = new ArrayList<>();
    List<Function<T, T>> rangeFilters = new ArrayList<>();

    for (ASTSetCollectionItem item : node.getSetCollectionItemList()) {

      if (item instanceof ASTSetValueItem) {
        setItemValues.add(convertExpr(((ASTSetValueItem) item).getExpression()));
        kind = setItemValues.get(0).getExprKind();
      } else if (item instanceof ASTSetValueRange) {
        Pair<Function<T, T>, ExpressionKind> range = convertSetValRang((ASTSetValueRange) item);
        rangeFilters.add(range.getLeft());
        kind = range.getRight();
      }
    }

    assert kind != null;
    T set = factory.mkSet(obj -> factory.mkBool(false), OCLType.buildOCLType(kind), this);
    T set1 = set;
    if (!setItemValues.isEmpty()) {

      Function<T, T> setFunc =
          obj ->
              factory.mkOr(factory.mkContains(set1, obj), addValuesToSetEnum(setItemValues, obj));
      set = factory.mkSet(setFunc, OCLType.buildOCLType(kind), this);
    }

    for (Function<T, T> range : rangeFilters) {
      T set2 = set;
      Function<T, T> setFunc = obj -> factory.mkOr(factory.mkContains(set2, obj), range.apply(obj));
      set = factory.mkSet(setFunc, OCLType.buildOCLType(kind), this);
    }
    return set;
  }

  private Pair<Function<T, T>, ExpressionKind> convertSetValRang(ASTSetValueRange node) {
    T expr1 = convertExpr(node.getUpperBound());
    T expr2 = convertExpr(node.getLowerBound());
    T low = factory.mkIte(factory.mkLt(expr1, expr2), expr1, expr2);
    T up = factory.mkIte(factory.mkLt(expr1, expr2), expr2, expr1);
    return new ImmutablePair<>(
        obj -> factory.mkAnd(factory.mkLeq(low, obj), factory.mkLeq(obj, up)), low.getExprKind());
  }

  T addValuesToSetEnum(List<T> elements, T value) {
    T res = factory.mkBool(false);
    for (T setElem : elements) {
      res = factory.mkOr(res, factory.mkEq(value, setElem));
    }
    return res;
  }

  /***
   * convert the right side of a set comprehension(filters)  into a BoolExpr
   */
  protected T convertSetCompRightSide(List<ASTSetComprehensionItem> filters) {
    T filter = factory.mkBool(true);

    // example: x isin {2,3}
    for (ASTSetComprehensionItem item : filters) {
      T subFilter = factory.mkBool(true);
      if (item.isPresentGeneratorDeclaration()) {
        T set = convertExpr(item.getGeneratorDeclaration().getExpression());
        String exprName = item.getGeneratorDeclaration().getName();
        subFilter = factory.mkContains(set, varNames.get(exprName));
      }

      // example: x == y
      if (item.isPresentExpression()) {
        subFilter = convertExpr(item.getExpression());
      }

      // example: int i = 10
      if (item.isPresentSetVariableDeclaration()) {
        ASTSetVariableDeclaration var = item.getSetVariableDeclaration();
        if (var.isPresentExpression()) {
          subFilter = factory.mkEq(varNames.get(var.getName()), convertExpr(var.getExpression()));
        }
      }
      filter = factory.mkAnd(filter, subFilter);
    }
    return filter;
  }

  protected Function<T, T> convertSetCompLeftSide(
      ASTSetComprehensionItem node, Set<String> declVars) {
    List<T> vars = declVars.stream().map(expr -> varNames.get(expr)).collect(Collectors.toList());
    T filter;
    T expr;

    if (node.isPresentGeneratorDeclaration()) {
      expr = varNames.get(node.getGeneratorDeclaration().getName());
      T set = convertExpr(node.getExpression());
      filter = factory.mkContains(set, expr);
    } else if (node.isPresentSetVariableDeclaration()) {
      expr = varNames.get(node.getSetVariableDeclaration().getName());

      if (node.isPresentExpression()) {
        filter = factory.mkEq(expr, convertExpr(node.getExpression()));
      } else {
        filter = factory.mkBool(true);
      }
    } else { // expression is present

      expr = convertExpr(node.getExpression());

      T expr2 = oclFtory.mkConst("var", getType(expr));
      filter = factory.mkEq(expr2, expr);
      vars.add(expr2);
    }

    return bool ->
        factory.mkSet(
            obj ->
                factory.mkExists(
                    vars, factory.mkAnd(factory.mkEq(obj, expr), factory.mkAnd(filter, bool))),
            getType(expr),
            this);
  }

  // a.auction**
  protected T convertTransClo(ASTOCLTransitiveQualification node) {
    /*
    ASTFieldAccessExpression fieldAcc = null;
    if (node.getExpression() instanceof ASTFieldAccessExpression) {
      fieldAcc = (ASTFieldAccessExpression) node.getExpression();
    } else if (node.getExpression() instanceof ASTCallExpression) {
      ASTCallExpression callExpression = (ASTCallExpression) node.getExpression();
      if (TypeConverter.hasOptionalType(
          ((ASTFieldAccessExpression) callExpression.getExpression()).getExpression())) {
        fieldAcc =
            (ASTFieldAccessExpression)
                ((ASTFieldAccessExpression) callExpression.getExpression()).getExpression();
      }
    }
    if (!node.isTransitive()) {
      return convertExpr(node);
    }

    T auction = convertExpr(fieldAcc.getExpression());

    FuncDecl<BoolSort> rel = buildReflexiveNewAssocFunc(getType(auction), fieldAcc.getName());
    FuncDecl<BoolSort> trans_rel = TransitiveClosure.mkTransitiveClosure(ctx, rel);

    Function<T, T> setFunc = obj -> factory.mkBool((BoolExpr) trans_rel.apply(auction, obj));
    return factory.mkSet(setFunc, getType(auction), this);*/
    return null;
  }

  private FuncDecl<BoolSort> buildReflexiveNewAssocFunc(OCLType type, String otherRole) {
    /* ASTCDType objClass = getASTCDType(type.getName(), getCD());
    ASTCDAssociation association = CDHelper.getAssociation(objClass, otherRole, getCD());
    Sort thisSort = typeConverter.deriveSort(type);
    FuncDecl<BoolSort> rel =
        ctx.mkFuncDecl("reflexive_relation", new Sort[] {thisSort, thisSort}, ctx.mkBoolSort());
    T obj1 = declVariable(type, "obj1");
    T obj2 = declVariable(type, "obj2");
    T rel_is_assocFunc =
        factory.mkForall(
            List.of(obj1, obj2),
            factory.mkEq(
                factory.mkBool((BoolExpr) rel.apply(obj1.expr(), obj2.expr())),
                factory.mkBool(
                    cd2smtGenerator.evaluateLink(
                        association, objClass, objClass, obj1.expr(), obj2.expr()))));
    genConstraints.add(rel_is_assocFunc);*/
    return null;
  }

  /***
   *Helper function to build a quantified formulas.
   * Quantification of CDType expressions (Auction, person...) must be perform by ge CD2SMTGenerator +
   * according to the actual Strategy.
   * But quantification of Expressions with primitive types (Bool, Int,..) must be perform directly.
   */

  public OCLType getType(T expr) {
    if (varTypes.containsKey(expr)) {
      return varTypes.get(expr);
    } else if (isNativeKind(expr.getExprKind())) {
      return OCLType.buildOCLType(expr.getExprKind());
    }
    Log.error("Type not found for the Variable " + expr);
    return null;
  }

  protected T createVarFromSymbol(ASTNameExpression node) {
    SymTypeExpression typeExpr = TypeConverter.deriveType(node);
    OCLType type = typeConverter.buildOCLType(typeExpr);
    return oclFtory.mkConst(node.getName(), type);
  }

  public void reset() {
    varNames.clear();
    genConstraints.clear();
  }

  protected boolean isNativeKind(ExpressionKind kind) {
    return kind == ExpressionKind.BOOL
        || kind == ExpressionKind.DOUBLE
        || kind == ExpressionKind.STRING
        || kind == ExpressionKind.CHAR;
  }

  public T mkQuantifier(List<T> vars, T body, boolean isForall) {

    // split expressions int non CDZ3ExprAdapterype(String , Bool..) and CDType Expression(Auction,
    // Person...)
    List<T> unInterpretedObj =
        vars.stream()
            .filter(
                e ->
                    (e.getExprKind() == ExpressionKind.UNINTERPRETED
                        && !(e.getExprKind() == ExpressionKind.BOOL)))
            .collect(Collectors.toList());

    List<T> uninterpretedBool =
        vars.stream()
            .filter(
                e ->
                    (e.getExprKind() == ExpressionKind.UNINTERPRETED
                        && (e.getExprKind() == ExpressionKind.BOOL)))
            .collect(Collectors.toList());

    // collect the CDType of the CDType Expressions
    /* List<ASTCDType> types =
    unInterpretedObj.stream()
            .map(var -> getASTCDType( getType(var).getName(), getCD()))
            .collect(Collectors.toList());*/
    T subRes = body;

    if (!unInterpretedObj.isEmpty()) {
      if (isForall) {
        subRes = cdExprFactory.mkForall(unInterpretedObj, body);
        // todo: refactoring this method
      } else {
        subRes = cdExprFactory.mkExists(unInterpretedObj, body);
      }
    }

    if (uninterpretedBool.isEmpty()) {
      return subRes;
    } else {
      if (isForall) {
        return factory.mkForall(uninterpretedBool, subRes);
      } else {
        return factory.mkExists(uninterpretedBool, subRes);
      }
    }
  }

  public T mkForall(List<T> expr, T z3ExprAdapter) {
    return mkQuantifier(expr, z3ExprAdapter, true);
  }

  public T mkExists(List<T> expr, T z3ExprAdapter) {
    return mkQuantifier(expr, z3ExprAdapter, false);
  }

  public CD2SMTGenerator getCd2smtGenerator() {
    return cd2SMTGenerator;
  }
}
