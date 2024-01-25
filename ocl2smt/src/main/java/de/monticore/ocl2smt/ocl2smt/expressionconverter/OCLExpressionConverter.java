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
public class OCLExpressionConverter extends Expression2smt {

  protected CD2SMTGenerator cd2smtGenerator;

  protected Map<String, ExprBuilder> varNames;
  protected Map<ExprBuilder, OCLType> varTypes;

  protected Set<ExprBuilder> genConstraints;

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

  public Set<ExprBuilder> getGenConstraints() {
    return genConstraints;
  }

  public ExprBuilder declVariable(OCLType type, String name) {
    ExprBuilder expr = ExprMill.exprBuilder(ctx).mkExpr(name, typeConverter.deriveSort(type));
    varNames.put(name, expr);
    varTypes.put(expr, type);
    return expr;
  }

  @Override
  public ExprBuilder convertExpr(ASTExpression node) {
    ExprBuilder result = null;
    SymTypeExpression typeExpression = TypeConverter.deriveType(node);
    if ((node instanceof ASTCallExpression) && typeExpression.isObjectType()) {
      result = convertCallObject((ASTCallExpression) node);
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
      result = convertCallBool((ASTCallExpression) node);
    }
    if (result != null && result.getKind() != ExpressionKind.NULL) {
      return result;
    }

    return super.convertExpr(node);
  }

  @Override
  protected ExprBuilder convertCallBool(ASTCallExpression node) {
    ExprBuilder res = null;

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

  protected ExprBuilder convertBoolSetOp(ASTCallExpression node, String methodName) {
    ExprBuilder res = null;
    ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();
    Z3SetBuilder set = convertSet(caller);
    ASTExpression arg;
    switch (methodName) {
      case "contains":
        arg = node.getArguments().getExpression(0);
        ExprBuilder argument = convertExpr(arg);
        res = set.contains(argument);
        break;
      case "containsAll":
        arg = node.getArguments().getExpression(0);
        Z3SetBuilder argument2 = convertSet(arg);
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

  protected ExprBuilder convertBoolOptionalOp(ASTFieldAccessExpression node, String methodName) {
    ExprBuilder res = null;
    Pair<ExprBuilder, ExprBuilder> link = convertFieldAccOptional(node);
    ExprBuilder isPresent = mkExists(List.of(link.getLeft()), link.getRight());
    switch (methodName) {
      case "isPresent":
        res = isPresent;
        break;
      case "isEmpty":
        res = ExprMill.exprBuilder(ctx).mkNot(isPresent);
        break;
    }
    return res;
  }

  /*------------------------------------quantified expressions----------------------------------------------------------*/
  private Map<ExprBuilder, Optional<ASTExpression>> openScope(
      List<ASTInDeclaration> inDeclarations) {
    Map<ExprBuilder, Optional<ASTExpression>> variableList = new HashMap<>();
    for (ASTInDeclaration decl : inDeclarations) {
      List<ExprBuilder> temp = convertInDecl(decl);
      if (decl.isPresentExpression()) {
        temp.forEach(t -> variableList.put(t, Optional.of(decl.getExpression())));
      } else {
        temp.forEach(t -> variableList.put(t, Optional.empty()));
      }
    }
    return variableList;
  }

  protected ExprBuilder convertInDeclConstraints(Map<ExprBuilder, Optional<ASTExpression>> var) {
    // get all the InPart in InDeclarations
    Map<ExprBuilder, ASTExpression> inParts = new HashMap<>();
    var.forEach((key, value) -> value.ifPresent(s -> inParts.put(key, s)));

    List<ExprBuilder> constraintList = new ArrayList<>();

    for (Map.Entry<ExprBuilder, ASTExpression> expr : inParts.entrySet()) {
      Z3SetBuilder mySet = convertSet(expr.getValue());
      constraintList.add(mySet.contains(expr.getKey()));
    }
    ExprBuilder result = ExprMill.exprBuilder(ctx).mkBool(true);

    for (ExprBuilder constr : constraintList) {
      result = ExprMill.exprBuilder(ctx).mkAnd(result, constr);
    }
    return result;
  }

  protected ExprBuilder convertForAll(ASTForallExpression node) {
    // declare Variable from scope
    Map<ExprBuilder, Optional<ASTExpression>> var = openScope(node.getInDeclarationList());

    ExprBuilder constraint = convertInDeclConstraints(var);

    ExprBuilder result =
        mkForall(
            new ArrayList<>(var.keySet()),
            ExprMill.exprBuilder(ctx).mkImplies(constraint, convertExpr(node.getExpression())));

    // Delete Variables from "scope"
    closeScope(node.getInDeclarationList());

    return result;
  }

  protected ExprBuilder convertExist(ASTExistsExpression node) {
    // declare Variable from scope
    Map<ExprBuilder, Optional<ASTExpression>> var = openScope(node.getInDeclarationList());

    ExprBuilder constraint = convertInDeclConstraints(var);

    ExprBuilder result =
        mkExists(
            new ArrayList<>(var.keySet()),
            ExprMill.exprBuilder(ctx).mkAnd(constraint, convertExpr(node.getExpression())));

    // Delete Variables from "scope"
    closeScope(node.getInDeclarationList());

    return result;
  }

  // -----------------------------------general----------------------------------------------------------------------*/
  @Override
  protected ExprBuilder convert(ASTNameExpression node) {
    ExprBuilder res;
    if (varNames.containsKey(node.getName())) {
      res = varNames.get(node.getName());
    } else {
      res = createVarFromSymbol(node);
    }
    return res;
  }

  @Override
  protected ExprBuilder convertCallObject(ASTCallExpression node) {
    ExprBuilder expr = super.convertCallObject(node);
    if (expr != null) {
      return expr;
    }

    if (node.getExpression() instanceof ASTFieldAccessExpression) {
      String methodName = ((ASTFieldAccessExpression) node.getExpression()).getName();
      ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();

      if (TypeConverter.hasOptionalType(caller) && methodName.equals("get")) {
        // TODO: 28.08.2023  fixme
        Pair<ExprBuilder, ExprBuilder> res =
            convertFieldAccOptional((ASTFieldAccessExpression) caller);
        genConstraints.add(res.getRight());
        return res.getLeft();
      }
    }
    return null;
  }

  // ========================================ASTFieldAccessExpressions===================================================

  /** convert a field access expression when the result produces a set */
  protected Z3SetBuilder convertFieldAccessSet(ASTFieldAccessExpression node) {
    SymTypeExpression elementType = TypeConverter.deriveType(node);
    return convertFieldAccessSetHelper(node.getExpression(), node.getName());
  }

  protected Z3SetBuilder convertFieldAccessSetHelper(ASTExpression node, String name) {

    // case we have simple field access. e.g. node = p, name = auction
    /*  if (!(node instanceof ASTFieldAccessExpression)) {
      ExprBuilder expr = convertExpr(node);
      return convertSimpleFieldAccessSet(expr, name);
    }

    // case we have nested field access. e.g. node = p.auction, name = person
    Z3SetBuilder leftSet = convertSet(node);

    // computes the type of the element of the right Set
    OCLType type1 = leftSet.getType();
    ASTCDAssociation person_parent = OCLHelper.getAssociation(type1, name, getCD());
    OCLType rightSetElemType = OCLHelper.getOtherType(person_parent, type1, name, getCD());

    Function<ExprBuilder, Z3SetBuilder> function =
        obj1 ->
            new Z3SetBuilder(
                obj2 ->
                    OCLHelper.evaluateLink(
                        person_parent, obj1.expr(), name, obj2.expr(), cd2smtGenerator, this::getType),
                rightSetElemType,
                this);

    return leftSet.collectAll(function);*/

    return null;
  }

  protected Pair<ExprBuilder, ExprBuilder> convertFieldAccessSetHelper(
      ExprBuilder obj, String name) {
    ExprBuilder res = ExprMill.exprBuilder(ctx);
    /* OCLType type = getType(obj);
    ASTCDDefinition cd = cd2smtGenerator.getClassDiagram().getCDDefinition();
    Pair<ExprBuilder, ExprBuilder> res;
    ASTCDType astcdType = getASTCDType(type.getName(), cd);

    if (OCLHelper.containsAttribute(astcdType, name, cd)) { // case obj.attribute
      res =
          new ImmutablePair<>(
              OCLHelper.getAttribute(obj.expr(), type, name, cd2smtGenerator), ctx.mkTrue());
    } else { // case obj.link
      res = convertFieldAccessAssocHelper(obj, name);
    }*/

    return new ImmutablePair<>(null, null);
  }

  private Pair<ExprBuilder, ExprBuilder> convertFieldAccessAssocHelper(
      ExprBuilder obj, String role) {
    OCLType type = getType(obj);
    ASTCDAssociation association = OCLHelper.getAssociation(type, role, getCD());

    OCLType type2 = OCLHelper.getOtherType(association, type, role, getCD());
    String resName = obj.expr().toString() + SMTHelper.fCharToLowerCase(type2.getName());
    ExprBuilder link = declVariable(type2, resName);
    ExprBuilder linkConstraint =
        ExprMill.exprBuilder(ctx)
            .mkBool(
                OCLHelper.evaluateLink(
                    association, obj, role, link, cd2smtGenerator, this::getType));
    return new ImmutablePair<>(link, linkConstraint);
  }

  protected ExprBuilder convert(ASTFieldAccessExpression node) {
    ExprBuilder obj = convertExpr(node.getExpression());
    Pair<ExprBuilder, ExprBuilder> res = convertFieldAccessSetHelper(obj, node.getName());
    if (!TypeConverter.hasOptionalType(node)) {
      genConstraints.add(res.getRight());
    }
    return res.getLeft();
  }

  protected Pair<ExprBuilder, ExprBuilder> convertFieldAccOptional(ASTFieldAccessExpression node) {
    ExprBuilder obj = convertExpr(node.getExpression());
    return convertFieldAccessSetHelper(obj, node.getName());
  }

  protected Z3SetBuilder convertSimpleFieldAccessSet(ExprBuilder obj, String role) {
    OCLType type1 = getType(obj);
    ASTCDAssociation association = OCLHelper.getAssociation(type1, role, getCD());
    OCLType type2 = OCLHelper.getOtherType(association, type1, role, getCD());

    Function<ExprBuilder, ExprBuilder> auction_per_set =
        per ->
            ExprMill.exprBuilder(ctx)
                .mkBool(
                    OCLHelper.evaluateLink(
                        association, obj, role, per, cd2smtGenerator, this::getType));

    return new Z3SetBuilder(auction_per_set, type2, this);
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

  protected List<ExprBuilder> convertInDecl(ASTInDeclaration node) {
    List<ExprBuilder> result = new ArrayList<>();
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
  protected ExprBuilder convertSetIn(ASTSetInExpression node) {
    return convertSet(node.getSet()).contains(convertExpr(node.getElem()));
  }

  protected ExprBuilder convertSetNotIn(ASTSetNotInExpression node) {
    return ExprMill.exprBuilder(ctx)
        .mkNot(convertSet(node.getSet()).contains(convertExpr(node.getElem())));
  }

  public Z3SetBuilder convertSet(ASTExpression node) {
    Optional<Z3SetBuilder> res = convertSetOpt(node);
    if (res.isPresent()) {
      return res.get();
    } else {
      Log.error("conversion of Set of the type " + node.getClass().getName() + " not implemented");
    }
    return null;
  }

  protected Optional<Z3SetBuilder> convertSetOpt(ASTExpression node) {
    Z3SetBuilder set = null;
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

  protected Z3SetBuilder convertSetUnion(ASTUnionExpression node) {
    return convertSet(node.getLeft()).mkSetUnion(convertSet(node.getRight()));
  }

  protected Z3SetBuilder convertSetMinus(ASTSetMinusExpression node) {
    return convertSet(node.getLeft()).mkSetMinus(convertSet(node.getRight()));
  }

  protected Z3SetBuilder convertSetInter(ASTIntersectionExpression node) {
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

  protected Z3SetBuilder convertSetComp(ASTSetComprehension node) {
    Set<String> setCompVarNames = openSetCompScope(node);

    Function<ExprBuilder, Z3SetBuilder> setComp =
        convertSetCompLeftSide(node.getLeft(), setCompVarNames);
    ExprBuilder filter = convertSetCompRightSide(node.getSetComprehensionItemList());

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
  protected Z3SetBuilder convertSetEnum(ASTSetEnumeration node) {
    ExpressionKind kind = null;
    List<ExprBuilder> setItemValues = new ArrayList<>();
    List<Function<ExprBuilder, ExprBuilder>> rangeFilters = new ArrayList<>();

    for (ASTSetCollectionItem item : node.getSetCollectionItemList()) {

      if (item instanceof ASTSetValueItem) {
        setItemValues.add(convertExpr(((ASTSetValueItem) item).getExpression()));
        kind = setItemValues.get(0).getKind();
      } else if (item instanceof ASTSetValueRange) {
        Pair<Function<ExprBuilder, ExprBuilder>, ExpressionKind> range =
            convertSetValRang((ASTSetValueRange) item);
        rangeFilters.add(range.getLeft());
        kind = range.getRight();
      }
    }
    assert kind != null;
    Z3SetBuilder set =
        new Z3SetBuilder(
            obj -> ExprMill.exprBuilder(ctx).mkBool(false), OCLType.buildOCLType(kind), this);
    Z3SetBuilder set1 = set;
    if (!setItemValues.isEmpty()) {
      set =
          new Z3SetBuilder(
              obj ->
                  ExprMill.exprBuilder(ctx)
                      .mkOr(set1.contains(obj), addValuesToSetEnum(setItemValues, obj)),
              OCLType.buildOCLType(kind),
              this);
    }

    for (Function<ExprBuilder, ExprBuilder> range : rangeFilters) {

      Z3SetBuilder set2 = set;
      set =
          new Z3SetBuilder(
              obj -> ExprMill.exprBuilder(ctx).mkOr(set2.contains(obj), range.apply(obj)),
              OCLType.buildOCLType(kind),
              this);
    }
    return set;
  }

  private Pair<Function<ExprBuilder, ExprBuilder>, ExpressionKind> convertSetValRang(
      ASTSetValueRange node) {
    ExprBuilder expr1 = convertExpr(node.getUpperBound());
    ExprBuilder expr2 = convertExpr(node.getLowerBound());
    ExprBuilder low =
        ExprMill.exprBuilder(ctx).mkIte(ExprMill.exprBuilder(ctx).mkLt(expr1, expr2), expr1, expr2);
    ExprBuilder up =
        ExprMill.exprBuilder(ctx).mkIte(ExprMill.exprBuilder(ctx).mkLt(expr1, expr2), expr2, expr1);
    return new ImmutablePair<>(
        obj ->
            ExprMill.exprBuilder(ctx)
                .mkAnd(
                    ExprMill.exprBuilder(ctx).mkLeq(low, obj),
                    ExprMill.exprBuilder(ctx).mkLeq(obj, up)),
        low.getKind());
  }

  ExprBuilder addValuesToSetEnum(List<ExprBuilder> elements, ExprBuilder value) {
    ExprBuilder res = ExprMill.exprBuilder(ctx).mkBool(false);
    for (ExprBuilder setElem : elements) {
      res = ExprMill.exprBuilder(ctx).mkOr(res, ExprMill.exprBuilder(ctx).mkEq(value, setElem));
    }
    return res;
  }

  protected Function<ExprBuilder, Z3SetBuilder> convertSetCompLeftSide(
      ASTSetComprehensionItem node, Set<String> declVars) {
    Function<ExprBuilder, Z3SetBuilder> res = null;
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
  protected ExprBuilder convertSetCompRightSide(List<ASTSetComprehensionItem> filters) {
    ExprBuilder filter = ExprMill.exprBuilder(ctx).mkBool(true);
    // example: x isin {2,3}
    for (ASTSetComprehensionItem item : filters) {

      if (item.isPresentGeneratorDeclaration()) {
        Z3SetBuilder set = convertSet(item.getGeneratorDeclaration().getExpression());
        ExprBuilder subFilter =
            set.contains(varNames.get(item.getGeneratorDeclaration().getName()));
        filter = ExprMill.exprBuilder(ctx).mkAnd(filter, subFilter);
      }

      // example: x == y
      if (item.isPresentExpression()) {
        filter = ExprMill.exprBuilder(ctx).mkAnd(filter, convertExpr(item.getExpression()));
      }

      // example: int i = 10
      if (item.isPresentSetVariableDeclaration()) {
        ASTSetVariableDeclaration var = item.getSetVariableDeclaration();
        if (var.isPresentExpression()) {
          ExprBuilder subFilter =
              ExprMill.exprBuilder(ctx)
                  .mkEq(varNames.get(var.getName()), convertExpr(var.getExpression()));
          filter = ExprMill.exprBuilder(ctx).mkAnd(filter, subFilter);
        }
      }
    }
    return filter;
  }

  protected Function<ExprBuilder, Z3SetBuilder> convertSetCompExprLeft(
      ASTExpression node, Set<String> declVars) {
    ExprBuilder expr1 = convertExpr(node);
    // define a const  for the quantifier
    ExprBuilder expr2 = ExprMill.exprBuilder(ctx).mkExpr("var", expr1.sort());
    List<ExprBuilder> vars = new ArrayList<>(collectExpr(declVars));
    vars.add(expr2);

    return bool ->
        new Z3SetBuilder(
            obj ->
                mkExists(
                    vars,
                    ExprMill.exprBuilder(ctx)
                        .mkAnd(
                            ExprMill.exprBuilder(ctx).mkEq(obj, expr2),
                            ExprMill.exprBuilder(ctx)
                                .mkAnd(ExprMill.exprBuilder(ctx).mkEq(expr2, expr1), bool))),
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
  protected Function<ExprBuilder, Z3SetBuilder> convertSetVarDeclLeft(
      ASTSetVariableDeclaration node, Set<String> declVars) {
    ExprBuilder var = varNames.get(node.getName());
    List<ExprBuilder> varList = new ArrayList<>(collectExpr(declVars));

    // check if the initial value of the var is present and convert it to a constraint
    ExprBuilder constr =
        node.isPresentExpression()
            ? ExprMill.exprBuilder(ctx).mkEq(var, convertExpr(node.getExpression()))
            : ExprMill.exprBuilder(ctx).mkBool(true);

    // create the function bool ->SMTSet
    return bool ->
        new Z3SetBuilder(
            obj ->
                mkExists(
                    varList,
                    ExprMill.exprBuilder(ctx)
                        .mkAnd(
                            ExprMill.exprBuilder(ctx).mkEq(obj, var),
                            ExprMill.exprBuilder(ctx).mkAnd(constr, bool))),
            getType(var),
            this);
  }

  private Set<ExprBuilder> collectExpr(Set<String> exprSet) {
    return exprSet.stream().map(expr -> varNames.get(expr)).collect(Collectors.toSet());
  }

  protected Function<ExprBuilder, Z3SetBuilder> convertGenDeclLeft(
      ASTGeneratorDeclaration node, Set<String> declVars) {

    ExprBuilder expr = varNames.get(node.getName());
    List<ExprBuilder> varList = new ArrayList<>(collectExpr(declVars));

    Z3SetBuilder set = convertSet(node.getExpression());
    return bool ->
        new Z3SetBuilder(
            obj ->
                mkExists(
                    varList,
                    ExprMill.exprBuilder(ctx)
                        .mkAnd(
                            ExprMill.exprBuilder(ctx).mkEq(obj, expr),
                            ExprMill.exprBuilder(ctx).mkAnd(set.contains(expr), bool))),
            getType(expr),
            this);
  }

  // a.auction**
  protected Z3SetBuilder convertTransClo(ASTOCLTransitiveQualification node) {

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

    ExprBuilder auction = convertExpr(fieldAcc.getExpression());

    FuncDecl<BoolSort> rel = buildReflexiveNewAssocFunc(getType(auction), fieldAcc.getName());
    FuncDecl<BoolSort> trans_rel = TransitiveClosure.mkTransitiveClosure(ctx, rel);

    Function<ExprBuilder, ExprBuilder> setFunc =
        obj ->
            ExprMill.exprBuilder(ctx)
                .mkBool((BoolExpr) trans_rel.apply(auction.expr(), obj.expr()));
    return new Z3SetBuilder(setFunc, getType(auction), this);
  }

  private FuncDecl<BoolSort> buildReflexiveNewAssocFunc(OCLType type, String otherRole) {
    ASTCDType objClass = getASTCDType(type.getName(), getCD());
    ASTCDAssociation association = CDHelper.getAssociation(objClass, otherRole, getCD());
    Sort thisSort = typeConverter.deriveSort(type);
    FuncDecl<BoolSort> rel =
        ctx.mkFuncDecl("reflexive_relation", new Sort[] {thisSort, thisSort}, ctx.mkBoolSort());
    ExprBuilder obj1 = declVariable(type, "obj1");
    ExprBuilder obj2 = declVariable(type, "obj2");
    ExprBuilder rel_is_assocFunc =
        mkForall(
            List.of(obj1, obj2),
            ExprMill.exprBuilder(ctx)
                .mkEq(
                    ExprMill.exprBuilder(ctx)
                        .mkBool((BoolExpr) rel.apply(obj1.expr(), obj2.expr())),
                    ExprMill.exprBuilder(ctx)
                        .mkBool(
                            (BoolExpr)
                                cd2smtGenerator.evaluateLink(
                                    association, objClass, objClass, obj1.expr(), obj2.expr()))));
    genConstraints.add(rel_is_assocFunc);
    return rel;
  }

  protected ExprBuilder createVarFromSymbol(ASTNameExpression node) {
    SymTypeExpression typeExpr = TypeConverter.deriveType(node);
    OCLType type = typeConverter.buildOCLType(typeExpr);
    return declVariable(type, node.getName());
  }

  private void notFullyImplemented(ASTExpression node) {
    Log.error("conversion of Set of the type " + node.getClass().getName() + " not implemented");
  }

  public ExprBuilder mkForall(List<ExprBuilder> vars, ExprBuilder body) {
    return ExprMill.exprBuilder(ctx).mkBool(mkQuantifier(vars, body, true));
  }

  public ExprBuilder mkExists(List<ExprBuilder> vars, ExprBuilder body) {
    return ExprMill.exprBuilder(ctx).mkBool(mkQuantifier(vars, body, false));
  }

  /***
   *Helper function to build a quantified formulas.
   * Quantification of CDType expressions (Auction, person...) must be perform by ge CD2SMTGenerator +
   * according to the actual Strategy.
   * But quantification of Expressions with primitive types (Bool, Int,..) must be perform directly.
   */
  public BoolExpr mkQuantifier(List<ExprBuilder> vars, ExprBuilder body, boolean isForall) {

    // split expressions int non CDType(String , Bool..) and CDType Expression(Auction, Person...)
    List<ExprBuilder> cdTypeExprList =
        vars.stream().filter(ExprBuilder::isUnInterpreted).collect(Collectors.toList());
    List<Expr<?>> noncdTypeExprList =
        vars.stream()
            .filter(ExprBuilder::hasNativeType)
            .map(e -> (Expr<?>) e.expr())
            .collect(Collectors.toList());

    // collect the CDType of the CDType Expressions
    List<ASTCDType> types =
        cdTypeExprList.stream()
            .map(var -> getASTCDType(getType(var).getName(), getCD()))
            .collect(Collectors.toList());
    BoolExpr subRes = body.expr();

    if (cdTypeExprList.size() > 0) {
      if (isForall) {
        subRes =
            cd2smtGenerator.mkForall(
                types,
                cdTypeExprList.stream().map(e -> (Expr<?>) e.expr()).collect(Collectors.toList()),
                body.expr());
        // todo: refactoring
      } else {
        subRes =
            cd2smtGenerator.mkExists(
                types,
                cdTypeExprList.stream().map(e -> (Expr<?>) e.expr()).collect(Collectors.toList()),
                body.expr());
      }
    }

    if (noncdTypeExprList.isEmpty()) {
      return subRes;
    } else {
      if (isForall) {
        return ctx.mkForall(vars.toArray(new Expr[0]), subRes, 0, null, null, null, null);
      } else {
        return ctx.mkExists(vars.toArray(new Expr[0]), subRes, 0, null, null, null, null);
      }
    }
  }

  public ExprBuilder declObj(OCLType type, String name) {
    ExprBuilder expr = ExprMill.exprBuilder(ctx).mkExpr(name, typeConverter.deriveSort(type));
    varTypes.put(expr, type);
    return expr;
  }

  public OCLType getType(ExprBuilder expr) {
    if (varTypes.containsKey(expr)) {
      return varTypes.get(expr);
    } else if (expr.hasNativeType()) {
      return OCLType.buildOCLType(expr.getKind());
    }
    Log.error("Type not found for the Variable " + expr);
    return null;
  }
}
