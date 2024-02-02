package de.monticore.ocl2smt.ocl2smt.expr2smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory.CDExprFactory;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Z3CDExprFactory implements CDExprFactory<Z3ExprAdapter, Sort> {
  private final Context ctx;
  private final CD2SMTGenerator cd2SMTGenerator;
  private final Z3TypeFactory tFactory;

  public Z3CDExprFactory(Z3TypeFactory tFactory, CD2SMTGenerator cd2SMTGenerator) {
    this.cd2SMTGenerator = cd2SMTGenerator;
    this.tFactory = tFactory;
    this.ctx = cd2SMTGenerator.getContext();
  }

  @Override
  public Z3ExprAdapter mkExists(List<Z3ExprAdapter> params, Z3ExprAdapter body) {
    // quantified boolean variable
    List<Z3ExprAdapter> boolExprList = filterExpr(params, Set.of(ExpressionKind.BOOL));
    BoolExpr subRes = mkExists(revertAdaptation(boolExprList), (BoolExpr) body.getExpr());

    // quantify CDType variable with
    List<Z3ExprAdapter> objList = filterExpr(params, Set.of(ExpressionKind.OO));
    List<ASTCDType> types = collectCDType(params);
    List<Expr<?>> vars = revertAdaptation(objList);

    BoolExpr res = cd2SMTGenerator.mkExists(types, vars, subRes);
    return new Z3ExprAdapter(res, tFactory.mkBoolType());
  }

  @Override
  public Z3ExprAdapter mkForall(List<Z3ExprAdapter> params, Z3ExprAdapter body) {
    // quantified boolean variable
    List<Z3ExprAdapter> boolExprList = filterExpr(params, Set.of(ExpressionKind.BOOL));
    BoolExpr subRes = mkForAll(revertAdaptation(boolExprList), (BoolExpr) body.getExpr());

    // quantify CDType variable with
    List<Z3ExprAdapter> objList = filterExpr(params, Set.of(ExpressionKind.OO));
    List<ASTCDType> types = collectCDType(params);
    List<Expr<?>> vars = revertAdaptation(objList);

    BoolExpr res = cd2SMTGenerator.mkForall(types, vars, subRes);
    return new Z3ExprAdapter(res, tFactory.mkBoolType());
  }

  @Override
  public Z3ExprAdapter getLink(Z3ExprAdapter obj, String link) {
    checkPreCond(obj);
    Z3ExprAdapter res = null;
    ASTCDType astcdType = obj.getExprType().getCDType();
    ;

    // case attribute
    Optional<ASTCDAttribute> attribute = resolveAttribute(astcdType, link);
    if (attribute.isPresent()) {
      Expr<?> expr = cd2SMTGenerator.getAttribute(astcdType, link, obj.getExpr());
      Z3TypeAdapter typeAdapter = tFactory.adapt(attribute.get().getMCType());
      res = new Z3ExprAdapter(expr, typeAdapter);
    }

    return res;
  }

  public static void checkPreCond(Z3ExprAdapter obj) {
    String message =
        "Method getLink(...) expected an object expression as first parameter but got %s ";
    if (!obj.isObjExpr()) {
      Log.error(String.format(message));
    }
  }

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

  public List<Z3ExprAdapter> filterExpr(List<Z3ExprAdapter> exprList, Set<ExpressionKind> filter) {
    return exprList.stream()
        .filter(e -> filter.contains(e.getExprType().getKind()))
        .collect(Collectors.toList());
  }

  public List<Expr<?>> revertAdaptation(List<Z3ExprAdapter> exprList) {
    return exprList.stream().map(Z3ExprAdapter::getExpr).collect(Collectors.toList());
  }

  public List<ASTCDType> collectCDType(List<Z3ExprAdapter> params) {
    return params.stream().map(e -> e.getExprType().getCDType()).collect(Collectors.toList());
  }

  public Optional<ASTCDAssociation> resolveAssociation(ASTCDType astcdType, String otherRole) {
    ASTCDDefinition cd = cd2SMTGenerator.getClassDiagram().getCDDefinition();
    return Optional.ofNullable(CDHelper.getAssociation(astcdType, otherRole, cd));
  }

  public Optional<ASTCDAttribute> resolveAttribute(ASTCDType astcdType, String attribute) {
    return Optional.ofNullable(CDHelper.getAttribute(astcdType, attribute));
  }
  /*
  public static ExprTypeAdapter getOtherType(
          ASTCDAssociation association, ExprTypeAdapter type, String otherRole, ASTCDDefinition cd) {
      ExprTypeAdapter type1 =
              ExprTypeAdapter.buildOCLType(association.getLeftQualifiedName().getQName());
      ExprTypeAdapter type2 =
              ExprTypeAdapter.buildOCLType(association.getRightQualifiedName().getQName());
      if (isLeftSide(CDHelper.getASTCDType(type.getName(), cd), otherRole, cd)) {
          return type2;
      } else {
          return type1;
      }
  }*/
  /*
  public static BoolExpr evaluateLink(
          ASTCDAssociation association,
          ExprBuilder obj,
          String otherRole,
          ExprBuilder otherObj,
          CD2SMTGenerator cd2SMTGenerator,
          Function<ExprBuilder, OCLType> types) {

      ASTCDDefinition cd = cd2SMTGenerator.getClassDiagram().getCDDefinition();
      OCLType oclType = types.apply(obj);
      ASTCDType type = CDHelper.getASTCDType(oclType.getName(), cd);

      ASTCDType otherType = CDHelper.getASTCDType(types.apply(otherObj).getName(), cd);

      BoolExpr res;
      if (isLeftSide(CDHelper.getASTCDType(type.getName(), cd), otherRole, cd)) {
          res = cd2SMTGenerator.evaluateLink(association, type, otherType, obj.expr(), otherObj.expr());
      } else {
          res = cd2SMTGenerator.evaluateLink(association, otherType, type, otherObj.expr(), obj.expr());
      }

      return res;
  }*/

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
}
