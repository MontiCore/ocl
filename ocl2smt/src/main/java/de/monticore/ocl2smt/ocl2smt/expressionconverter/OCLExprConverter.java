package de.monticore.ocl2smt.ocl2smt.expressionconverter;

import static de.monticore.cd2smt.Helper.CDHelper.getASTCDType;
import static de.monticore.ocl2smt.helpers.IOHelper.print;
import static de.monticore.ocl2smt.helpers.IOHelper.printPosition;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.Helper.SMTHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.ocl2smt.ocl2smt.expr.*;
import de.monticore.ocl2smt.ocl2smt.expr2smt.Z3CDExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.T;
import de.monticore.ocl2smt.ocl2smt.expr2smt.Z3ExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.Z3OCLExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprFactory.ExprFactory;
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
  
  Z3CDExprFactory cdExprFactory  ;
  
  Z3OCLExprFactory oclExprFactory;
  CD2SMTGenerator cd2SMTGenerator ;
  
  protected Set<T> genConstraints;

  public TypeConverter typeConverter;

  public OCLExprConverter(ASTCDCompilationUnit astcdCompilationUnit, Context ctx) {
      super((ExprFactory<T>) new Z3ExprFactory(ctx));

      genConstraints = new HashSet<>();
   
     cd2SMTGenerator =CD2SMTMill.cd2SMTGenerator();


    cd2SMTGenerator.cd2smt(astcdCompilationUnit, ctx);
    typeConverter = new TypeConverter(cd2SMTGenerator);
    cdExprFactory = new Z3CDExprFactory(cd2SMTGenerator);

  }

  public OCLExprConverter(ASTCDCompilationUnit ast, OCL2SMTGenerator ocl2SMTGenerator) {
      super((ExprFactory<T>) new Z3ExprFactory(ocl2SMTGenerator.getCtx()));

      cd2SMTGenerator = ocl2SMTGenerator.getCD2SMTGenerator();
    cd2SMTGenerator.cd2smt(ast, ocl2SMTGenerator.getCtx());
    typeConverter = new TypeConverter(cd2SMTGenerator);
    cdExprFactory = new Z3CDExprFactory(cd2SMTGenerator);
  }

  public void reset() {
    varNames.clear();
    genConstraints.clear();
  }

  public ASTCDDefinition getCD() {
    return cd2SMTGenerator.getClassDiagram().getCDDefinition();
  }

  public Set<T> getGenConstraints() {
    return genConstraints;
  }



  @Override
  protected T convertMethodCall(ASTCallExpression node) {
    T res = null;

    if (node.getExpression() instanceof ASTFieldAccessExpression) {
      String methodName = ((ASTFieldAccessExpression) node.getExpression()).getName();

      Pair<T, T> link = convertFieldAccOptional((ASTFieldAccessExpression) node.getExpression());
      T isPresent = mkExists(List.of(link.getLeft()), link.getRight());
      
      switch (methodName) {
        case "isPresent":
          res = isPresent;
          break;
        case "isEmpty":
          res = factory.mkNot(isPresent);
          break;
      }
    }
    // TODO: 24.08.23 handle the case of nested call
    return res;
  }



  /*------------------------------------quantified expressions----------------------------------------------------------*/
  private Map<T, Optional<ASTExpression>> openScope(
      List<ASTInDeclaration> inDeclarations) {
    Map<T, Optional<ASTExpression>> variableList = new HashMap<>();
    for (ASTInDeclaration decl : inDeclarations) {
      List<T> temp = convertInDecl(decl);
      if (decl.isPresentExpression()) {
        temp.forEach(t -> variableList.put(t, Optional.of(decl.getExpression())));
      } else {
        temp.forEach(t -> variableList.put(t, Optional.empty()));
      }
    }
    return variableList;
  }

  protected T convertInDeclConstraints(
      Map<T, Optional<ASTExpression>> var) {
    // get all the InPart in InDeclarations
    Map<T, ASTExpression> inParts = new HashMap<>();
    var.forEach((key, value) -> value.ifPresent(s -> inParts.put(key, s)));

    List<T> constraintList = new ArrayList<>();

    for (Map.Entry<T, ASTExpression> expr : inParts.entrySet()) {
      T mySet = convertExpr(expr.getValue());
      constraintList.add(factory.mkContains(mySet,expr.getKey()));
    }
    T result = factory.mkBool(true);

    for (T constr : constraintList) {
      result = factory.mkAnd(result, constr);
    }
    return result;
  }



  // -----------------------------------general----------------------------------------------------------------------*/
  @Override
  protected T convert(ASTNameExpression node) {
    T res;
    if (varNames.containsKey(node.getName())) {
      res = varNames.get(node.getName());
    } else {
      res = createVarFromSymbol(node);
    }
    return res;
  }



  // ========================================ASTFieldAccessExpressions===================================================

  /** convert a field access expression when the result produces a set */
  protected T convertFieldAccessSet(ASTFieldAccessExpression node) {
    SymTypeExpression elementType = TypeConverter.deriveType(node);
    return convertFieldAccessSetHelper(node.getExpression(), node.getName());
  }

  protected T convertFieldAccessSetHelper(ASTExpression node, String name) {

    // case we have simple field access. e.g. node = p, name = auction
    if (!(node instanceof ASTFieldAccessExpression)) {
      T expr = convertExpr(node);
      return convertSimpleFieldAccessSet(expr, name);
    }

    // case we have nested field access. e.g. node = p.auction, name = person
    T leftSet = convertExpr(node);

    // computes the type of the element of the right Set
    OCLType type1 = leftSet.type;
    ASTCDAssociation person_parent = OCLHelper.getAssociation(type1, name, getCD());
    OCLType rightSetElemType = OCLHelper.getOtherType(person_parent, type1, name, getCD());

    Function<T, T> function =
        obj1 ->
            new T(
                obj2 ->
                    OCLHelper.evaluateLink(
                        person_parent,
                        obj1.expr(),
                        name,
                        obj2.expr(),
                        cd2smtGenerator,
                        this::getType),
                rightSetElemType,
                this);

    return leftSet.collectAll(function);

    return null;
  }

  protected Pair<T, T> convertFieldAccessSetHelper(
      T obj, String name) {

    OCLType type = getType(obj);
    ASTCDDefinition cd = cd2smtGenerator.getClassDiagram().getCDDefinition();
    Pair<T, T> res = null;
    ASTCDType astcdType = getASTCDType(type.getName(), cd);

    if (OCLHelper.containsAttribute(astcdType, name, cd)) { // case obj.attribute
      res =
          new ImmutablePair<>(
              factory
                  .mkExpr(
                      ExpressionKind.UNINTERPRETED,
                      OCLHelper.getAttribute(obj.expr(), type, name, cd2smtGenerator)),
              factory.mkBool(true));
    } else { // case obj.link
      res = convertFieldAccessAssocHelper(obj, name);
    }

    return res;
  }

  private Pair<T, T> convertFieldAccessAssocHelper(
      T obj, String role) {
    OCLType type = getType(obj);
    ASTCDAssociation association = OCLHelper.getAssociation(type, role, getCD());

    OCLType type2 = OCLHelper.getOtherType(association, type, role, getCD());
    String resName = obj.expr().toString() + SMTHelper.fCharToLowerCase(type2.getName());
    T link = declVariable(type2, resName);
    T linkConstraint =
        factory
            .mkBool(
                OCLHelper.evaluateLink(
                    association, obj, role, link, cd2smtGenerator, this::getType));
    return new ImmutablePair<>(link, linkConstraint);
  }

  protected T convert(ASTFieldAccessExpression node) {
    T obj = convertExpr(node.getExpression());
    Pair<T, T> res = convertFieldAccessSetHelper(obj, node.getName());
    if (!TypeConverter.hasOptionalType(node)) {
      genConstraints.add(res.getRight());
    }
    return res.getLeft();
  }

  protected Pair<T, T> convertFieldAccOptional(
      ASTFieldAccessExpression node) {
    T obj = convertExpr(node.getExpression());
    return convertFieldAccessSetHelper(obj, node.getName());
  }

  protected T convertSimpleFieldAccessSet(T obj, String role) {
    OCLType type1 = getType(obj);
    ASTCDAssociation association = OCLHelper.getAssociation(type1, role, getCD());
    OCLType type2 = OCLHelper.getOtherType(association, type1, role, getCD());

    Function<T, T> auction_per_set =
        per ->
            factory
                .mkBool(
                    OCLHelper.evaluateLink(
                        association, obj, role, per, cd2smtGenerator, this::getType));

    return factory.mkSet(auction_per_set, type2, this);
  }

  // ======================================End ASTFieldAccessExpression============================
  protected void closeScope(List<ASTInDeclaration> inDeclarations) {
    for (ASTInDeclaration decl : inDeclarations) {
      for (ASTInDeclarationVariable var : decl.getInDeclarationVariableList()) {
        assert varNames.containsKey(var.getName());
        varNames.remove(var.getName());
      }
    }
  }

  protected List<T> convertInDecl(ASTInDeclaration node) {
    List<T> result = new ArrayList<>();
    for (ASTInDeclarationVariable var : node.getInDeclarationVariableList()) {
      if (node.isPresentMCType()) {
        result.add(declVariable(typeConverter.buildOCLType(node.getMCType()), var.getName()));
      } else {
        result.add(declVariable(typeConverter.buildOCLType(var.getSymbol()), var.getName()));
      }
    }
    return result;
  }

  // ---------------------------------------Set-Expressions----------------------------------------------------------------
  protected T convertSetIn(ASTSetInExpression node) {
    return convertExpr(node.getSet()).contains(convertExpr(node.getElem()));
  }

  protected T convertSetNotIn(ASTSetNotInExpression node) {
    return factory
        .mkNot(convertExpr(node.getSet()).contains(convertExpr(node.getElem())));
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
      result = convertSetIn((ASTSetInExpression) node);
    } else if (node instanceof ASTSetNotInExpression) {
      result = convertSetNotIn((ASTSetNotInExpression) node);
    } else if (node instanceof ASTCallExpression && TypeConverter.hasBooleanType(node)) {
      result = this.convertMethodCall((ASTCallExpression) node);
    }

    if (node instanceof ASTFieldAccessExpression) {
      result = convertFieldAccessSet((ASTFieldAccessExpression) node);
    } else if (node instanceof ASTOCLTransitiveQualification) {
      result = convertTransClo((ASTOCLTransitiveQualification) node);
    } else if (node instanceof ASTUnionExpression) {
      result = convertSetUnion((ASTUnionExpression) node);
    } else if (node instanceof ASTIntersectionExpression) {
      result = convertSetInter((ASTIntersectionExpression) node);
    } else if (node instanceof ASTSetMinusExpression) {
      result = convertSetMinus((ASTSetMinusExpression) node);

    } else if (node instanceof ASTSetComprehension) {
      result = convertSetComp((ASTSetComprehension) node);
    } else if (node instanceof ASTSetEnumeration) {
      result = convertSetEnum((ASTSetEnumeration) node);
    }

    if (result != null && result.getKind() != ExpressionKind.NULL) {
      return result;
    }

    return super.convertExpr(node);
  }

  protected T convertSetUnion(ASTUnionExpression node) {
    return convertExpr(node.getLeft()).mkSetUnion(convertExpr(node.getRight()));
  }

  protected T convertSetMinus(ASTSetMinusExpression node) {
    return convertExpr(node.getLeft()).mkSetMinus(convertExpr(node.getRight()));
  }

  protected T convertSetInter(ASTIntersectionExpression node) {
    return convertExpr(node.getLeft()).mkSetIntersect(convertExpr(node.getRight()));
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
      declVariable(varType, var.getName());
    }

    // convert variable by generator-declaration : int x in {1,2}
    for (ASTGeneratorDeclaration gen : generatorCollector.getAllGenrators()) {
      OCLType type;
      if (gen.isPresentMCType()) {
        type = typeConverter.buildOCLType(gen.getMCType());
      } else {
        type = typeConverter.buildOCLType(gen.getSymbol());
      }
      declVariable(type, gen.getName());
    }

    Set<String> setCompScopeVar = new HashSet<>(varCollector.getAllVariableNames());
    setCompScopeVar.addAll(generatorCollector.getAllVariableNames());

    return setCompScopeVar;
  }

  protected T convertSetComp(ASTSetComprehension node) {
    Set<String> setCompVarNames = openSetCompScope(node);

    Function<T, T> setComp =
        convertSetCompLeftSide(node.getLeft(), setCompVarNames);
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

  /***
   * convert set enum to smt {1,}
   * @param node
   * @return
   */
  protected T convertSetEnum(ASTSetEnumeration node) {
    ExpressionKind kind = null;
    List<T> setItemValues = new ArrayList<>();
    List<Function<T, T>> rangeFilters = new ArrayList<>();

    for (ASTSetCollectionItem item : node.getSetCollectionItemList()) {

      if (item instanceof ASTSetValueItem) {
        setItemValues.add(convertExpr(((ASTSetValueItem) item).getExpression()));
        kind = setItemValues.get(0).getKind();
      } else if (item instanceof ASTSetValueRange) {
        Pair<Function<T, T>, ExpressionKind> range =
            convertSetValRang((ASTSetValueRange) item);
        rangeFilters.add(range.getLeft());
        kind = range.getRight();
      }
    }
    assert kind != null;
    T set =
        factory
            .mkSet(
                obj -> factory.mkBool(false), OCLType.buildOCLType(kind), this);
    T set1 = set;
    if (!setItemValues.isEmpty()) {
      set =
          factory
              .mkSet(
                  obj ->
                      factory
                          .mkOr(factory.mkContains(set1,obj), addValuesToSetEnum(setItemValues, obj)),
                  OCLType.buildOCLType(kind),
                  this);
    }

    for (Function<T, T> range : rangeFilters) {

      T set2 = set;
      set =
          factory
              .mkSet(
                  obj -> factory.mkOr(factory.mkContains(set2,obj), range.apply(obj)),
                  OCLType.buildOCLType(kind),
                  this);
    }
    return set;
  }

  private Pair<Function<T, T>, ExpressionKind> convertSetValRang(
      ASTSetValueRange node) {
    T expr1 = convertExpr(node.getUpperBound());
    T expr2 = convertExpr(node.getLowerBound());
    T low =
        factory.mkIte(factory.mkLt(expr1, expr2), expr1, expr2);
    T up =
        factory
            .mkIte(factory.mkLt(expr1, expr2), expr2, expr1);
    return new ImmutablePair<>(
        obj ->
            factory
                .mkAnd(
                    factory.mkLeq(low, obj),
                    factory.mkLeq(obj, up)),
        low.getKind());
  }

  T addValuesToSetEnum(List<T> elements, T value) {
    T res = factory.mkBool(false);
    for (T setElem : elements) {
      res = factory.mkOr(res, factory.mkEq(value, setElem));
    }
    return res;
  }

  protected Function<T, T> convertSetCompLeftSide(
      ASTSetComprehensionItem node, Set<String> declVars) {
    Function<T, T> res = null;
    if (node.isPresentGeneratorDeclaration()) {
      res = convertGenDeclLeft(node.getGeneratorDeclaration(), declVars);
    } else if (node.isPresentSetVariableDeclaration()) {
      res = convertSetVarDeclLeft(node.getSetVariableDeclaration(), declVars);
    } else if (node.isPresentExpression()) {
      res = convertSetCompExprLeft(node.getExpression(), declVars);
    } else {
      Log.error(
          printPosition(node.get_SourcePositionStart())
              + " Unable to convert the expression "
              + print(node));
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

      if (item.isPresentGeneratorDeclaration()) {
        T set = convertExpr(item.getGeneratorDeclaration().getExpression());
        T subFilter =
            set.contains(varNames.get(item.getGeneratorDeclaration().getName()));
        filter = factory.mkAnd(filter, subFilter);
      }

      // example: x == y
      if (item.isPresentExpression()) {
        filter = factory.mkAnd(filter, convertExpr(item.getExpression()));
      }

      // example: int i = 10
      if (item.isPresentSetVariableDeclaration()) {
        ASTSetVariableDeclaration var = item.getSetVariableDeclaration();
        if (var.isPresentExpression()) {
          T subFilter =
              factory
                  .mkEq(varNames.get(var.getName()), convertExpr(var.getExpression()));
          filter = factory.mkAnd(filter, subFilter);
        }
      }
    }
    return filter;
  }

  protected Function<T, T> convertSetCompExprLeft(
      ASTExpression node, Set<String> declVars) {
    T expr1 = convertExpr(node);
    // define a const  for the quantifier
    T expr2 = factory.mkExpr("var", expr1.sort());
    List<T> vars = new ArrayList<>(collectExpr(declVars));
    vars.add(expr2);

    return bool ->
        factory
            .mkSet(
                obj ->
                  factory.mkExists(
                        vars,
                        factory
                            .mkAnd(
                                factory.mkEq(obj, expr2),
                                factory
                                    .mkAnd(factory.mkEq(expr2, expr1), bool))),
                getType(expr1),
                this);
  }

  /***
   * will be transformed to a function that takes a bool expression and return
   * and SMTSet because the filter of the function is on the right side, and
   * this function just converts the left Side.
   * *
   * example: {int z = x*x|...}
   * *
   */
  protected Function<T, T> convertSetVarDeclLeft(
      ASTSetVariableDeclaration node, Set<String> declVars) {
    T var = varNames.get(node.getName());
    List<T> varList = new ArrayList<>(collectExpr(declVars));

    // check if the initial value of the var is present and convert it to a constraint
    T constr =
        node.isPresentExpression()
            ? factory.mkEq(var, convertExpr(node.getExpression()))
            : factory.mkBool(true);

    // create the function bool ->SMTSet
    return bool ->
        factory
            .mkSet(
                obj ->
                   factory.mkExists(
                        varList,
                        factory
                            .mkAnd(
                                factory.mkEq(obj, var),
                                factory.mkAnd(constr, bool))),
                getType(var),
                this);
  }

  private Set<T> collectExpr(Set<String> exprSet) {
    return exprSet.stream().map(expr -> varNames.get(expr)).collect(Collectors.toSet());
  }

  protected Function<T, T> convertGenDeclLeft(
      ASTGeneratorDeclaration node, Set<String> declVars) {

    T expr = varNames.get(node.getName());
    List<T> varList = new ArrayList<>(collectExpr(declVars));

    T set = convertExpr(node.getExpression());
    return bool ->
        factory
            .mkSet(
                obj ->
                    mkExists(
                        varList,
                        factory
                            .mkAnd(
                                factory.mkEq(obj, expr),
                                factory.mkAnd(factory.mkContains( set,expr), bool))),
                getType(expr),
                this);
  }

  // a.auction**
  protected T convertTransClo(ASTOCLTransitiveQualification node) {

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
      } else {
        notFullyImplemented(node);
        assert false;
      }
    } else {
      notFullyImplemented(node);
      assert false;
    }
    if (!node.isTransitive()) {
      return convertExpr(node);
    }

    T auction = convertExpr(fieldAcc.getExpression());

    FuncDecl<BoolSort> rel = buildReflexiveNewAssocFunc(getType(auction), fieldAcc.getName());
    FuncDecl<BoolSort> trans_rel = TransitiveClosure.mkTransitiveClosure(ctx, rel);

    Function<T, T> setFunc =
        obj ->
            factory
                .mkBool((BoolExpr) trans_rel.apply(auction, obj));
    return factory.mkSet(setFunc, getType(auction), this);
  }

  private FuncDecl<BoolSort> buildReflexiveNewAssocFunc(OCLType type, String otherRole) {
    ASTCDType objClass = getASTCDType(type.getName(), getCD());
    ASTCDAssociation association = CDHelper.getAssociation(objClass, otherRole, getCD());
    Sort thisSort = typeConverter.deriveSort(type);
    FuncDecl<BoolSort> rel =
        ctx.mkFuncDecl("reflexive_relation", new Sort[] {thisSort, thisSort}, ctx.mkBoolSort());
    T obj1 = declVariable(type, "obj1");
    T obj2 = declVariable(type, "obj2");
    T rel_is_assocFunc =
       factory.mkForall(
            List.of(obj1, obj2),
            factory
                .mkEq(
                    factory
                        .mkBool((BoolExpr) rel.apply(obj1.expr(), obj2.expr())),
                    factory
                        .mkBool(
                            cd2smtGenerator.evaluateLink(
                                association, objClass, objClass, obj1.expr(), obj2.expr()))));
    genConstraints.add(rel_is_assocFunc);
    return rel;
  }

  protected T createVarFromSymbol(ASTNameExpression node) {
    SymTypeExpression typeExpr = TypeConverter.deriveType(node);
    OCLType type = typeConverter.buildOCLType(typeExpr);
    return declVariable(type, node.getName());
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
    } else if (expr.hasNativeType()) {
      return OCLType.buildOCLType(expr.getKind());
    }
    Log.error("Type not found for the Variable " + expr);
    return null;
  }
}
