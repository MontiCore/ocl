package de.monticore.od2smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.monticore.ast.ASTNode;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdbasis._symboltable.CDTypeSymbol;
import de.monticore.cddiff.alloycddiff.CDSemantics;
import de.monticore.cdinterfaceandenum._ast.ASTCDEnum;
import de.monticore.cdinterfaceandenum._ast.ASTCDEnumConstant;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.literals.mccommonliterals._ast.ASTStringLiteral;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.OCLExpressionConverter;
import de.monticore.odbasis._ast.*;
import de.monticore.odlink._ast.ASTODLink;
import de.monticore.odvalidity.OD2CDMatcher;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class OD2SMTGenerator implements IOD2SMTGenerator {
  protected static int count_object = 0;
  protected CD2SMTGenerator cd2SMTGenerator;
  protected ASTCDCompilationUnit cd;
  protected ASTODArtifact od;
  protected Context ctx;
  protected OCLExpressionConverter exprConv; // TODO: update to Expression2SMT

  protected Map<ASTODObject, Expr<?>> objectsMap = new HashMap<>();
  protected Map<ASTODObject, Set<IdentifiableBoolExpr>> objectConstraint = new HashMap<>();
  protected Map<ASTODLink, IdentifiableBoolExpr> linksConstraints = new HashMap<>();

  public void od2smt(ASTODArtifact od, ASTCDCompilationUnit cd, Context ctx) {
    if (!match(od, cd)) {
      Log.error(
          "The Object Diagram "
              + od.get_SourcePositionStart().getFileName().orElse(od.getObjectDiagram().getName())
              + " don't Match to the Class Diagram "
              + cd.get_SourcePositionStart().getFileName().orElse(cd.getCDDefinition().getName()));
    }

    // init the expression converter
    exprConv = new OCLExpressionConverter(cd, ctx);
    cd2SMTGenerator = exprConv.getCd2smtGenerator();
    this.ctx = ctx;
    this.cd = cd;
    this.od = od;

    // convert objects
    OD2SMTUtils.getObjectList(od).forEach(this::convert);

    // convert links
    OD2SMTUtils.getLinkList(od).forEach(this::convert);
  }

  @Override
  public Expr<?> getObject(ASTODObject namedObject) {
    return objectsMap.get(namedObject);
  }

  @Override
  public Set<IdentifiableBoolExpr> getObjectConstraints(ASTODObject object) {
    return objectConstraint.get(object);
  }

  @Override
  public IdentifiableBoolExpr getLinkConstraint(ASTODLink link) {
    return linksConstraints.get(link);
  }

  @Override
  public Set<IdentifiableBoolExpr> getAllODConstraints() {
    Set<IdentifiableBoolExpr> res =
        objectConstraint.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    res.addAll(linksConstraints.values());
    return res;
  }

  private void convert(ASTODObject node) {
    Set<IdentifiableBoolExpr> objectConstr = new HashSet<>();
    String objectName = "object_" + count_object;
    count_object++;

    ASTCDType objType = OD2SMTUtils.getObjectType(node, cd.getCDDefinition());
    Expr<?> objExpr = ctx.mkConst(objectName, cd2SMTGenerator.getSort(objType));
    objectConstr.add(
        IdentifiableBoolExpr.buildIdentifiable(
            cd2SMTGenerator.hasType(objExpr, objType),
            node.get_SourcePositionStart(),
            Optional.of(objectName + "_type")));

    // make constraints for attribute Value Value
    for (ASTODAttribute attr : node.getODAttributeList()) {
      if (attr.isPresentODValue()) {

        Expr<?> value = convertValue(attr);
        Expr<?> attrExpr = cd2SMTGenerator.getAttribute(objType, attr.getName(), objExpr);
        BoolExpr constr = ctx.mkAnd(ctx.mkEq(attrExpr, value));

        IdentifiableBoolExpr attrConstr =
            IdentifiableBoolExpr.buildIdentifiable(
                constr,
                attr.get_SourcePositionStart(),
                Optional.of(objectName + "_" + attr.getName()));
        objectConstr.add(attrConstr);
      }
    }
    objectConstraint.put(node, objectConstr);
    objectsMap.put(node, objExpr);
  }

  private Expr<?> convertValue(ASTODAttribute attr) {
    Expr<? extends Sort> res = null;
    if (CDHelper.isDateType(attr.getMCType())) {
      ASTLiteralExpression value =
          (ASTLiteralExpression) ((ASTODSimpleAttributeValue) attr.getODValue()).getExpression();
      res = convertDateValue(value);

    } else if (CDHelper.isPrimitiveType(attr.getMCType())) {
      ASTExpression value = ((ASTODSimpleAttributeValue) attr.getODValue()).getExpression();
      res = exprConv.convertExpr(value).expr();
      ;
    } else if (CDHelper.isEnumType(cd.getCDDefinition(), attr.getMCType().printType())) {
      res = convertEnumValue((ASTODName) attr.getODValue(), attr.getMCType());
    } else {
      notFullyImplemented(attr);
    }

    return res;
  }

  private Expr<? extends Sort> convertEnumValue(ASTODName odValue, ASTMCType type) {
    // get enumeration
    Optional<CDTypeSymbol> enumSymbol = cd.getEnclosingScope().resolveCDType(type.printType());
    assert enumSymbol.isPresent();
    ASTCDEnum enums = (ASTCDEnum) enumSymbol.get().getAstNode();

    // get enum constant
    ASTCDEnumConstant enumConst =
        enums.getCDEnumConstantList().stream()
            .filter(c -> c.getName().equals(odValue.getName()))
            .findAny()
            .orElse(null);

    return cd2SMTGenerator.getEnumConstant(enums, enumConst);
  }

  private Expr<?> convertDateValue(ASTLiteralExpression node) {
    assert node.getLiteral() instanceof ASTStringLiteral;
    LocalDateTime date = null;

    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss");
      date = LocalDateTime.parse(((ASTStringLiteral) node.getLiteral()).getValue(), formatter);

    } catch (DateTimeParseException e) {
      Log.error(
          "The date value format is not correct. It should be yyyy-MM-dd' 'HH:mm:ss. eg:2023-05-28 10:30:00 ");
    }

    return CDHelper.date2smt(date, ctx);
  }

  private void convert(ASTODLink link) {
    // get left objects
    List<Expr<?>> leftObjs =
        link.getLeftReferenceNames().stream()
            .map(name -> objectsMap.get(OD2SMTUtils.getObject(name, od)))
            .collect(Collectors.toList());

    // get the right Objects
    List<Expr<?>> rightObjs =
        link.getRightReferenceNames().stream()
            .map(name -> objectsMap.get(OD2SMTUtils.getObject(name, od)))
            .collect(Collectors.toList());

    // compute types of Objects
    ASTCDType leftType =
        OD2SMTUtils.getObjectType(link.getLeftReferenceNames().get(0), od, cd.getCDDefinition());
    ASTCDType rightType =
        OD2SMTUtils.getObjectType(link.getRightReferenceNames().get(0), od, cd.getCDDefinition());
    ASTCDAssociation association = null;

    // getAssociation
    if (link.getODLinkLeftSide().isPresentRole()) {
      association =
          CDHelper.getAssociation(
              rightType, link.getODLinkLeftSide().getRole(), cd.getCDDefinition());
    } else if (link.getODLinkRightSide().isPresentRole()) {
      association =
          CDHelper.getAssociation(
              rightType, link.getODLinkRightSide().getRole(), cd.getCDDefinition());

    } else {
      notFullyImplemented(link);
      Log.error("Conversion of ODLink only possible when role is present at at least one side ");
      assert false;
    }

    // build constraints
    List<BoolExpr> constr = new ArrayList<>(List.of(ctx.mkTrue()));
    for (Expr<?> leftObj : leftObjs) {
      for (Expr<?> rightObj : rightObjs) {
        constr.add(
            cd2SMTGenerator.evaluateLink(association, leftType, rightType, leftObj, rightObj));
      }
    }

    linksConstraints.put(
        link,
        IdentifiableBoolExpr.buildIdentifiable(
            ctx.mkAnd(constr.toArray(new BoolExpr[0])),
            link.get_SourcePositionStart(),
            Optional.of("link_constraints")));
  }

  private boolean match(ASTODArtifact od, ASTCDCompilationUnit cd) {
    return new OD2CDMatcher().checkODValidity(CDSemantics.SIMPLE_CLOSED_WORLD, od, cd);
  }

  private void notFullyImplemented(ASTNode node) {
    Log.error(
        this.getClass().getName()
            + "Conversion of expression with the type "
            + node.getClass().getName()
            + " is not fully implemented");
  }
}
