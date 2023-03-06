package de.monticore.ocl2smt.helpers;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Sort;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.Helper.SMTHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd4analysis.CD4AnalysisMill;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdbasis._visitor.CDBasisTraverser;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.LiteralConverter;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.ocl2smt.ocldiff.operationDiff.OPConstraint;
import de.monticore.ocl2smt.trafo.BuildPreCDTrafo;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.od4report.OD4ReportMill;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odbasis._ast.ASTODAttribute;
import de.monticore.odbasis._ast.ASTODElement;
import de.monticore.odbasis._ast.ASTODNamedObject;
import de.monticore.odlink._ast.ASTODLink;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.mcbasictypes._prettyprint.MCBasicTypesFullPrettyPrinter;
import de.monticore.umlmodifier._ast.ASTModifier;
import de.monticore.umlstereotype._ast.ASTStereotype;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class OCLHelper {

  public static ASTCDAssociation getAssociation(
      OCLType type, String otherRole, ASTCDDefinition cd) {
    return CDHelper.getAssociation(CDHelper.getASTCDType(type.getName(), cd), otherRole, cd);
  }

  public static OCLType getOtherType(ASTCDAssociation association, OCLType type) {
    OCLType type1 = OCLType.buildOCLType(association.getLeftQualifiedName().getQName());
    OCLType type2 = OCLType.buildOCLType(association.getRightQualifiedName().getQName());
    if (type.equals(type1)) {
      return type2;
    } else {
      return type1;
    }
  }

  public static List<ASTODNamedObject> getObjectList(ASTODArtifact od) {
    return od.getObjectDiagram().getODElementList().stream()
        .filter(x -> x instanceof ASTODNamedObject)
        .map(x -> (ASTODNamedObject) x)
        .collect(Collectors.toList());
  }

  public static List<ASTODLink> getLinkList(ASTODArtifact od) {

    return od.getObjectDiagram().getODElementList().stream()
        .filter(x -> x instanceof ASTODLink)
        .map(x -> (ASTODLink) x)
        .collect(Collectors.toList());
  }

  public static BoolExpr evaluateLink(
      ASTCDAssociation association,
      Expr<? extends Sort> obj1,
      Expr<? extends Sort> obj2,
      CD2SMTGenerator cd2SMTGenerator,
      LiteralConverter cc) {

    ASTCDDefinition cd = cd2SMTGenerator.getClassDiagram().getCDDefinition();

    ASTCDType left = CDHelper.getLeftType(association, cd);
    ASTCDType right = CDHelper.getRightType(association, cd);

    OCLType type1 = cc.getType(obj1);
    BoolExpr res;
    if (left.getName().equals(type1.getName())) {
      res = cd2SMTGenerator.evaluateLink(association, left, right, obj1, obj2);
    } else {
      res = cd2SMTGenerator.evaluateLink(association, right, left, obj1, obj2);
    }

    return res;
  }

  public static Expr<? extends Sort> getAttribute(
      Expr<? extends Sort> obj,
      OCLType type,
      String attributeName,
      CD2SMTGenerator cd2SMTGenerator) {

    return cd2SMTGenerator.getAttribute(
        CDHelper.getASTCDType(type.getName(), cd2SMTGenerator.getClassDiagram().getCDDefinition()),
        attributeName,
        obj);
  }

  public static OCLOPWitness splitPreOD(
      ASTOCLMethodSignature method, ASTODArtifact od, Model model, OPConstraint opConstraint) {
    List<ASTODElement> preOdElements = new ArrayList<>();
    List<ASTODElement> postOdElements = new ArrayList<>();

    // split links
    for (ASTODNamedObject object : OCLHelper.getObjectList(od)) {
      Pair<ASTODNamedObject, ASTODNamedObject> objects = splitPreObject(object);
      preOdElements.add(objects.getLeft());
      postOdElements.add(objects.getRight());
    }

    // split objects
    for (ASTODLink link : OCLHelper.getLinkList(od)) {
      if (isPreLink(link)) {
        preOdElements.add(preLink2Link(link));
      } else {
        postOdElements.add(link);
      }
    }

    ASTODArtifact preOD =
        de.monticore.cd2smt.Helper.ODHelper.buildOD(
            "pre_" + od.getObjectDiagram().getName(), preOdElements);

    ASTODArtifact postOD =
        de.monticore.cd2smt.Helper.ODHelper.buildOD(
            "post_" + od.getObjectDiagram().getName(), postOdElements);

    return setStereotypes(method, new OCLOPWitness(method, preOD, postOD), model, opConstraint);
  }

  private static OCLOPWitness setStereotypes(
      ASTOCLMethodSignature method, OCLOPWitness diff, Model model, OPConstraint opConstraint) {
    setThisStereotypes(diff.getPreOD(), model, opConstraint);
    setThisStereotypes(diff.getPostOD(), model, opConstraint);

    setResultStereotypes(diff.getPostOD(), method, model, opConstraint);
    return diff;
  }

  private static void setThisStereotypes(ASTODArtifact od, Model model, OPConstraint opConstraint) {

    for (ASTODNamedObject obj : OCLHelper.getObjectList(od)) {
      if (isThis(obj, model, opConstraint.getThisObj())) {
        obj.setModifier(buildModifier("this", "true"));
      }
    }
  }

  private static ASTModifier buildModifier(String stereotypeName, String stereotypeValue) {
    return OD4ReportMill.modifierBuilder()
        .setStereotype(buildStereotype(stereotypeName, stereotypeValue))
        .build();
  }

  private static ASTStereotype buildStereotype(String stereotypeName, String stereotypeValue) {
    return OD4ReportMill.stereotypeBuilder()
        .addValues(
            OD4ReportMill.stereoValueBuilder()
                .setName(stereotypeName)
                .setContent(stereotypeValue)
                .setText(OD4ReportMill.stringLiteralBuilder().setSource(stereotypeValue).build())
                .build())
        .build();
  }

  private static void setResultStereotypes(
      ASTODArtifact od, ASTOCLMethodSignature method, Model model, OPConstraint opConstraint) {

    if (!opConstraint.isPresentResult()) {
      if (!new MCBasicTypesFullPrettyPrinter(new IndentPrinter())
          .prettyprint(method.getMCReturnType())
          .equals("void")) {
        od.getObjectDiagram().setStereotype(buildStereotype("result", "Unspecified"));
      }
    } else {
      OCLType resultType = opConstraint.getResultType();
      if (resultType.isPrimitiv()) {
        String res = model.evaluate(opConstraint.getResult(), true).getSExpr();
        od.getObjectDiagram().setStereotype(buildStereotype("result", res));
      } else {
        Expr<? extends Sort> result = model.evaluate(opConstraint.getResult(), true);
        String resName = SMTHelper.buildObjectName(result);
        ASTODNamedObject obj =
            OCLHelper.getObjectList(od).stream()
                .filter(x -> x.getName().equals(resName))
                .findFirst()
                .orElse(null);

        assert obj != null;
        obj.setModifier(buildModifier("result", "true"));
      }
    }
  }

  private static boolean isThis(ASTODNamedObject obj, Model model, Expr<? extends Sort> thisObj) {
    return obj.getName().equals(SMTHelper.buildObjectName(model.evaluate(thisObj, true)));
  }

  private static boolean isPreLink(ASTODLink link) {
    return isPre(link.getODLinkLeftSide().getRole()) && isPre(link.getODLinkRightSide().getRole());
  }

  private static Pair<ASTODNamedObject, ASTODNamedObject> splitPreObject(ASTODNamedObject object) {
    // split attributes
    List<ASTODAttribute> postAttributeList =
        object.getODAttributeList().stream()
            .filter(a -> !isPre(a.getName()))
            .collect(Collectors.toList());

    List<ASTODAttribute> preObjAttributeList =
        object.getODAttributeList().stream()
            .filter(a -> isPre(a.getName()))
            .collect(Collectors.toList());
    preObjAttributeList.forEach(a -> a.setName(removePre(a.getName())));

    String type =
        new MCBasicTypesFullPrettyPrinter(new IndentPrinter())
            .prettyprint(object.getMCObjectType());
    ASTODNamedObject preObj =
        de.monticore.cd2smt.Helper.ODHelper.buildObject(
            object.getName(), type, preObjAttributeList);

    ASTODNamedObject obj =
        de.monticore.cd2smt.Helper.ODHelper.buildObject(object.getName(), type, postAttributeList);
    return new ImmutablePair<>(preObj, obj);
  }

  private static ASTODLink preLink2Link(ASTODLink preLink) {
    preLink.getODLinkLeftSide().setRole(removePre(preLink.getODLinkLeftSide().getRole()));
    preLink.getODLinkRightSide().setRole(removePre(preLink.getODLinkRightSide().getRole()));
    return preLink;
  }

  public static void buildPreCD(ASTCDCompilationUnit ast) {
    final BuildPreCDTrafo preAttributeTrafo = new BuildPreCDTrafo();

    final CDBasisTraverser traverser = CD4AnalysisMill.traverser();
    traverser.add4CDBasis(preAttributeTrafo);
    traverser.setCDBasisHandler(preAttributeTrafo);
    ast.accept(traverser);
  }

  public static String mkPre(String s) {
    return s + "__pre";
  }

  public static boolean isPre(String s) {
    return s.endsWith("__pre");
  }

  public static String removePre(String s) {
    if (isPre(s)) {
      return s.substring(0, s.length() - 5);
    }
    return s;
  }

  public static boolean containsAttribute(
      ASTCDType astCdType, String attributeName, ASTCDDefinition cd) {
    if (CDHelper.containsProperAttribute(astCdType, attributeName)) {
      return true;
    }
    List<ASTCDType> superclassList = CDHelper.getSuperTypeList(astCdType, cd);

    for (ASTCDType superType : superclassList) {
      boolean res = containsAttribute(superType, attributeName, cd);

      if (res) {
        return res;
      }
    }

    return false;
  }
}
