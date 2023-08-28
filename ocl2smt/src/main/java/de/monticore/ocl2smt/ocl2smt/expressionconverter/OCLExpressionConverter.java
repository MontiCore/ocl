package de.monticore.ocl2smt.ocl2smt.expressionconverter;

import static de.monticore.cd2smt.Helper.CDHelper.getASTCDType;
import static de.monticore.ocl2smt.helpers.IOHelper.print;
import static de.monticore.ocl2smt.helpers.IOHelper.printPosition;
// TODO: 24.08.23 make contant declaration unique

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
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.ocl2smt.util.SMTSet;
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
public class OCLExpressionConverter extends Expression2smt {

  protected CD2SMTGenerator cd2smtGenerator;

  protected Map<String, Expr<? extends Sort>> varNames;
  protected Map<Expr<? extends Sort>, OCLType> varTypes;

  protected Set<BoolExpr> genConstraints;

  public TypeConverter typeConverter;

  public OCLExpressionConverter(ASTCDCompilationUnit astcdCompilationUnit, Context ctx) {
    this.ctx = ctx;
    genConstraints = new HashSet<>();
    varNames = new HashMap<>();
    varTypes = new HashMap<>();
    cd2smtGenerator = CD2SMTMill.cd2SMTGenerator();

    cd2smtGenerator.cd2smt(astcdCompilationUnit, ctx);
    typeConverter = new TypeConverter(cd2smtGenerator);
  }

  public OCLExpressionConverter(ASTCDCompilationUnit ast, OCL2SMTGenerator ocl2SMTGenerator) {
    this.ctx = ocl2SMTGenerator.getCtx();
    cd2smtGenerator = ocl2SMTGenerator.getCD2SMTGenerator();
    cd2smtGenerator.cd2smt(ast, ctx);
    typeConverter = new TypeConverter(cd2smtGenerator);
  }

  public void reset() {
    varNames.clear();
    genConstraints.clear();
  }

  public ASTCDDefinition getCD() {
    return cd2smtGenerator.getClassDiagram().getCDDefinition();
  }

  public CD2SMTGenerator getCd2smtGenerator() {
    return cd2smtGenerator;
  }

  public Set<BoolExpr> getGenConstraints() {
    return genConstraints;
  }

  public Expr<? extends Sort> declVariable(OCLType type, String name) {
    Expr<? extends Sort> expr = ctx.mkConst(name, typeConverter.deriveSort(type));
    varNames.put(name, expr);
    varTypes.put(expr, type);
    return expr;
  }

  @Override
  protected BoolExpr convert(ASTEqualsExpression node) {
    // case comparison of sets
    if (TypeConverter.hasSetType(node.getRight()) && TypeConverter.hasSetType(node.getLeft())) {
      return convertSet(node.getLeft()).mkSetEq(convertSet(node.getRight()));
    }
    return ctx.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight()));
  }

  @Override
  protected BoolExpr convert(ASTNotEqualsExpression node) {
    // case comparison of sets
    if (TypeConverter.hasSetType(node.getRight()) && TypeConverter.hasSetType(node.getLeft())) {
      return ctx.mkNot(convertSet(node.getLeft()).mkSetEq(convertSet(node.getRight())));
    }
    return ctx.mkNot(ctx.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight())));
  }

  protected Optional<BoolExpr> convertBoolExprOpt(ASTExpression node) {
    BoolExpr result = super.convertBoolExprOpt(node).orElse(null);
    if (result != null) {
      return Optional.of(result);
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
      result = convertMethodCallBool((ASTCallExpression) node);
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

  protected Optional<Expr<? extends Sort>> convertGenExprOpt(ASTExpression node) {

    Optional<Expr<?>> res = Optional.ofNullable(super.convertGenExprOpt(node).orElse(null));
    if (res.isPresent()) {
      return res;
    }

    SymTypeExpression typeExpression = TypeConverter.deriveType(node);
    if ((node instanceof ASTCallExpression) && typeExpression.isObjectType()) {
      res = Optional.ofNullable(convertCallObject((ASTCallExpression) node));
    }
    return res;
  }

  @Override
  protected BoolExpr convertMethodCallBool(ASTCallExpression node) {
    BoolExpr res = null;

    if (node.getExpression() instanceof ASTFieldAccessExpression) {
      ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();
      String methodName = ((ASTFieldAccessExpression) node.getExpression()).getName();
      if (TypeConverter.hasStringType(caller)) {
        res = convertBoolStringOp(caller, node.getArguments().getExpression(0), methodName);
      } else if (TypeConverter.hasDateType(caller)) {
        res = convertBoolDateOp(caller, node.getArguments().getExpression(0), methodName);
      } else if (TypeConverter.hasSetType(caller)) {
        res = convertBoolSetOp(node, methodName);
      } else if (TypeConverter.hasOptionalType(caller)) {
        if (caller instanceof ASTFieldAccessExpression) {
          res = convertBoolOptionalOp((ASTFieldAccessExpression) caller, methodName);
        }
      }
    }
    // TODO: 24.08.23 handle the case of nested call
    return res;
  }

  protected BoolExpr convertBoolSetOp(ASTCallExpression node, String methodName) {
    BoolExpr res = null;
    ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();
    SMTSet set = convertSet(caller);
    ASTExpression arg;
    switch (methodName) {
      case "contains":
        arg = node.getArguments().getExpression(0);
        Expr<? extends Sort> argument = convertExpr(arg);
        res = set.contains(argument);
        break;
      case "containsAll":
        arg = node.getArguments().getExpression(0);
        SMTSet argument2 = convertSet(arg);
        res = set.containsAll(argument2);
        break;

      case "isEmpty":
        res = set.isEmpty(ctx);
        break;
      default:
        notFullyImplemented(node);
    }
    return res;
  }

  protected BoolExpr convertBoolOptionalOp(ASTFieldAccessExpression node, String methodName) {
    BoolExpr res = null;
    Pair<Expr<? extends Sort>, BoolExpr> link = convertFieldAccOptional(node);
    BoolExpr isPresent = mkExists(Set.of(link.getLeft()), link.getRight());
    switch (methodName) {
      case "isPresent":
        res = isPresent;
        break;
      case "isEmpty":
        res = ctx.mkNot(isPresent);
        break;
    }
    return res;
  }

  /*------------------------------------quantified expressions----------------------------------------------------------*/
  private Map<Expr<? extends Sort>, Optional<ASTExpression>> openScope(
      List<ASTInDeclaration> inDeclarations) {
    Map<Expr<? extends Sort>, Optional<ASTExpression>> variableList = new HashMap<>();
    for (ASTInDeclaration decl : inDeclarations) {
      List<Expr<? extends Sort>> temp = convertInDecl(decl);
      if (decl.isPresentExpression()) {
        temp.forEach(t -> variableList.put(t, Optional.of(decl.getExpression())));
      } else {
        temp.forEach(t -> variableList.put(t, Optional.empty()));
      }
    }
    return variableList;
  }

  protected BoolExpr convertInDeclConstraints(
      Map<Expr<? extends Sort>, Optional<ASTExpression>> var) {
    // get all the InPart in InDeclarations
    Map<Expr<? extends Sort>, ASTExpression> inParts = new HashMap<>();
    var.forEach((key, value) -> value.ifPresent(s -> inParts.put(key, s)));

    List<BoolExpr> constraintList = new ArrayList<>();

    for (Map.Entry<Expr<? extends Sort>, ASTExpression> expr : inParts.entrySet()) {
      SMTSet mySet = convertSet(expr.getValue());
      constraintList.add(mySet.contains(expr.getKey()));
    }
    BoolExpr result = ctx.mkTrue();

    for (BoolExpr constr : constraintList) {
      result = ctx.mkAnd(result, constr);
    }
    return result;
  }

  protected BoolExpr convertForAll(ASTForallExpression node) {
    // declare Variable from scope
    Map<Expr<? extends Sort>, Optional<ASTExpression>> var = openScope(node.getInDeclarationList());

    BoolExpr constraint = convertInDeclConstraints(var);

    BoolExpr result =
        mkForall(var.keySet(), ctx.mkImplies(constraint, convertBoolExpr(node.getExpression())));

    // Delete Variables from "scope"
    closeScope(node.getInDeclarationList());

    return result;
  }

  protected BoolExpr convertExist(ASTExistsExpression node) {
    // declare Variable from scope
    Map<Expr<? extends Sort>, Optional<ASTExpression>> var = openScope(node.getInDeclarationList());

    BoolExpr constraint = convertInDeclConstraints(var);

    BoolExpr result =
        mkExists(var.keySet(), ctx.mkAnd(constraint, convertBoolExpr(node.getExpression())));

    // Delete Variables from "scope"
    closeScope(node.getInDeclarationList());

    return result;
  }

  /*----------------------------------control expressions----------------------------------------------------------*/
  protected Expr<? extends Sort> convert(ASTIfThenElseExpression node) {
    return ctx.mkITE(
        convertBoolExpr(node.getCondition()),
        convertExpr(node.getThenExpression()),
        convertExpr(node.getElseExpression()));
  }

  // -----------------------------------general----------------------------------------------------------------------*/
  @Override
  protected Expr<? extends Sort> convert(ASTNameExpression node) {
    Expr<? extends Sort> res;
    if (varNames.containsKey(node.getName())) {
      res = varNames.get(node.getName());
    } else {
      res = createVarFromSymbol(node);
    }
    return res;
  }

  @Override
  protected Expr<? extends Sort> convertCallObject(ASTCallExpression node) {
    Expr<?> expr = super.convertCallObject(node);
    if (expr != null) {
      return expr;
    }
    String methodName = ((ASTFieldAccessExpression) node.getExpression()).getName();
    ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();

    if (TypeConverter.hasOptionalType(caller) && methodName.equals("get")) {
      // TODO: 28.08.2023  fixme
      Pair<Expr<? extends Sort>, BoolExpr> res =
          convertFieldAccOptional((ASTFieldAccessExpression) caller);
      genConstraints.add(res.getRight());
      return res.getLeft();
    }

    return null;
  }

  // ========================================ASTFieldAccessExpressions===================================================

  /** convert a field access expression when the result produces a set */
  protected SMTSet convertFieldAccessSet(ASTFieldAccessExpression node) {
    SymTypeExpression elementType = TypeConverter.deriveType(node);
    return convertFieldAccessSetHelper(node.getExpression(), node.getName());
  }

  protected SMTSet convertFieldAccessSetHelper(ASTExpression node, String name) {

    // case we have simple field access. e.g. node = p, name = auction
    if (!(node instanceof ASTFieldAccessExpression)) {
      Expr<? extends Sort> expr = convertExpr(node);
      return convertSimpleFieldAccessSet(expr, name);
    }

    // case we have nested field access. e.g. node = p.auction, name = person
    SMTSet leftSet = convertSet(node);

    // computes the type of the element of the right Set
    OCLType type1 = leftSet.getType();
    ASTCDAssociation person_parent = OCLHelper.getAssociation(type1, name, getCD());
    OCLType rightSetElemType = OCLHelper.getOtherType(person_parent, type1, name, getCD());

    Function<Expr<? extends Sort>, SMTSet> function =
        obj1 ->
            new SMTSet(
                obj2 ->
                    OCLHelper.evaluateLink(
                        person_parent, obj1, name, obj2, cd2smtGenerator, this::getType),
                rightSetElemType,
                this);

    return leftSet.collectAll(function);
  }

  protected Pair<Expr<? extends Sort>, BoolExpr> convertFieldAccessSetHelper(
      Expr<? extends Sort> obj, String name) {
    OCLType type = getType(obj);
    ASTCDDefinition cd = cd2smtGenerator.getClassDiagram().getCDDefinition();
    Pair<Expr<? extends Sort>, BoolExpr> res;
    ASTCDType astcdType = CDHelper.getASTCDType(type.getName(), cd);
    if (OCLHelper.containsAttribute(astcdType, name, cd)) { // case obj.attribute
      res =
          new ImmutablePair<>(
              OCLHelper.getAttribute(obj, type, name, cd2smtGenerator), ctx.mkTrue());
    } else { // case obj.link
      res = convertFieldAccessAssocHelper(obj, name);
    }

    return res;
  }

  private Pair<Expr<? extends Sort>, BoolExpr> convertFieldAccessAssocHelper(
      Expr<? extends Sort> obj, String role) {
    OCLType type = getType(obj);
    ASTCDAssociation association = OCLHelper.getAssociation(type, role, getCD());

    OCLType type2 = OCLHelper.getOtherType(association, type, role, getCD());
    String resName = obj.getSExpr() + SMTHelper.fCharToLowerCase(type2.getName());
    Expr<? extends Sort> link = declVariable(type2, resName);
    BoolExpr linkConstraint =
        OCLHelper.evaluateLink(association, obj, role, link, cd2smtGenerator, this::getType);
    return new ImmutablePair<>(link, linkConstraint);
  }

  protected Expr<? extends Sort> convert(ASTFieldAccessExpression node) {
    Expr<? extends Sort> obj = convertExpr(node.getExpression());
    Pair<Expr<? extends Sort>, BoolExpr> res = convertFieldAccessSetHelper(obj, node.getName());
    if (!TypeConverter.hasOptionalType(node)) {
      genConstraints.add(res.getRight());
    }
    return res.getLeft();
  }

  protected Pair<Expr<? extends Sort>, BoolExpr> convertFieldAccOptional(
      ASTFieldAccessExpression node) {
    Expr<? extends Sort> obj = convertExpr(node.getExpression());
    return convertFieldAccessSetHelper(obj, node.getName());
  }

  protected SMTSet convertSimpleFieldAccessSet(Expr<? extends Sort> obj, String role) {
    OCLType type1 = getType(obj);
    ASTCDAssociation association = OCLHelper.getAssociation(type1, role, getCD());
    OCLType type2 = OCLHelper.getOtherType(association, type1, role, getCD());

    Function<Expr<? extends Sort>, BoolExpr> auction_per_set =
        per -> OCLHelper.evaluateLink(association, obj, role, per, cd2smtGenerator, this::getType);

    return new SMTSet(auction_per_set, type2, this);
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

  protected List<Expr<? extends Sort>> convertInDecl(ASTInDeclaration node) {
    List<Expr<? extends Sort>> result = new ArrayList<>();
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
  protected BoolExpr convertSetIn(ASTSetInExpression node) {
    return convertSet(node.getSet()).contains(convertExpr(node.getElem()));
  }

  protected BoolExpr convertSetNotIn(ASTSetNotInExpression node) {
    return ctx.mkNot(convertSet(node.getSet()).contains(convertExpr(node.getElem())));
  }

  public SMTSet convertSet(ASTExpression node) {
    Optional<SMTSet> res = convertSetOpt(node);
    if (res.isPresent()) {
      return res.get();
    } else {
      Log.error("conversion of Set of the type " + node.getClass().getName() + " not implemented");
    }
    return null;
  }

  protected Optional<SMTSet> convertSetOpt(ASTExpression node) {
    SMTSet set = null;
    if (node instanceof ASTFieldAccessExpression) {
      set = convertFieldAccessSet((ASTFieldAccessExpression) node);
    } else if (node instanceof ASTBracketExpression) {
      set = convertSet(((ASTBracketExpression) node).getExpression());
    } else if (node instanceof ASTOCLTransitiveQualification) {
      set = convertTransClo((ASTOCLTransitiveQualification) node);
    } else if (node instanceof ASTUnionExpression) {
      set = convertSetUnion((ASTUnionExpression) node);
    } else if (node instanceof ASTIntersectionExpression) {
      set = convertSetInter((ASTIntersectionExpression) node);
    } else if (node instanceof ASTSetMinusExpression) {
      set = convertSetMinus((ASTSetMinusExpression) node);

    } else if (node instanceof ASTSetComprehension) {
      set = convertSetComp((ASTSetComprehension) node);
    } else if (node instanceof ASTSetEnumeration) {
      set = convertSetEnum((ASTSetEnumeration) node);
    }
    return Optional.ofNullable(set);
  }

  protected SMTSet convertSetUnion(ASTUnionExpression node) {
    return convertSet(node.getLeft()).mkSetUnion(convertSet(node.getRight()));
  }

  protected SMTSet convertSetMinus(ASTSetMinusExpression node) {
    return convertSet(node.getLeft()).mkSetMinus(convertSet(node.getRight()));
  }

  protected SMTSet convertSetInter(ASTIntersectionExpression node) {
    return convertSet(node.getLeft()).mkSetIntersect(convertSet(node.getRight()));
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

  protected SMTSet convertSetComp(ASTSetComprehension node) {
    Set<String> setCompVarNames = openSetCompScope(node);

    Function<BoolExpr, SMTSet> setComp = convertSetCompLeftSide(node.getLeft(), setCompVarNames);
    BoolExpr filter = convertSetCompRightSide(node.getSetComprehensionItemList());

    closeSetCompScope(setCompVarNames);
    return setComp.apply(filter);
  }

  private void closeSetCompScope(Set<String> setCompVarNames) {
    for (String name : setCompVarNames) {
      varNames.remove(name);
    }
  }

  protected SMTSet convertSetEnum(ASTSetEnumeration node) {
    Sort sort = null;
    List<Expr<? extends Sort>> setItemValues = new ArrayList<>();
    List<Function<ArithExpr<? extends Sort>, BoolExpr>> rangeFilters = new ArrayList<>();

    for (ASTSetCollectionItem item : node.getSetCollectionItemList()) {

      if (item instanceof ASTSetValueItem) {
        setItemValues.add(convertExpr(((ASTSetValueItem) item).getExpression()));
        sort = setItemValues.get(0).getSort();
      } else if (item instanceof ASTSetValueRange) {
        Pair<Function<ArithExpr<? extends Sort>, BoolExpr>, Sort> range =
            convertSetValRang((ASTSetValueRange) item);
        rangeFilters.add(range.getLeft());
        sort = range.getRight();
      }
    }
    assert sort != null;
    SMTSet set =
        new SMTSet(obj -> ctx.mkFalse(), OCLType.buildOCLType(sort.getName().toString()), this);
    SMTSet set1 = set;
    if (!setItemValues.isEmpty()) {
      set =
          new SMTSet(
              obj -> ctx.mkOr(set1.contains(obj), addValuesToSetEnum(setItemValues, obj)),
              OCLType.buildOCLType(sort.getName().toString()),
              this);
    }

    for (Function<ArithExpr<? extends Sort>, BoolExpr> range : rangeFilters) {
      // TODO:: fix the Warning
      SMTSet set2 = set;
      set =
          new SMTSet(
              obj -> ctx.mkOr(set2.contains(obj), range.apply((ArithExpr<? extends Sort>) obj)),
              OCLType.buildOCLType(sort.getName().toString()),
              this);
    }
    return set;
  }

  private Pair<Function<ArithExpr<? extends Sort>, BoolExpr>, Sort> convertSetValRang(
      ASTSetValueRange node) {
    ArithExpr<? extends Sort> expr1 = convertExprArith(node.getUpperBound());
    ArithExpr<? extends Sort> expr2 = convertExprArith(node.getLowerBound());
    ArithExpr<? extends Sort> low =
        (ArithExpr<? extends Sort>) ctx.mkITE(ctx.mkLt(expr1, expr2), expr1, expr2);
    ArithExpr<? extends Sort> up =
        (ArithExpr<? extends Sort>) ctx.mkITE(ctx.mkLt(expr1, expr2), expr2, expr1);
    return new ImmutablePair<>(
        obj -> ctx.mkAnd(ctx.mkLe(low, obj), ctx.mkLe(obj, up)), low.getSort());
  }

  BoolExpr addValuesToSetEnum(List<Expr<? extends Sort>> elements, Expr<? extends Sort> value) {
    BoolExpr res = ctx.mkFalse();
    for (Expr<? extends Sort> setElem : elements) {
      res = ctx.mkOr(res, ctx.mkEq(value, setElem));
    }
    return res;
  }

  protected Function<BoolExpr, SMTSet> convertSetCompLeftSide(
      ASTSetComprehensionItem node, Set<String> declVars) {
    Function<BoolExpr, SMTSet> res = null;
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
  protected BoolExpr convertSetCompRightSide(List<ASTSetComprehensionItem> filters) {
    BoolExpr filter = ctx.mkTrue();
    // example: x isin {2,3}
    for (ASTSetComprehensionItem item : filters) {

      if (item.isPresentGeneratorDeclaration()) {
        SMTSet set = convertSet(item.getGeneratorDeclaration().getExpression());
        BoolExpr subFilter = set.contains(varNames.get(item.getGeneratorDeclaration().getName()));
        filter = ctx.mkAnd(filter, subFilter);
      }

      // example: x == y
      if (item.isPresentExpression()) {
        filter = ctx.mkAnd(filter, convertBoolExpr(item.getExpression()));
      }

      // example: int i = 10
      if (item.isPresentSetVariableDeclaration()) {
        ASTSetVariableDeclaration var = item.getSetVariableDeclaration();
        if (var.isPresentExpression()) {
          BoolExpr subFilter =
              ctx.mkEq(varNames.get(var.getName()), convertExpr(var.getExpression()));
          filter = ctx.mkAnd(filter, subFilter);
        }
      }
    }
    return filter;
  }

  protected Function<BoolExpr, SMTSet> convertSetCompExprLeft(
      ASTExpression node, Set<String> declVars) {
    Expr<? extends Sort> expr1 = convertExpr(node);
    // define a const  for the quantifier
    Expr<? extends Sort> expr2 = ctx.mkConst("var", expr1.getSort());
    Set<Expr<? extends Sort>> vars = collectExpr(declVars);
    vars.add(expr2);

    return bool ->
        new SMTSet(
            obj -> mkExists(vars, ctx.mkAnd(ctx.mkEq(obj, expr2), ctx.mkEq(expr2, expr1), bool)),
            getType(expr1),
            this);
  }

  /***
   * will be transformed to a function that take a bool expression and return
   * and SMTSet because the filter of the function is  on the right side and
   * this function just convert the left Side.
   * *
   * example: {int z = x*x|...}
   * *
   */
  protected Function<BoolExpr, SMTSet> convertSetVarDeclLeft(
      ASTSetVariableDeclaration node, Set<String> declVars) {
    Expr<?> var = varNames.get(node.getName());
    Set<Expr<?>> varSet = collectExpr(declVars);

    // check if the initial value of the var  is present and convert it to a constraint
    BoolExpr constr =
        node.isPresentExpression()
            ? ctx.mkEq(var, convertExpr(node.getExpression()))
            : ctx.mkTrue();

    // create the function bool ->SMTSet
    return bool ->
        new SMTSet(
            obj -> mkExists(varSet, ctx.mkAnd(ctx.mkEq(obj, var), constr, bool)),
            getType(var),
            this);
  }

  private Set<Expr<?>> collectExpr(Set<String> exprSet) {
    return exprSet.stream().map(expr -> varNames.get(expr)).collect(Collectors.toSet());
  }

  protected Function<BoolExpr, SMTSet> convertGenDeclLeft(
      ASTGeneratorDeclaration node, Set<String> declVars) {

    Expr<? extends Sort> expr = varNames.get(node.getName());
    Set<Expr<?>> exprSet = collectExpr(declVars);
    exprSet.add(expr);

    SMTSet set = convertSet(node.getExpression());
    return bool ->
        new SMTSet(
            obj -> mkExists(exprSet, ctx.mkAnd(ctx.mkEq(obj, expr), set.contains(expr), bool)),
            getType(expr),
            this);
  }

  // a.auction**
  protected SMTSet convertTransClo(ASTOCLTransitiveQualification node) {

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
      return convertSet(node);
    }

    Expr<? extends Sort> auction = convertExpr(fieldAcc.getExpression());

    FuncDecl<BoolSort> rel = buildReflexiveNewAssocFunc(getType(auction), fieldAcc.getName());
    FuncDecl<BoolSort> trans_rel = TransitiveClosure.mkTransitiveClosure(ctx, rel);

    Function<Expr<? extends Sort>, BoolExpr> setFunc =
        obj -> (BoolExpr) trans_rel.apply(auction, obj);
    return new SMTSet(setFunc, getType(auction), this);
  }

  private FuncDecl<BoolSort> buildReflexiveNewAssocFunc(OCLType type, String otherRole) {
    ASTCDType objClass = getASTCDType(type.getName(), getCD());
    ASTCDAssociation association = CDHelper.getAssociation(objClass, otherRole, getCD());
    Sort thisSort = typeConverter.deriveSort(type);
    FuncDecl<BoolSort> rel =
        ctx.mkFuncDecl("reflexive_relation", new Sort[] {thisSort, thisSort}, ctx.mkBoolSort());
    Expr<? extends Sort> obj1 = declVariable(type, "obj1");
    Expr<? extends Sort> obj2 = declVariable(type, "obj2");
    BoolExpr rel_is_assocFunc =
        mkForall(
            Set.of(obj1, obj2),
            ctx.mkEq(
                rel.apply(obj1, obj2),
                cd2smtGenerator.evaluateLink(association, objClass, objClass, obj1, obj2)));
    genConstraints.add(rel_is_assocFunc);
    return rel;
  }

  protected Expr<? extends Sort> createVarFromSymbol(ASTNameExpression node) {
    SymTypeExpression typeExpr = TypeConverter.deriveType(node);
    OCLType type = typeConverter.buildOCLType(typeExpr);
    return declVariable(type, node.getName());
  }

  private void notFullyImplemented(ASTExpression node) {
    Log.error("conversion of Set of the type " + node.getClass().getName() + " not implemented");
  }

  public BoolExpr mkForall(Set<Expr<?>> vars, BoolExpr body) {

    return ctx.mkForall(
        vars.toArray(new Expr[0]),
        ctx.mkImplies(filterObjects(vars), body),
        0,
        null,
        null,
        null,
        null);
  }

  public BoolExpr filterObjects(Set<Expr<?>> vars) {
    BoolExpr filter = ctx.mkTrue();
    for (Expr<?> entry : vars) {

      if (!TypeConverter.hasSimpleType(getType(entry).getName())) {
        ASTCDType type =
            CDHelper.getASTCDType(
                getType(entry).getName(), cd2smtGenerator.getClassDiagram().getCDDefinition());
        filter = ctx.mkAnd(filter, cd2smtGenerator.filterObject(entry, type));
      }
    }
    return filter;
  }

  public BoolExpr mkExists(Set<Expr<?>> vars, BoolExpr body) {
    return ctx.mkExists(
        vars.toArray(new Expr[0]), ctx.mkAnd(filterObjects(vars), body), 0, null, null, null, null);
  }

  public OCLType getType(Expr<? extends Sort> expr) {
    if (varTypes.containsKey(expr)) {
      return varTypes.get(expr);
    } else if (TypeConverter.hasSimpleType(expr.getSort())) {
      return OCLType.buildOCLType(expr.getSort().toString());
    }
    Log.error("Type not found for the Variable " + expr);
    return null;
  }
}
