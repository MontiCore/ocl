package de.monticore.ocl2smt.ocl2smt;

import static de.monticore.cd2smt.Helper.CDHelper.getASTCDType;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.util.*;
import de.monticore.ocl2smt.visitors.NameExpressionVisitor;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class OCLExpression2SMT {

  protected final Context ctx;
  protected final CD2SMTGenerator cd2smtGenerator;
  protected ConstraintsData constrData = new ConstraintsData();
  protected ConstConverter constConverter;

  public OCLExpression2SMT(ASTCDCompilationUnit astcdCompilationUnit, Context ctx) {
    constConverter = new ConstConverter(ctx);
    this.ctx = ctx;
    cd2smtGenerator = new CD2SMTGenerator();
    cd2smtGenerator.cd2smt(astcdCompilationUnit, ctx);
    TypeConverter.setup(cd2smtGenerator);
  }

  public OCLExpression2SMT(
      ASTCDCompilationUnit astcdCompilationUnit,
      de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator ocl2SMTGenerator) {
    constConverter = new ConstConverter(ocl2SMTGenerator.getCtx());
    this.ctx = ocl2SMTGenerator.getCtx();
    cd2smtGenerator = ocl2SMTGenerator.getCD2SMTGenerator();
    cd2smtGenerator.cd2smt(astcdCompilationUnit, ctx);
    TypeConverter.setup(cd2smtGenerator);
  }

  public void init() {
    constrData = new ConstraintsData();
    constConverter.reset();
  }

  protected ASTCDDefinition getCD() {
    return cd2smtGenerator.getClassDiagram().getCDDefinition();
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
    } else if (node instanceof ASTSetInExpression) {
      result = convertSetIn((ASTSetInExpression) node);
    } else if (node instanceof ASTSetNotInExpression) {
      result = convertSetNotIn((ASTSetNotInExpression) node);
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
      Log.error(
          "the conversion of expressions with the type "
              + node.getClass().getName()
              + "is   not totally  implemented");
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
      Log.error(
          "the conversion of expressions with the type "
              + node.getClass().getName()
              + "is   not totally  implemented");
      assert false;
    }
    return result.get();
  }

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

  // ----------------------Arithmetic
  // ----------------------------------------
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
    return ctx.mkMod(
        (IntExpr) convertExprArith(node.getLeft()), (IntExpr) convertExprArith(node.getRight()));
  }

  protected ArithExpr<? extends ArithSort> convertPlus(ASTPlusExpression node) {
    return ctx.mkAdd(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }

  protected ArithExpr<ArithSort> convertMinus(ASTMinusExpression node) {
    return ctx.mkSub(convertExprArith(node.getLeft()), convertExprArith(node.getRight()));
  }
  // ---------------------------------------Logic---------------------------------

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

  // --------------------------comparison----------------------------------------------
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
    if (TypeConverter.isSet(node.getRight()) && TypeConverter.isSet(node.getLeft())) {
      return SMTSet.mkSetEq(convertSet(node.getLeft()), convertSet(node.getRight()), ctx);
    }
    return ctx.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight()));
  }

  protected BoolExpr convertNEq(ASTNotEqualsExpression node) {
    return ctx.mkNot(ctx.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight())));
  }

  /*------------------------------------quantified expressions----------------------------------------------------------*/
  Map<Expr<? extends Sort>, Optional<ASTExpression>> openScope(
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
      constraintList.add(mySet.isIn(expr.getKey()));
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
        ctx.mkForall(
            var.keySet().toArray(new Expr[0]),
            ctx.mkImplies(constraint, convertBoolExpr(node.getExpression())),
            1,
            null,
            null,
            null,
            null);

    // Delete Variables from "scope"
    closeScope(node.getInDeclarationList());

    return result;
  }

  protected BoolExpr convertExist(ASTExistsExpression node) {
    // declare Variable from scope
    Map<Expr<? extends Sort>, Optional<ASTExpression>> var = openScope(node.getInDeclarationList());

    BoolExpr constraint = convertInDeclConstraints(var);

    BoolExpr result =
        ctx.mkExists(
            var.keySet().toArray(new Expr[0]),
            ctx.mkAnd(constraint, convertBoolExpr(node.getExpression())),
            0,
            null,
            null,
            null,
            null);

    // Delete Variables from "scope"
    closeScope(node.getInDeclarationList());

    return result;
  }

  /*----------------------------------control expressions----------------------------------------------------------*/
  protected Expr<? extends Sort> convertIfTEl(ASTIfThenElseExpression node) {
    return ctx.mkITE(
        convertBoolExpr(node.getCondition()),
        convertExpr(node.getThenExpression()),
        convertExpr(node.getElseExpression()));
  }

  protected Expr<? extends Sort> convertImpl(ASTImpliesExpression node) {
    return ctx.mkImplies(convertBoolExpr(node.getLeft()), convertBoolExpr(node.getRight()));
  }

  // -----------------------------------general----------------------------------------------------------------------*/
  protected Expr<? extends Sort> convertName(ASTNameExpression node) {
     Expr<? extends  Sort> res;
    if (constrData.containsVar(node.getName())) {
      res = constrData.getVar(node.getName());
    }else {
      res = declVariable(
              TypeConverter.buildOCLType((VariableSymbol) node.getDefiningSymbol().get()),
              node.getName());
    }
    return  res ;
  }

  protected Expr<? extends Sort> convertBracket(ASTBracketExpression node) {
    return convertExpr(node.getExpression());
  }

  protected Expr<? extends Sort> convertFieldAcc(ASTFieldAccessExpression node) {
    Expr<? extends Sort> obj = convertExpr(node.getExpression());
    OCLType type = constConverter.getType(obj);
    return OCLHelper.getAttribute(obj, type, node.getName(), cd2smtGenerator);
  }

  // e.g  auction.person.parent
  protected SMTSet convertFieldAccAssoc(ASTFieldAccessExpression node) {


    if (!(node.getExpression() instanceof ASTFieldAccessExpression)) {

      Expr<? extends Sort> auction = convertExpr(node.getExpression());
      OCLType type1 = constConverter.getType(auction);
      ASTCDAssociation association = OCLHelper.getAssociation(type1, node.getName(), getCD());
      OCLType type2 = OCLHelper.getOtherType(association, type1);

      Function<Expr<? extends Sort>, BoolExpr> auction_per_set =
          per ->
              OCLHelper.evaluateLink(
                  association, auction, per, cd2smtGenerator, constConverter);

      return new SMTSet(auction_per_set, type2);
    }

    SMTSet pSet = convertSet(node.getExpression());

    OCLType type1 = pSet.getType();
    ASTCDAssociation person_parent = OCLHelper.getAssociation(type1, node.getName(), getCD());
    OCLType type2 = OCLHelper.getOtherType(person_parent, type1);

    Function<Expr<? extends Sort>, SMTSet> function =
        obj1 ->
            new SMTSet(
                obj2 ->
                    OCLHelper.evaluateLink(
                        person_parent, obj1, obj2, cd2smtGenerator, constConverter),
                type2);

    return pSet.collectAll(function, ctx);
  }

  protected void closeScope(List<ASTInDeclaration> inDeclarations) {
    for (ASTInDeclaration decl : inDeclarations) {
      for (ASTInDeclarationVariable var : decl.getInDeclarationVariableList()) {
        assert constrData.containsVar(var.getName());
        constrData.removeVar(var.getName());
      }
    }
  }

  protected List<Expr<? extends Sort>> convertInDecl(ASTInDeclaration node) {
    List<Expr<? extends Sort>> result = new ArrayList<>();
    for (ASTInDeclarationVariable var : node.getInDeclarationVariableList()) {
      if (node.isPresentMCType()) {
        result.add(declVariable(TypeConverter.buildOCLType(node.getMCType()), var.getName()));
      } else {
        result.add(declVariable(TypeConverter.buildOCLType(var.getSymbol()), var.getName()));
      }
    }
    return result;
  }

  // ---------------------------------------Set-Expressions----------------------------------------------------------------
  protected BoolExpr convertSetIn(ASTSetInExpression node) {
    return convertSet(node.getSet()).isIn(convertExpr(node.getElem()));
  }

  protected BoolExpr convertSetNotIn(ASTSetNotInExpression node) {
    return ctx.mkNot(convertSet(node.getSet()).isIn(convertExpr(node.getElem())));
  }

  protected SMTSet convertSet(ASTExpression node) {
    Log.info("I have got a " + node.getClass().getName(), this.getClass().getName());
    SMTSet set = null;
    if (node instanceof ASTFieldAccessExpression) {
      set = convertFieldAccAssoc((ASTFieldAccessExpression) node);
    } else if (node instanceof ASTBracketExpression) {
      set = convertSet(((ASTBracketExpression) node).getExpression());
    } else if (node instanceof ASTOCLTransitiveQualification) {
      set = convertTransClo((ASTOCLTransitiveQualification) node);
    } else if (node instanceof ASTUnionExpression) {
      set =
          SMTSet.mkSetUnion(
              convertSet(((ASTUnionExpression) node).getLeft()),
              convertSet(((ASTUnionExpression) node).getRight()),
              ctx);
    } else if (node instanceof ASTIntersectionExpression) {
      set =
          SMTSet.mkSetIntersect(
              convertSet(((ASTIntersectionExpression) node).getLeft()),
              convertSet(((ASTIntersectionExpression) node).getRight()),
              ctx);
    } else if (node instanceof ASTSetMinusExpression) {
      set =
          SMTSet.mkSetMinus(
              convertSet(((ASTSetMinusExpression) node).getLeft()),
              convertSet(((ASTSetMinusExpression) node).getRight()),
              ctx);
    } else if (node instanceof ASTSetComprehension) {
      set = convertSetComp((ASTSetComprehension) node);
    } else if (node instanceof ASTSetEnumeration) {
      set = convertSetEnum((ASTSetEnumeration) node);
    } else {
      Log.error("conversion of Set of the type " + node.getClass().getName() + " not implemented");
    }
    return set;
  }

  private Set<String> openSetCompScope(ASTSetComprehension node) {
    // collect all variable in the ASTSetComprehension
    OCLTraverser traverser = OCLMill.traverser();
    NameExpressionVisitor collectVarName = new NameExpressionVisitor();
    traverser.add4ExpressionsBasis(collectVarName);
    node.accept(traverser);

    // just return Variable which was declared in the  ASTSetComprehension scope
    return collectVarName.getVariableNameSet().stream()
        .filter(name -> !constrData.containsVar(name))
        .collect(Collectors.toSet());
  }

  protected SMTSet convertSetComp(ASTSetComprehension node) {
    Set<String> setCompVarNames = openSetCompScope(node);
    Function<BoolExpr, SMTSet> setComp = convertSetCompLeftSide(node.getLeft(), setCompVarNames);
    BoolExpr filter = ctx.mkTrue();

    for (ASTSetComprehensionItem item : node.getSetComprehensionItemList()) {
      if (item.isPresentGeneratorDeclaration()) {
        filter = ctx.mkAnd(filter, convertGenDeclRight(item.getGeneratorDeclaration()));
      }
      if (item.isPresentExpression()) {
        filter = ctx.mkAnd(filter, convertBoolExpr(item.getExpression()));
      }
      if (item.isPresentSetVariableDeclaration()) {
        filter = ctx.mkAnd(filter, convertSetVarDeclRight(item.getSetVariableDeclaration()));
      }
    }
    closeSetCompScope(setCompVarNames);
    SMTSet res = setComp.apply(filter);
    assert res.getType() != null;
    return res;
  }

  private void closeSetCompScope(Set<String> setCompVarNames) {
    for (String name : setCompVarNames) {
      constrData.removeVar(name);
    }
    setCompVarNames.clear();
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
    SMTSet set = new SMTSet(obj -> ctx.mkFalse(), OCLType.buildOCLType(sort.getName().toString()));
    SMTSet set1 = set;
    if (!setItemValues.isEmpty()) {
      set =
          new SMTSet(
              obj -> ctx.mkOr(set1.isIn(obj), addValuesToSetEnum(setItemValues, obj)),
              OCLType.buildOCLType(sort.getName().toString()));
    }

    for (Function<ArithExpr<? extends Sort>, BoolExpr> range : rangeFilters) {
      // TODO:: fix the Warning
      SMTSet set2 = set;
      set =
          new SMTSet(
              obj -> ctx.mkOr(set2.isIn(obj), range.apply((ArithExpr<? extends Sort>) obj)),
              OCLType.buildOCLType(sort.getName().toString()));
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
      ASTSetComprehensionItem node, Set<String> setCompvarnames) {
    Function<BoolExpr, SMTSet> res = null;
    if (node.isPresentGeneratorDeclaration()) {
      res = convertGenDeclLeft(node.getGeneratorDeclaration());
    } else if (node.isPresentSetVariableDeclaration()) {
      res = convertSetVarDeclLeft(node.getSetVariableDeclaration());
    } else if (node.isPresentExpression()) {
      res = convertSetCompExprLeft(node.getExpression(), setCompvarnames);
    } else {
      Log.error(
          "AT position "
              + "<"
              + node.get_SourcePositionStart().getLine()
              + ","
              + node.get_SourcePositionStart().getColumn()
              + ">"
              + "The Left side  of a ASTSetComprehension Cannot be from the type "
              + node.getExpression().getClass());
    }
    return res;
  }

  protected Function<BoolExpr, SMTSet> convertSetCompExprLeft(
      ASTExpression node, Set<String> setCompVarNames) {
    Expr<? extends Sort> expr1 = convertExpr(node);
    // define a const  for the quantifier
    Expr<? extends Sort> expr2 = ctx.mkConst("var", expr1.getSort());
    Set<Expr<? extends Sort>> vars = new HashSet<>();
    setCompVarNames.forEach(
        x -> {
          if (constrData.containsVar(x)) {
            vars.add(constrData.getVar(x));
          }
          vars.add(expr2);
        });
    return bool ->
        new SMTSet(
            obj ->
                ctx.mkExists(
                    vars.toArray(new Expr[0]),
                    ctx.mkAnd(ctx.mkEq(obj, expr2), ctx.mkEq(expr2, expr1), bool),
                    0,
                    null,
                    null,
                    null,
                    null),
            constConverter.getType(expr1));
  }

  protected Function<BoolExpr, SMTSet> convertSetVarDeclLeft(ASTSetVariableDeclaration node) {
    Expr<? extends Sort> expr =
        declVariable(TypeConverter.buildOCLType(node.getMCType()), node.getName());
    return bool ->
        new SMTSet(
            obj ->
                ctx.mkExists(
                    new Expr[] {expr},
                    ctx.mkAnd(ctx.mkEq(obj, expr), bool),
                    0,
                    null,
                    null,
                    null,
                    null),
            constConverter.getType(expr));
  }

  protected BoolExpr convertSetVarDeclRight(ASTSetVariableDeclaration node) {
    Expr<? extends Sort> expr;
    if (node.isPresentMCType()) {
      expr = declVariable(TypeConverter.buildOCLType(node.getMCType()), node.getName());
    } else {
      expr = declVariable(TypeConverter.buildOCLType(node.getSymbol()), node.getName());
    }

    if (node.isPresentExpression()) {
      return ctx.mkEq(expr, convertExpr(node.getExpression()));
    }
    return ctx.mkTrue();
  }

  protected BoolExpr convertGenDeclRight(ASTGeneratorDeclaration node) {
    Expr<? extends Sort> expr = declareSetGenVar(node);
    SMTSet set = convertSet(node.getExpression());
    return set.isIn(expr);
  }

  protected Expr<? extends Sort> declareSetGenVar(ASTGeneratorDeclaration node) {
    Expr<? extends Sort> res;
    if (node.isPresentMCType()) {
      res = declVariable(TypeConverter.buildOCLType(node.getMCType()), node.getName());
    } else {
      res = declVariable(TypeConverter.buildOCLType(node.getSymbol()), node.getName());
    }
    return res;
  }

  protected Function<BoolExpr, SMTSet> convertGenDeclLeft(ASTGeneratorDeclaration node) {
    Expr<? extends Sort> expr = declareSetGenVar(node);
    SMTSet set = convertSet(node.getExpression());
    return bool ->
        new SMTSet(
            obj ->
                ctx.mkExists(
                    new Expr[] {expr},
                    ctx.mkAnd(ctx.mkEq(obj, expr), set.isIn(expr), bool),
                    0,
                    null,
                    null,
                    null,
                    null),
            constConverter.getType(expr));
  }

  // a.auction**
  protected SMTSet convertTransClo(ASTOCLTransitiveQualification node) {
    assert node.getExpression() instanceof ASTFieldAccessExpression;
    if (!node.isTransitive()) {
      return convertSet(node);
    }
    ASTFieldAccessExpression fieldAcc = (ASTFieldAccessExpression) node.getExpression();
    Expr<? extends Sort> auction = convertExpr(fieldAcc.getExpression());

    FuncDecl<BoolSort> rel =
        buildReflexiveNewAssocFunc(constConverter.getType(auction), fieldAcc.getName());
    FuncDecl<BoolSort> trans_rel = TransitiveClosure.mkTransitiveClosure(ctx, rel);

    Function<Expr<? extends Sort>, BoolExpr> setFunc =
        obj -> (BoolExpr) trans_rel.apply(auction, obj);
    return new SMTSet(setFunc, constConverter.getType(auction));
  }

  private FuncDecl<BoolSort> buildReflexiveNewAssocFunc(OCLType type, String otherRole) {
    ASTCDType objClass = getASTCDType(type.getName(), getCD());
    ASTCDAssociation association = CDHelper.getAssociation(objClass, otherRole, getCD());
    Sort thisSort = TypeConverter.getSort(type);
    FuncDecl<BoolSort> rel =
        ctx.mkFuncDecl("reflexive_relation", new Sort[] {thisSort, thisSort}, ctx.mkBoolSort());
    Expr<? extends Sort> obj1 = ctx.mkConst("obj1", thisSort);
    Expr<? extends Sort> obj2 = ctx.mkConst("obj2", thisSort);
    BoolExpr rel_is_assocFunc =
        ctx.mkForall(
            new Expr[] {obj1, obj2},
            ctx.mkEq(
                rel.apply(obj1, obj2),
                cd2smtGenerator.evaluateLink(association, objClass, objClass, obj1, obj2)),
            0,
            null,
            null,
            null,
            null);
    constrData.genConstraints.add(rel_is_assocFunc);
    return rel;
  }



  protected Expr<? extends Sort> declVariable(OCLType type, String name) {
    Expr<? extends Sort> res = constConverter.declObj(type, name);
    constrData.addVar(name, res);
    return res;
  }


}
