package de.monticore.ocl2smt.ocl2smt.expressionconverter;

import static de.monticore.cd2smt.Helper.CDHelper.getASTCDType;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.Helper.SMTHelper;
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
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.ocl2smt.util.SMTSet;
import de.monticore.ocl2smt.util.TypeConverter;
import de.monticore.ocl2smt.visitors.NameExpressionVisitor;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symboltable.ISymbol;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/** This class convert All OCL-Expressions except @Pre-Expressions in SMT */
public class OCLExpressionConverter {

  protected final Context ctx;
  protected final CD2SMTGenerator cd2smtGenerator;

  protected Map<String, Expr<? extends Sort>> varNames;

  protected Set<BoolExpr> genConstraints;

  protected LiteralConverter literalConverter;

  public TypeConverter typeConverter;

  public OCLExpressionConverter(ASTCDCompilationUnit astcdCompilationUnit, Context ctx) {
    this.ctx = ctx;
    genConstraints = new HashSet<>();
    varNames = new HashMap<>();
    cd2smtGenerator = new CD2SMTGenerator();

    cd2smtGenerator.cd2smt(astcdCompilationUnit, ctx);
    typeConverter = new TypeConverter(cd2smtGenerator);
    literalConverter = new LiteralConverter(ctx, typeConverter);
  }

  public OCLExpressionConverter(ASTCDCompilationUnit ast, OCL2SMTGenerator ocl2SMTGenerator) {
    literalConverter = new LiteralConverter(ocl2SMTGenerator.getCtx(), typeConverter);
    this.ctx = ocl2SMTGenerator.getCtx();
    cd2smtGenerator = ocl2SMTGenerator.getCD2SMTGenerator();
    cd2smtGenerator.cd2smt(ast, ctx);
    typeConverter = new TypeConverter(cd2smtGenerator);
  }

  public LiteralConverter getLiteralConverter() {
    return literalConverter;
  }

  public Map<String, Expr<? extends Sort>> getVarNames() {
    return varNames;
  }

  public void reset() {
    literalConverter.reset();
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

  public BoolExpr convertBoolExpr(ASTExpression node) {
    Optional<BoolExpr> result = convertBoolExprOpt(node);
    if (result.isEmpty()) {
      notFullyImplemented(node);
      assert false;
    }
    return result.get();
  }

  public Expr<? extends Sort> convertExpr(ASTExpression node) {
    Expr<? extends Sort> res;
    res = convertGenExprOpt(node).orElse(null);
    if (res == null) {
      res = convertBoolExprOpt(node).orElse(null);
      if (res == null) {
        res = convertExprArithOpt(node).orElse(null);
        if (res == null) {
          res = convertExprString(node);
        }
      }
    }
    return res;
  }

  public Expr<? extends Sort> declVariable(OCLType type, String name) {
    Expr<? extends Sort> res = literalConverter.declObj(type, name);
    varNames.put(name, res);
    return res;
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
    } else if (node instanceof ASTImpliesExpression) {
      result = convertImpl((ASTImpliesExpression) node);
    } else if (node instanceof ASTCallExpression && methodReturnsBool((ASTCallExpression) node)) {
      result = convertBoolMethodCall((ASTCallExpression) node);
    } else if (node instanceof ASTEquivalentExpression) {
      result = convertEquiv((ASTEquivalentExpression) node);
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

  protected Optional<ArithExpr<? extends ArithSort>> convertExprArithOpt(ASTExpression node) {
    ArithExpr<? extends ArithSort> result;
    if (node instanceof ASTMinusPrefixExpression) {
      result = convertMinPref((ASTMinusPrefixExpression) node);
    } else if (node instanceof ASTPlusPrefixExpression) {
      result = convertPlusPref((ASTPlusPrefixExpression) node);
    } else if ((node instanceof ASTPlusExpression) && isAddition((ASTPlusExpression) node)) {
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

  protected Optional<SeqExpr<CharSort>> convertExprStringOpt(ASTExpression node) {
    SeqExpr<CharSort> result;

    if ((node instanceof ASTPlusExpression && isStringConcat((ASTPlusExpression) node))) {
      result = convertStringConcat((ASTPlusExpression) node);
    } else if (node instanceof ASTCallExpression
        && methodReturnsString((ASTCallExpression) node)
        && ((ASTCallExpression) node).getExpression() instanceof ASTFieldAccessExpression) {
      result = convertStringMethodCall((ASTCallExpression) node);
    } else {
      Optional<Expr<? extends Sort>> buf = convertGenExprOpt(node);
      if (buf.isPresent() && buf.get().getSort().getName().isStringSymbol()) {
        result = (SeqExpr<CharSort>) buf.get();
      } else {
        return Optional.empty();
      }
    }
    return Optional.of(result);
  }

  protected SeqExpr<CharSort> convertExprString(ASTExpression node) {
    //  Log.info("I have got a " + node.getClass().getName(), this.getClass().getName());
    Optional<SeqExpr<CharSort>> result = convertExprStringOpt(node);
    if (result.isEmpty()) {
      notFullyImplemented(node);
      assert false;
    }
    return result.get();
  }

  protected SeqExpr<CharSort> convertStringMethodCall(ASTCallExpression node) {
    SeqExpr<CharSort> res = null;
    if (node.getDefiningSymbol().isPresent()) {
      String name = node.getDefiningSymbol().get().getName();
      if (node.getExpression() instanceof ASTFieldAccessExpression) {
        SeqExpr<CharSort> arg1 = convertExprString(node.getArguments().getExpression(0));
        SeqExpr<CharSort> arg2 = convertExprString(node.getArguments().getExpression(1));
        SeqExpr<CharSort> str =
            convertExprString(((ASTFieldAccessExpression) node.getExpression()).getExpression());
        if ("replace".equals(name)) {
          res = ctx.mkReplace(str, arg1, arg2);
        }
      }
      return res;
    }
    notFullyImplemented(node);
    return null;
  }

  protected ArithExpr<? extends ArithSort> convertExprArith(ASTExpression node) {
    Optional<ArithExpr<? extends ArithSort>> result = convertExprArithOpt(node);
    if (result.isEmpty()) {
      notFullyImplemented(node);
      assert false;
    }
    return result.get();
  }

  protected Optional<Expr<? extends Sort>> convertGenExprOpt(ASTExpression node) {
    Expr<? extends Sort> res;
    if (node instanceof ASTLiteralExpression) {
      res = literalConverter.convert((ASTLiteralExpression) node);
    } else if (node instanceof ASTBracketExpression) {
      res = convertBracket((ASTBracketExpression) node);
    } else if (node instanceof ASTNameExpression) {
      res = convertName((ASTNameExpression) node);
    } else if (node instanceof ASTFieldAccessExpression) {
      res = convertFieldAcc((ASTFieldAccessExpression) node);
    } else if (node instanceof ASTIfThenElseExpression) {
      res = convertIfTEl((ASTIfThenElseExpression) node);
    } else if (node instanceof ASTTypeIfExpression) {
      res = convertTypeIf((ASTTypeIfExpression) node);
    } else if (node instanceof ASTConditionalExpression) {
      res = convertCond((ASTConditionalExpression) node);
    } else if (node instanceof ASTCallExpression
        && !TypeConverter.isString(node)
        && !methodReturnsBool((ASTCallExpression) node)) {
      res = convertCall((ASTCallExpression) node);
    } else {
      return Optional.empty();
    }
    return Optional.of(res);
  }

  // -----------String--------------
  protected SeqExpr<CharSort> convertStringConcat(ASTPlusExpression node) {
    return ctx.mkConcat(convertExprString(node.getLeft()), convertExprString(node.getRight()));
  }

  // ----------------------Arit--------------------
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
      return convertSet(node.getLeft()).mkSetEq(convertSet(node.getRight()));
    }
    return ctx.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight()));
  }

  protected BoolExpr convertNEq(ASTNotEqualsExpression node) {
    return ctx.mkNot(ctx.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight())));
  }

  protected BoolExpr convertEquiv(ASTEquivalentExpression node) {
    return ctx.mkEq(convertExpr(node.getLeft()), convertExpr(node.getRight()));
  }

  protected Expr<? extends Sort> convertTypeIf(ASTTypeIfExpression node) {
    return ctx.mkITE(
        ctx.mkTrue(), convertExpr(node.getElseExpression()), convertExpr(node.getElseExpression()));
  }

  protected BoolExpr convertBoolMethodCall(ASTCallExpression node) {
    BoolExpr res = null;
    if (node.getDefiningSymbol().isPresent()) {
      String name = node.getDefiningSymbol().get().getName();
      if (node.getExpression() instanceof ASTFieldAccessExpression) {
        ASTExpression caller = ((ASTFieldAccessExpression) node.getExpression()).getExpression();
        if (TypeConverter.isString(caller)) {
          res = convertBoolStringOp(caller, node.getArguments().getExpression(0), name);
        } else if (TypeConverter.isDate(caller)) {
          res = convertBoolDateOp(caller, node.getArguments().getExpression(0), name);
        } else if (TypeConverter.isSet(caller)) {
          res = convertBoolSetOp(node, name);
        } else if (TypeConverter.isOptional(caller)) {
          if (caller instanceof ASTFieldAccessExpression) {
            res = convertBoolOptionalOp((ASTFieldAccessExpression) caller, name);
          } else {
            notFullyImplemented(node);
          }
        }
      }
      return res;
    }
    notFullyImplemented(node);
    return null;
  }

  protected BoolExpr convertImpl(ASTImpliesExpression node) {
    return ctx.mkImplies(convertBoolExpr(node.getLeft()), convertBoolExpr(node.getRight()));
  }

  protected BoolExpr convertBoolDateOp(ASTExpression caller, ASTExpression arg, String methodName) {
    BoolExpr res = null;

    ArithExpr<? extends ArithSort> argument = convertExprArith(arg);
    ArithExpr<? extends ArithSort> date = convertExprArith(caller);
    switch (methodName) {
      case "before":
        res = ctx.mkLt(date, argument);
        break;

      case "after":
        res = ctx.mkGt(date, argument);
        break;
    }
    return res;
  }

  protected BoolExpr convertBoolStringOp(
      ASTExpression caller, ASTExpression arg, String methodName) {
    BoolExpr res = null;
    SeqExpr<CharSort> argument = convertExprString(arg);
    SeqExpr<CharSort> str = convertExprString(caller);
    switch (methodName) {
      case "contains":
        res = ctx.mkContains(str, argument);
        break;
      case "endsWith":
        res = ctx.mkSuffixOf(argument, str);
        break;
      case "startsWith":
        res = ctx.mkPrefixOf(argument, str);
        break;
    }
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
    BoolExpr isPresent =
        ctx.mkExists(new Expr[] {link.getLeft()}, link.getRight(), 0, null, null, null, null);
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

  protected Expr<? extends Sort> convertCond(ASTConditionalExpression node) {
    return ctx.mkITE(
        convertBoolExpr(node.getCondition()),
        convertExpr(node.getTrueExpression()),
        convertExpr(node.getFalseExpression()));
  }

  protected Expr<? extends Sort> convertCall(ASTCallExpression node) {
    if (node.getExpression() instanceof ASTFieldAccessExpression) {
      ASTFieldAccessExpression node1 = (ASTFieldAccessExpression) node.getExpression();
      if ((node1.getExpression() instanceof ASTFieldAccessExpression
          && node1.getName().equals("get"))) {
        ASTFieldAccessExpression caller = (ASTFieldAccessExpression) node1.getExpression();
        Pair<Expr<? extends Sort>, BoolExpr> res = convertFieldAccOptional(caller);
        genConstraints.add(res.getRight());
        return res.getLeft();
      }
    } else {
      notFullyImplemented(node);
    }
    return null;
  }

  // -----------------------------------general----------------------------------------------------------------------*/
  protected Expr<? extends Sort> convertName(ASTNameExpression node) {
    Expr<? extends Sort> res;
    if (varNames.containsKey(node.getName())) {
      res = varNames.get(node.getName());
    } else {
      res = createVarFromSymbol(node);
    }
    return res;
  }

  protected Expr<? extends Sort> convertBracket(ASTBracketExpression node) {
    return convertExpr(node.getExpression());
  }

  protected Pair<Expr<? extends Sort>, BoolExpr> convertFieldAccessSetHelper(
      Expr<? extends Sort> obj, String name) {
    OCLType type = literalConverter.getType(obj);
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
    OCLType type = literalConverter.getType(obj);
    ASTCDAssociation association = OCLHelper.getAssociation(type, role, getCD());

    OCLType type2 = OCLHelper.getOtherType(association, type, role, getCD());
    String resName = obj.getSExpr() + SMTHelper.fCharToLowerCase(type2.getName());
    Expr<? extends Sort> link = declVariable(type2, resName);
    BoolExpr linkConstraint =
        OCLHelper.evaluateLink(association, obj, role, link, cd2smtGenerator, literalConverter);
    return new ImmutablePair<>(link, linkConstraint);
  }

  protected Expr<? extends Sort> convertFieldAcc(ASTFieldAccessExpression node) {
    Expr<? extends Sort> obj = convertExpr(node.getExpression());
    Pair<Expr<? extends Sort>, BoolExpr> res = convertFieldAccessSetHelper(obj, node.getName());
    if (!TypeConverter.isOptional(node)) {
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
    OCLType type1 = literalConverter.getType(obj);
    ASTCDAssociation association = OCLHelper.getAssociation(type1, role, getCD());
    OCLType type2 = OCLHelper.getOtherType(association, type1, role, getCD());

    Function<Expr<? extends Sort>, BoolExpr> auction_per_set =
        per ->
            OCLHelper.evaluateLink(association, obj, role, per, cd2smtGenerator, literalConverter);

    return new SMTSet(auction_per_set, type2, literalConverter);
  }

  protected SMTSet convertFieldAccessSetHelper(ASTExpression node, String name) {
    if (!(node instanceof ASTFieldAccessExpression)) {
      Expr<? extends Sort> expr = convertExpr(node);
      return convertSimpleFieldAccessSet(expr, name);
    }

    SMTSet pSet = convertSet(node);

    OCLType type1 = pSet.getType();
    ASTCDAssociation person_parent = OCLHelper.getAssociation(type1, name, getCD());
    OCLType type2 = OCLHelper.getOtherType(person_parent, type1, name, getCD());

    Function<Expr<? extends Sort>, SMTSet> function =
        obj1 ->
            new SMTSet(
                obj2 ->
                    OCLHelper.evaluateLink(
                        person_parent, obj1, name, obj2, cd2smtGenerator, literalConverter),
                type2,
                literalConverter);

    return pSet.collectAll(function);
  }

  protected SMTSet convertFieldAccessSet(ASTFieldAccessExpression node) {
    return convertFieldAccessSetHelper(node.getExpression(), node.getName());
  }

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

  private Set<String> openSetCompScope(ASTSetComprehension node) {
    // collect all variable in the ASTSetComprehension
    OCLTraverser traverser = OCLMill.traverser();
    NameExpressionVisitor collectVarName = new NameExpressionVisitor();
    traverser.add4ExpressionsBasis(collectVarName);
    node.accept(traverser);

    // just return Variable which was declared in the  ASTSetComprehension scope
    return collectVarName.getVariableNameSet().stream()
        .filter(name -> !varNames.containsKey(name))
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
      varNames.remove(name);
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
    SMTSet set =
        new SMTSet(
            obj -> ctx.mkFalse(),
            OCLType.buildOCLType(sort.getName().toString()),
            literalConverter);
    SMTSet set1 = set;
    if (!setItemValues.isEmpty()) {
      set =
          new SMTSet(
              obj -> ctx.mkOr(set1.contains(obj), addValuesToSetEnum(setItemValues, obj)),
              OCLType.buildOCLType(sort.getName().toString()),
              literalConverter);
    }

    for (Function<ArithExpr<? extends Sort>, BoolExpr> range : rangeFilters) {
      // TODO:: fix the Warning
      SMTSet set2 = set;
      set =
          new SMTSet(
              obj -> ctx.mkOr(set2.contains(obj), range.apply((ArithExpr<? extends Sort>) obj)),
              OCLType.buildOCLType(sort.getName().toString()),
              literalConverter);
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
          if (varNames.containsKey(x)) {
            vars.add(varNames.get(x));
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
            literalConverter.getType(expr1),
            literalConverter);
  }

  protected Function<BoolExpr, SMTSet> convertSetVarDeclLeft(ASTSetVariableDeclaration node) {
    Expr<? extends Sort> expr =
        declVariable(typeConverter.buildOCLType(node.getMCType()), node.getName());
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
            literalConverter.getType(expr),
            literalConverter);
  }

  protected BoolExpr convertSetVarDeclRight(ASTSetVariableDeclaration node) {
    Expr<? extends Sort> expr;
    if (node.isPresentMCType()) {
      expr = declVariable(typeConverter.buildOCLType(node.getMCType()), node.getName());
    } else {
      expr = declVariable(typeConverter.buildOCLType(node.getSymbol()), node.getName());
    }

    if (node.isPresentExpression()) {
      return ctx.mkEq(expr, convertExpr(node.getExpression()));
    }
    return ctx.mkTrue();
  }

  protected BoolExpr convertGenDeclRight(ASTGeneratorDeclaration node) {
    Expr<? extends Sort> expr = declareSetGenVar(node);
    SMTSet set = convertSet(node.getExpression());
    return set.contains(expr);
  }

  protected Expr<? extends Sort> declareSetGenVar(ASTGeneratorDeclaration node) {
    Expr<? extends Sort> res;
    if (node.isPresentMCType()) {
      res = declVariable(typeConverter.buildOCLType(node.getMCType()), node.getName());
    } else {
      res = declVariable(typeConverter.buildOCLType(node.getSymbol()), node.getName());
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
                    ctx.mkAnd(ctx.mkEq(obj, expr), set.contains(expr), bool),
                    0,
                    null,
                    null,
                    null,
                    null),
            literalConverter.getType(expr),
            literalConverter);
  }

  // a.auction**
  protected SMTSet convertTransClo(ASTOCLTransitiveQualification node) {

    ASTFieldAccessExpression fieldAcc = null;
    if (node.getExpression() instanceof ASTFieldAccessExpression) {
      fieldAcc = (ASTFieldAccessExpression) node.getExpression();
    } else if (node.getExpression() instanceof ASTCallExpression) {
      ASTCallExpression callExpression = (ASTCallExpression) node.getExpression();
      if (TypeConverter.isOptional(
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

    FuncDecl<BoolSort> rel =
        buildReflexiveNewAssocFunc(literalConverter.getType(auction), fieldAcc.getName());
    FuncDecl<BoolSort> trans_rel = TransitiveClosure.mkTransitiveClosure(ctx, rel);

    Function<Expr<? extends Sort>, BoolExpr> setFunc =
        obj -> (BoolExpr) trans_rel.apply(auction, obj);
    return new SMTSet(setFunc, literalConverter.getType(auction), literalConverter);
  }

  private FuncDecl<BoolSort> buildReflexiveNewAssocFunc(OCLType type, String otherRole) {
    ASTCDType objClass = getASTCDType(type.getName(), getCD());
    ASTCDAssociation association = CDHelper.getAssociation(objClass, otherRole, getCD());
    Sort thisSort = typeConverter.getSort(type);
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
    genConstraints.add(rel_is_assocFunc);
    return rel;
  }

  protected Expr<? extends Sort> createVarFromSymbol(ASTNameExpression node) {
    Expr<? extends Sort> res = null;
    Optional<ISymbol> symbol = node.getDefiningSymbol();
    if (symbol.isPresent()) {
      OCLType type = typeConverter.buildOCLType((VariableSymbol) symbol.get());
      res = declVariable(type, node.getName());
    } else {
      Log.error(node.getClass().getName() + " Unrecognized Symbol " + node.getName());
    }
    return res;
  }

  private boolean methodReturnsBool(ASTCallExpression node) {
    if (node.getDefiningSymbol().isPresent()) {
      String name = node.getDefiningSymbol().get().getName();
      return (Set.of(
              "contains",
              "endsWith",
              "startsWith",
              "before",
              "after",
              "containsAll",
              "isEmpty",
              "isPresent")
          .contains(name));
    }
    return false;
  }

  private boolean methodReturnsString(ASTCallExpression node) {
    if (node.getDefiningSymbol().isPresent()) {
      String name = node.getDefiningSymbol().get().getName();
      return (name.equals("replace"));
    }
    return false;
  }

  private boolean isAddition(ASTPlusExpression node) {
    return (convertExpr(node.getLeft()).isInt() || convertExpr(node.getLeft()).isReal());
  }

  private boolean isStringConcat(ASTPlusExpression node) {
    return convertExpr((node).getLeft()).getSort().getName().isStringSymbol();
  }

  private void notFullyImplemented(ASTExpression node) {
    Log.error("conversion of Set of the type " + node.getClass().getName() + " not implemented");
  }
}
