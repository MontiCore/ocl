package de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3;

import static de.monticore.cd2smt.Helper.CDHelper.getLeftType;
import static de.monticore.cd2smt.Helper.CDHelper.getRightType;

import com.microsoft.z3.*;
import de.monticore.cd._symboltable.CDSymbolTables;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdassociation._ast.ASTCDCardinality;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.ocl2smt.ocl2smt.expr2smt.ExprKind;
import de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory.CDExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprFactory.ExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Z3ExprFactory implements ExprFactory<Z3ExprAdapter>, CDExprFactory<Z3ExprAdapter> {
  private final Context ctx;
  private final Z3TypeFactory tFactory;
  private final CD2SMTGenerator cd2SMTGenerator;

  private final String wrongParam =
      "Method %s(...) get parameter with wrong type '%s' expected was %s";

  public Z3ExprFactory(Z3TypeFactory factory, CD2SMTGenerator cd2SMTGenerator) {
    this.ctx = cd2SMTGenerator.getContext();
    this.cd2SMTGenerator = cd2SMTGenerator;
    this.tFactory = factory;
  }

  @Override
  public Z3ExprAdapter mkBool(boolean value) {
    return new Z3ExprAdapter(ctx.mkBool(value), tFactory.mkBoolType());
  }

  @Override
  public Z3ExprAdapter mkString(String value) {
    return new Z3ExprAdapter(ctx.mkString(value), tFactory.mkStringType());
  }

  @Override
  public Z3ExprAdapter mkInt(int value) {
    return new Z3ExprAdapter(ctx.mkInt(value), tFactory.mkInType());
  }

  @Override
  public Z3ExprAdapter mkChar(char value) {
    return new Z3ExprAdapter(ctx.mkInt(value), tFactory.mkCharType());
  }

  @Override
  public Z3ExprAdapter mkDouble(double value) {
    return new Z3ExprAdapter(ctx.mkFP(value, ctx.mkFPSortDouble()), tFactory.mkDoubleType());
  }

  @Override
  public Z3ExprAdapter mkSet(
      Function<Z3ExprAdapter, Z3ExprAdapter> setFunction, Z3ExprAdapter expr) {
    String name = "set<" + expr.getType().getName() + ">";
    return new Z3GenExprAdapter(setFunction, expr, name, ExprKind.SET);
  }

  public Z3ExprAdapter mkOptional(
      Function<Z3ExprAdapter, Z3ExprAdapter> setFunction, Z3ExprAdapter expr) {
    String name = "Optional<" + expr.getType().getName() + ">";
    return new Z3GenExprAdapter(setFunction, expr, name, ExprKind.OPTIONAL);
  }

  @Override
  public Z3ExprAdapter mkConst(String name, TypeAdapter type) {
    return new Z3ExprAdapter(
        ctx.mkConst(name, ((Z3TypeAdapter) type).getSort()), (Z3TypeAdapter) type);
  }

  @Override
  public Z3ExprAdapter mkNot(Z3ExprAdapter node) {
    checkBool("mkNot", node);

    BoolExpr expr = ctx.mkNot((BoolExpr) node.getExpr());
    Z3ExprAdapter res = new Z3ExprAdapter(expr, tFactory.mkBoolType());
    return wrap(res, node);
  }

  @Override
  public Z3ExprAdapter mkAnd(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkBool("mkAnd", leftNode);
    checkBool("mkAnd", rightNode);

    BoolExpr left = (BoolExpr) leftNode.getExpr();
    BoolExpr right = (BoolExpr) rightNode.getExpr();
    Z3ExprAdapter res = new Z3ExprAdapter(ctx.mkAnd(left, right), tFactory.mkBoolType());
    return wrap(res, leftNode, rightNode);
  }

  @Override
  public Z3ExprAdapter mkOr(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkBool("mkOr", leftNode);
    checkBool("mkOr", rightNode);

    BoolExpr left = (BoolExpr) leftNode.getExpr();
    BoolExpr right = (BoolExpr) rightNode.getExpr();
    Z3ExprAdapter res = new Z3ExprAdapter(ctx.mkOr(left, right), tFactory.mkBoolType());
    return wrap(res, leftNode, rightNode);
  }

  @Override
  public Z3ExprAdapter mkImplies(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkBool("mkImplies", leftNode);
    checkBool("mkImplies", rightNode);

    BoolExpr left = (BoolExpr) leftNode.getExpr();
    BoolExpr right = (BoolExpr) rightNode.getExpr();
    Z3ExprAdapter res = new Z3ExprAdapter(ctx.mkImplies(left, right), tFactory.mkBoolType());
    return wrap(res, leftNode, rightNode);
  }

  @Override
  public Z3ExprAdapter mkLt(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkArith("mkLt", leftNode);
    checkArith("mkLt", rightNode);

    Expr<?> value;
    if (leftNode.isIntExpr() || leftNode.isCharExpr()) {
      value = ctx.mkLt((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
    } else {
      value = ctx.mkFPLt((FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
    }
    Z3ExprAdapter res = new Z3ExprAdapter(value, tFactory.mkBoolType());
    return wrap(res, leftNode, rightNode);
  }

  @Override
  public Z3ExprAdapter mkLeq(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkArith("mkLeq", leftNode);
    checkArith("mkLeq", rightNode);

    Expr<?> expr;
    if (leftNode.isIntExpr() || leftNode.isCharExpr()) {
      expr = ctx.mkLe((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
    } else {
      expr = ctx.mkFPLEq((FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
    }
    Z3ExprAdapter res = new Z3ExprAdapter(expr, tFactory.mkBoolType());
    return wrap(res, leftNode, rightNode);
  }

  @Override
  public Z3ExprAdapter mkGt(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkArith("mkGt", leftNode);
    checkArith("mkGt", rightNode);

    Expr<?> expr;
    if (leftNode.isIntExpr() && rightNode.isIntExpr()) {
      expr = ctx.mkGt((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
    } else {
      expr = ctx.mkFPGt((FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
    }
    Z3ExprAdapter res = new Z3ExprAdapter(expr, tFactory.mkBoolType());
    return wrap(res, leftNode, rightNode);
  }

  @Override
  public Z3ExprAdapter mkEq(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    Z3ExprAdapter res;
    if (!leftNode.isSetExpr() && !rightNode.isSetExpr()) {
      res =
          new Z3ExprAdapter(
              ctx.mkEq(leftNode.getExpr(), rightNode.getExpr()), tFactory.mkBoolType());
    } else {
      Z3ExprAdapter element = ((Z3GenExprAdapter) leftNode).getElement();

      Expr<?> left = mkContains(rightNode, element).getExpr();
      Expr<?> right = mkContains(leftNode, element).getExpr();
      Z3ExprAdapter body = new Z3ExprAdapter(ctx.mkEq(left, right), tFactory.mkBoolType());
      res = mkForall(List.of(element), body);
    }

    return wrap(res, leftNode, rightNode);
  }

  @Override
  public Z3ExprAdapter mkGe(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkArith("mkGe", leftNode);
    checkArith("mkGe", rightNode);

    Expr<?> expr;
    if (leftNode.isIntExpr() || rightNode.isIntExpr()) {
      expr = ctx.mkGe((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
    } else {
      expr = ctx.mkFPGEq((FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
    }
    Z3ExprAdapter res = new Z3ExprAdapter(expr, tFactory.mkBoolType());
    return wrap(res, leftNode, rightNode);
  }

  @Override
  public Z3ExprAdapter mkSub(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkArith("mkSub", leftNode);
    checkArith("mkSub", rightNode);

    Expr<?> expr;
    Z3TypeAdapter type;
    if (leftNode.isIntExpr() || leftNode.isCharExpr()) {
      expr = ctx.mkSub((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
      type = tFactory.mkInType();
    } else {
      expr = ctx.mkFPSub(ctx.mkFPRNA(), (FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
      type = tFactory.mkDoubleType();
    }

    Z3ExprAdapter res = new Z3ExprAdapter(expr, type);
    return wrap(res, leftNode, rightNode);
  }

  @Override
  public Z3ExprAdapter mkPlus(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {

    Expr<?> expr;
    Z3TypeAdapter type;
    if (leftNode.isIntExpr() || leftNode.isCharExpr()) {
      expr = ctx.mkAdd((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
      type = tFactory.mkInType();
    } else if (leftNode.isDoubleExpr() && rightNode.isDoubleExpr()) {
      expr = ctx.mkFPAdd(ctx.mkFPRNA(), (FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
      type = tFactory.mkDoubleType();
    } else {

      expr = ctx.mkConcat(toCharSeq(leftNode), toCharSeq(rightNode));
      type = tFactory.mkStringType();
    }
    Z3ExprAdapter res = new Z3ExprAdapter(expr, type);
    return wrap(res, leftNode, rightNode);
  }

  @Override
  public Z3ExprAdapter mkMul(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkArith("mkSub", leftNode);
    checkArith("mkSub", rightNode);

    Expr<?> expr;
    Z3TypeAdapter type;
    if (leftNode.isIntExpr() || rightNode.isIntExpr()) {
      expr = ctx.mkMul((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
      type = tFactory.mkInType();
    } else {
      expr = ctx.mkFPMul(ctx.mkFPRNA(), (FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
      type = tFactory.mkDoubleType();
    }
    Z3ExprAdapter res = new Z3ExprAdapter(expr, type);
    return wrap(res, leftNode, rightNode);
  }

  @Override
  public Z3ExprAdapter mkDiv(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkArith("mkSub", leftNode);
    checkArith("mkSub", rightNode);

    Expr<?> expr;
    Z3TypeAdapter type;
    if (leftNode.isIntExpr() || rightNode.isIntExpr()) {
      expr = ctx.mkDiv((ArithExpr<?>) leftNode.getExpr(), (ArithExpr<?>) rightNode.getExpr());
      type = tFactory.mkInType();
    } else {
      expr = ctx.mkFPDiv(ctx.mkFPRNA(), (FPExpr) leftNode.getExpr(), (FPExpr) rightNode.getExpr());
      type = tFactory.mkDoubleType();
    }
    Z3ExprAdapter res = new Z3ExprAdapter(expr, type);
    return wrap(res, leftNode, rightNode);
  }

  @Override
  public Z3ExprAdapter mkMod(Z3ExprAdapter leftNode, Z3ExprAdapter rightNode) {
    checkInt("mkSub", leftNode);
    checkArith("mkSub", rightNode);

    Expr<?> expr = ctx.mkMod((IntExpr) leftNode.getExpr(), (IntExpr) rightNode.getExpr());
    Z3ExprAdapter res = new Z3ExprAdapter(expr, tFactory.mkInType());
    return wrap(res, leftNode, rightNode);
  }

  @Override
  public Z3ExprAdapter mkPlusPrefix(Z3ExprAdapter node) {
    return wrap(node);
  }

  @Override
  public Z3ExprAdapter mkMinusPrefix(Z3ExprAdapter node) {
    checkArith("mkMinusPrefix", node);

    Expr<?> expr;
    Z3TypeAdapter type;
    if (node.isIntExpr()) {
      expr = ctx.mkMul(ctx.mkInt(-1), (ArithExpr<?>) node.getExpr());
      type = tFactory.mkInType();
    } else {
      expr =
          ctx.mkFPMul(ctx.mkFPRNA(), ctx.mkFP(-1, ctx.mkFPSortDouble()), (FPExpr) node.getExpr());
      type = tFactory.mkDoubleType();
    }
    Z3ExprAdapter res = new Z3ExprAdapter(expr, type);
    return wrap(res, node);
  }

  @Override
  public Z3ExprAdapter mkIte(Z3ExprAdapter cond, Z3ExprAdapter expr1, Z3ExprAdapter expr2) {
    checkBool("mkIte", cond);

    Expr<?> expr = ctx.mkITE((BoolExpr) cond.getExpr(), expr1.getExpr(), expr2.getExpr());
    Z3ExprAdapter res = new Z3ExprAdapter(expr, expr1.getType());
    return wrap(res, cond, expr1, expr2);
  }

  @Override
  public Z3ExprAdapter mkReplace(Z3ExprAdapter s, Z3ExprAdapter src, Z3ExprAdapter dst) {
    checkString("mkReplace", s);
    checkString("mkReplace", src);
    checkString("mkReplace", dst);

    Expr<?> expr = ctx.mkReplace(toCharSeq(s), toCharSeq(src), toCharSeq(dst));
    Z3ExprAdapter res = new Z3ExprAdapter(expr, tFactory.mkStringType());
    return wrap(res, s, src, dst);
  }

  @Override
  public Z3ExprAdapter mkPrefixOf(Z3ExprAdapter s1, Z3ExprAdapter s2) {
    checkString("mkPrefixOf", s1);
    checkString("mkPrefixOf", s2);

    Z3TypeAdapter type = tFactory.mkBoolType();
    Z3ExprAdapter res = new Z3ExprAdapter(ctx.mkPrefixOf(toCharSeq(s1), toCharSeq(s2)), type);
    return wrap(res, s1, s2);
  }

  @Override
  public Z3ExprAdapter mkSuffixOf(Z3ExprAdapter s1, Z3ExprAdapter s2) {
    checkString("mkSuffixOf", s1);
    checkString("mkSuffixOf", s2);

    Z3TypeAdapter type = tFactory.mkBoolType();
    Z3ExprAdapter res = new Z3ExprAdapter(ctx.mkSuffixOf(toCharSeq(s1), toCharSeq(s2)), type);
    return wrap(res, s1, s2);
  }

  @Override
  public Z3ExprAdapter containsAll(Z3ExprAdapter exp1, Z3ExprAdapter exp2) {
    checkSet("containsAll", exp1);
    checkSet("containsAll", exp1);

    Z3GenExprAdapter set1 = (Z3GenExprAdapter) exp1;
    Z3GenExprAdapter set2 = (Z3GenExprAdapter) exp2;
    Z3ExprAdapter expr = set1.getElement();
    Z3ExprAdapter body = mkImplies(mkContains(set2, expr), mkContains(set1, expr));
    Z3ExprAdapter res = mkForall(List.of(expr), body);
    return wrap(res, exp1, exp2);
  }

  @Override
  public Z3ExprAdapter mkIsEmpty(Z3ExprAdapter expr) {
    checkGen("mkIsEmpty", expr);

    Z3GenExprAdapter set = ((Z3GenExprAdapter) expr);
    Z3ExprAdapter elem = set.getElement();
    Z3ExprAdapter res = mkForall(List.of(elem), mkNot(mkContains(set, elem)));
    return wrap(res, expr);
  }

  @Override
  public Z3ExprAdapter mkSetUnion(Z3ExprAdapter expr1, Z3ExprAdapter expr2) {
    checkSet("mkSetUnion", expr1);
    checkSet("mkSetUnion", expr2);

    Z3GenExprAdapter set1 = (Z3GenExprAdapter) expr1;
    Z3GenExprAdapter set2 = (Z3GenExprAdapter) expr2;
    Function<Z3ExprAdapter, Z3ExprAdapter> setFunction =
        obj -> mkOr(mkContains(set1, obj), mkContains(set2, obj));
    Z3ExprAdapter res = mkSet(setFunction, set1.getElement());
    return wrap(res, expr1, expr2);
  }

  @Override
  public Z3ExprAdapter mkSetIntersect(Z3ExprAdapter expr1, Z3ExprAdapter expr2) {
    checkSet("mkSetIntersect", expr1);
    checkSet("mkSetIntersect", expr2);

    Z3GenExprAdapter set1 = (Z3GenExprAdapter) expr1;
    Z3GenExprAdapter set2 = (Z3GenExprAdapter) expr2;
    Function<Z3ExprAdapter, Z3ExprAdapter> setFunction =
        obj -> mkAnd(mkContains(set1, obj), mkContains(set2, obj));
    Z3ExprAdapter res = mkSet(setFunction, set1.getElement());
    return wrap(res, expr1, expr2);
  }

  @Override
  public Z3ExprAdapter mkSetMinus(Z3ExprAdapter expr1, Z3ExprAdapter expr2) {
    checkSet("mkSetMinus", expr1);
    checkSet("mkSetMinus", expr2);

    Z3GenExprAdapter set1 = (Z3GenExprAdapter) expr1;
    Z3GenExprAdapter set2 = (Z3GenExprAdapter) expr2;
    Function<Z3ExprAdapter, Z3ExprAdapter> setFunction =
        obj -> mkAnd(mkContains(set1, obj), mkNot(mkContains(set2, obj)));
    Z3ExprAdapter res = mkSet(setFunction, set1.getElement());
    return wrap(res, expr1, expr2);
  }

  @Override
  public Z3ExprAdapter mkContains(Z3ExprAdapter expr1, Z3ExprAdapter arg1) {
    Z3ExprAdapter res;
    if (expr1.isStringExpr()) {
      Z3TypeAdapter type = tFactory.mkBoolType();
      res = new Z3ExprAdapter(ctx.mkContains(toCharSeq(expr1), toCharSeq(arg1)), type);
    } else {
      checkGen("mkContains", expr1);
      res = ((Z3GenExprAdapter) expr1).isIn(arg1);
    }

    return wrap(res, expr1, arg1);
  }

  @Override
  public Z3ExprAdapter mkNeq(Z3ExprAdapter left, Z3ExprAdapter right) {
    Z3ExprAdapter res = mkNot(mkEq(left, right));
    return wrap(res);
  }

  @Override
  public Z3ExprAdapter mkExists(List<Z3ExprAdapter> params, Z3ExprAdapter body) {
    List<Z3ExprAdapter> objParams = new ArrayList<>();
    List<Z3ExprAdapter> primParams = new ArrayList<>();
    for (Z3ExprAdapter param : params) {
      boolean check = param.isObjExpr() ? objParams.add(param) : primParams.add(param);
    }

    // quantified primitive variable
    BoolExpr expr = mkExists(revertAdaptation(primParams), (BoolExpr) body.getExpr());

    // quantify CDType variable with
    if (!objParams.isEmpty()) {
      List<ASTCDType> types = collectCDType(objParams);
      List<Expr<?>> vars = revertAdaptation(objParams);
      expr = cd2SMTGenerator.mkExists(types, vars, expr);
    }
    Z3ExprAdapter res = new Z3ExprAdapter(expr, tFactory.mkBoolType());
    return wrap(res, body);
  }

  @Override
  public Z3ExprAdapter mkForall(List<Z3ExprAdapter> params, Z3ExprAdapter body) {
    List<Z3ExprAdapter> objParams = new ArrayList<>();
    List<Z3ExprAdapter> primParams = new ArrayList<>();
    for (Z3ExprAdapter param : params) {
      boolean check = param.isObjExpr() ? objParams.add(param) : primParams.add(param);
    }

    // quantified primitive variable
    BoolExpr expr = mkForAll(revertAdaptation(primParams), (BoolExpr) body.getExpr());

    // quantify CDType variable with
    if (!objParams.isEmpty()) {
      List<ASTCDType> types = collectCDType(objParams);
      List<Expr<?>> vars = revertAdaptation(objParams);
      expr = cd2SMTGenerator.mkForall(types, vars, expr);
    }
    Z3ExprAdapter res = new Z3ExprAdapter(expr, tFactory.mkBoolType());
    return wrap(res, body);
  }

  @Override
  public Z3ExprAdapter getLink(Z3ExprAdapter obj, String role) {
    checkObj("getLink", obj);

    ASTCDType astcdType = obj.getType().getCDType();
    Z3ExprAdapter res;

    // case association Link
    Optional<ASTCDAssociation> association = resolveAssociation(astcdType, role);
    if (association.isPresent()) {
      res = getAssocLink(association.get(), obj, role);
      return wrap(res);
    }

    // case attribute
    Optional<ASTCDAttribute> attribute = resolveAttribute(astcdType, role);
    if (attribute.isPresent()) {
      Expr<?> expr = cd2SMTGenerator.getAttribute(astcdType, role, obj.getExpr());
      Z3TypeAdapter typeAdapter = tFactory.adapt(attribute.get().getMCType());
      res = new Z3ExprAdapter(expr, typeAdapter);
      return wrap(res, obj);
    }

    Log.error("Cannot resolve role or attribute " + role + " for the type " + astcdType.getName());
    return null;
  }

  @Override
  public Z3ExprAdapter getLinkTransitive(Z3ExprAdapter obj, String role) {

    ASTCDType astcdType = obj.getType().getCDType();
    Sort sort = obj.getType().getSort();
    ASTCDAssociation association = CDHelper.getAssociation(astcdType, role, getCD());

    // declare a new function with the like the association function
    FuncDecl<BoolSort> rel =
        ctx.mkFuncDecl(ctx.mkSymbol("assoc_reflexive"), new Sort[] {sort, sort}, ctx.mkBoolSort());
    Z3ExprAdapter obj1 = mkConst("obj1", obj.getType());
    Z3ExprAdapter obj2 = mkConst("obj2", obj.getType());

    // make te new function equivalent to the assoc function
    BoolExpr constr =
        cd2SMTGenerator.evaluateLink(
            association, astcdType, astcdType, obj1.getExpr(), obj2.getExpr());
    Z3ExprAdapter assocConstraint = new Z3ExprAdapter(constr, tFactory.mkBoolType());

    Expr<?> definition = rel.apply(obj1.getExpr(), obj2.getExpr());
    Z3ExprAdapter funcDef = new Z3ExprAdapter(definition, obj.getType());
    Z3ExprAdapter rel_is_assocFunc = mkForall(List.of(obj1, obj2), mkEq(funcDef, assocConstraint));

    // make the new function transitive
    FuncDecl<BoolSort> trans_rel = TransitiveClosure.mkTransitiveClosure(ctx, rel);

    // define the transitive closure with the newly defined function
    Function<Z3ExprAdapter, Z3ExprAdapter> setFunc =
        expr ->
            new Z3ExprAdapter(
                trans_rel.apply(obj.getExpr(), expr.getExpr()), tFactory.mkBoolType());

    // return Set and  with the definition  of the new set as general constraint
    Z3ExprAdapter res = mkSet(setFunc, obj1);
    res.addGenConstraint(rel_is_assocFunc);
    return wrap(res, obj);
  }

  @Override
  public Z3ExprAdapter unWrap(Z3ExprAdapter opt) {
    checkOptional("unWrap", opt);
    Z3GenExprAdapter set = ((Z3GenExprAdapter) opt);
    Z3ExprAdapter res = set.getElement();

    res.setWrapper(bool -> mkExists(List.of(res), mkAnd(bool, set.isIn(res))));
    return wrap(res, opt);
  }

  // todo continuew review here...

  private BoolExpr mkForAll(List<Expr<?>> quanParams, BoolExpr body) {
    if (quanParams.isEmpty()) {
      return body;
    }
    return ctx.mkForall(quanParams.toArray(Expr[]::new), body, 0, null, null, null, null);
  }

  private BoolExpr mkExists(List<Expr<?>> quanParams, BoolExpr body) {
    if (quanParams.isEmpty()) {
      return body;
    }
    return ctx.mkExists(quanParams.toArray(Expr[]::new), body, 0, null, null, null, null);
  }

  public List<Expr<?>> revertAdaptation(List<Z3ExprAdapter> exprList) {
    return exprList.stream().map(Z3ExprAdapter::getExpr).collect(Collectors.toList());
  }

  public List<ASTCDType> collectCDType(List<Z3ExprAdapter> params) {
    return params.stream().map(e -> e.getType().getCDType()).collect(Collectors.toList());
  }

  public Optional<ASTCDAssociation> resolveAssociation(ASTCDType astcdType, String otherRole) {
    return Optional.ofNullable(CDHelper.getAssociation(astcdType, otherRole, getCD()));
  }

  public Optional<ASTCDAttribute> resolveAttribute(ASTCDType astcdType, String attrName) {
    for (ASTCDAttribute attr : CDSymbolTables.getAttributesInHierarchy(astcdType)) {
      if (attr.getName().equals(attrName)) {
        return Optional.of(attr);
      }
    }
    return Optional.empty();
  }

  public Z3ExprAdapter getAssocLink(ASTCDAssociation assoc, Z3ExprAdapter obj, String role) {

    ASTCDType type = obj.getType().getCDType();
    ASTCDType otherType =
        isLeftSide(type, role, getCD())
            ? getRightType(assoc, getCD())
            : getLeftType(assoc, getCD());

    String linkName = obj.getExpr() + "_" + role + "_";
    Z3ExprAdapter otherObj = mkConst(linkName, tFactory.adapt(otherType));

    Function<Z3ExprAdapter, Z3ExprAdapter> link;
    ASTCDCardinality cardinality;

    if (isLeftSide(obj.getType().getCDType(), role, getCD())) {
      cardinality = assoc.getRight().getCDCardinality();
      link =
          expr ->
              new Z3ExprAdapter(
                  cd2SMTGenerator.evaluateLink(
                      assoc, type, otherType, obj.getExpr(), expr.getExpr()),
                  tFactory.mkBoolType());
    } else {
      cardinality = assoc.getLeft().getCDCardinality();
      link =
          expr ->
              new Z3ExprAdapter(
                  cd2SMTGenerator.evaluateLink(
                      assoc, otherType, type, expr.getExpr(), obj.getExpr()),
                  tFactory.mkBoolType());
    }

    Z3ExprAdapter res;
    if (cardinality.isOne()) {
      otherObj.setWrapper(bool -> mkExists(List.of(otherObj), mkAnd(bool, link.apply(otherObj))));
      res = otherObj;
    } else if (cardinality.isOpt()) {
      res = mkOptional(link, mkConst(linkName, tFactory.adapt(otherType)));
    } else {
      res = mkSet(link, mkConst(linkName, tFactory.adapt(otherType)));
    }
    return res;
  }

  public static boolean isLeftSide(ASTCDType astcdType, String otherRole, ASTCDDefinition cd) {
    List<ASTCDType> objTypes = new ArrayList<>();
    objTypes.add(astcdType);
    objTypes.addAll(CDHelper.getSuperTypeAllDeep(astcdType, cd));

    ASTCDType leftType;
    ASTCDType rightType;
    String leftRole;
    String rightRole;

    for (ASTCDAssociation association : cd.getCDAssociationsList()) {
      leftType = CDHelper.getASTCDType(association.getLeftQualifiedName().getQName(), cd);
      rightType = CDHelper.getASTCDType(association.getRightQualifiedName().getQName(), cd);
      leftRole = association.getLeft().getCDRole().getName();
      rightRole = association.getRight().getCDRole().getName();

      if (objTypes.contains(leftType) && otherRole.equals(rightRole)) {
        return true;
      } else if (objTypes.contains(rightType) && otherRole.equals(leftRole)) {
        return false;
      }
    }
    Log.error(
        "Association with the other-role "
            + otherRole
            + " not found for the ASTCDType"
            + astcdType.getName());
    return false;
  }

  private void checkBool(String method, Z3ExprAdapter node) {
    if (!node.isBoolExpr()) {
      Log.error(String.format(wrongParam, method, node.getType().getName(), "bool"));
    }
  }

  public void checkObj(String method, Z3ExprAdapter node) {
    if (!node.isObjExpr()) {
      Log.error(String.format(wrongParam, method, node.getType().getName(), " as CDType"));
    }
  }

  private void checkArith(String method, Z3ExprAdapter node) {
    if (!node.isIntExpr() && !node.isDoubleExpr() && !node.isCharExpr()) {
      Log.error(String.format(wrongParam, method, node.getType(), "int or double"));
    }
  }

  private void checkString(String method, Z3ExprAdapter node) {
    if (!node.isStringExpr()) {
      Log.error(String.format(wrongParam, method, node.getType(), "String"));
    }
  }

  private void checkInt(String method, Z3ExprAdapter node) {
    if (!node.isIntExpr()) {
      Log.error(String.format(wrongParam, method, node.getType(), "int"));
    }
  }

  private void checkSet(String method, Z3ExprAdapter node) {
    if (!node.isSetExpr()) {
      Log.error(String.format(wrongParam, method, node.getType(), "'Set'"));
    }
  }

  private void checkGen(String method, Z3ExprAdapter node) {
    if (!node.isSetExpr() && !node.isOptExpr()) {
      Log.error(String.format(wrongParam, method, node.getType(), "'Set<?> or Optional<?>'"));
    }
  }

  private void checkOptional(String method, Z3ExprAdapter node) {
    if (!node.isOptExpr()) {
      Log.error(String.format(wrongParam, method, node.getType(), "'Optional'"));
    }
  }

  private Expr<SeqSort<Sort>> toCharSeq(Z3ExprAdapter expr) {
    return (Expr<SeqSort<Sort>>) expr.getExpr();
  }

  private Z3ExprAdapter wrap(Z3ExprAdapter parent, Z3ExprAdapter... children) {
    if (parent.isBoolExpr() && children.length == 0) {
      return parent;
    }

    Function<Z3ExprAdapter, Z3ExprAdapter> wrapper = expr -> expr;
    Z3ExprAdapter genConstraint = mkBool(true);

    for (Z3ExprAdapter child : children) {
      if (child.isPresentWrapper()) {
        Function<Z3ExprAdapter, Z3ExprAdapter> finalWrapper = wrapper;
        wrapper = bool -> child.getWrapper().apply(finalWrapper.apply(bool));
      }

      if (child.isPresentGenConstr()) {
        genConstraint = mkAnd(genConstraint, child.getGenConstraint());
      }
    }

    if (parent.isPresentGenConstr()) {
      genConstraint = mkAnd(genConstraint, parent.getGenConstraint());
    }

    Z3ExprAdapter res = parent.isBoolExpr() ? wrapper.apply(parent) : parent;
    res.addGenConstraint(genConstraint);
    return res;
  }

  public ASTCDDefinition getCD() {
    return cd2SMTGenerator.getClassDiagram().getCDDefinition();
  }
}
