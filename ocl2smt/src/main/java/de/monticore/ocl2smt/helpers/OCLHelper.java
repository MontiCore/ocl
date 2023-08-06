package de.monticore.ocl2smt.helpers;

import com.microsoft.z3.*;
import com.microsoft.z3.enumerations.Z3_lbool;
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
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.ocl2smt.ocldiff.operationDiff.OPConstraint;
import de.monticore.ocl2smt.trafo.BuildPreCDTrafo;
import de.monticore.ocl2smt.util.OCLMethodResult;
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
import de.se_rwth.commons.logging.Log;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OCLHelper {

  public static ASTCDAssociation getAssociation(
      OCLType type, String otherRole, ASTCDDefinition cd) {
    return CDHelper.getAssociation(CDHelper.getASTCDType(type.getName(), cd), otherRole, cd);
  }

  public static OCLType getOtherType(
      ASTCDAssociation association, OCLType type, String otherRole, ASTCDDefinition cd) {
    OCLType type1 = OCLType.buildOCLType(association.getLeftQualifiedName().getQName());
    OCLType type2 = OCLType.buildOCLType(association.getRightQualifiedName().getQName());
    if (isLeftSide(CDHelper.getASTCDType(type.getName(), cd), otherRole, cd)) {
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
      Expr<? extends Sort> obj,
      String otherRole,
      Expr<? extends Sort> otherObj,
      CD2SMTGenerator cd2SMTGenerator,
      Function<Expr<?>, OCLType> types) {

    ASTCDDefinition cd = cd2SMTGenerator.getClassDiagram().getCDDefinition();
    OCLType oclType = types.apply(obj);
    ASTCDType type = CDHelper.getASTCDType(oclType.getName(), cd);

    ASTCDType otherType = CDHelper.getASTCDType(types.apply(otherObj).getName(), cd);

    BoolExpr res;
    if (isLeftSide(CDHelper.getASTCDType(type.getName(), cd), otherRole, cd)) {
      res = cd2SMTGenerator.evaluateLink(association, type, otherType, obj, otherObj);
    } else {
      res = cd2SMTGenerator.evaluateLink(association, otherType, type, otherObj, obj);
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
    if (opConstraint == null) {
      return new OCLOPWitness(method, preOD, postOD);
    } else {
      return setStereotypes(new OCLOPWitness(method, preOD, postOD), model, opConstraint);
    }
  }

  private static OCLOPWitness setStereotypes(
      OCLOPWitness diff, Model model, OPConstraint opConstraint) {
    setThisStereotypes(diff.getPreOD(), model, opConstraint);
    setThisStereotypes(diff.getPostOD(), model, opConstraint);

    setResultStereotypes(diff.getPostOD(), model, opConstraint);
    return diff;
  }

  private static void setThisStereotypes(ASTODArtifact od, Model model, OPConstraint opConstraint) {

    for (ASTODNamedObject obj : OCLHelper.getObjectList(od)) {
      if (isThis(obj, model, opConstraint.getThisType(), opConstraint.getThisObj())) {
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
      ASTODArtifact od, Model model, OPConstraint opConstraint) {
    OCLMethodResult result = opConstraint.getResult();
    if (!opConstraint.isPresentResult()) {
      if (result.isVoid()) {
        od.getObjectDiagram().setStereotype(buildStereotype("result", "Unspecified"));
      }
    } else {

      if (result.isPrimitive()) {
        String res = model.evaluate(result.getResultExpr(), true).getSExpr();
        od.getObjectDiagram().setStereotype(buildStereotype("result", res));
      } else if (result.isObject()) {
        Expr<? extends Sort> resultExpr = model.evaluate(result.getResultExpr(), true);
        ASTODNamedObject obj = getObjectWithExpr(result.getOclType(), resultExpr, od);

        obj.setModifier(buildModifier("result", "true"));
      } else if (result.isSetOfObject()) {
          List<Expr<? extends Sort>> resList = new ArrayList<>();
          FuncDecl<BoolSort> seFunc = result.getResultSet();
          for (Expr<? extends Sort> expr : model.getSortUniverse(seFunc.getDomain()[0])) {
              if (model.evaluate(seFunc.apply(expr), true).getBoolValue() == Z3_lbool.Z3_L_TRUE) {
                  resList.add(expr);
              }
          }
          List<ASTODNamedObject> resObjList =
                  resList.stream()
                          .map(expr -> getObjectWithExpr(result.getOclType(), expr, od))
                          .collect(Collectors.toList());
          resObjList.forEach(obj -> obj.setModifier(buildModifier("result", "true")));
      }
    }
  }

    private static boolean isThis(
            ASTODNamedObject obj, Model model, OCLType type, Expr<? extends Sort> thisObj) {
        return obj.getName()
                .equals(SMTHelper.buildObjectName(model.evaluate(thisObj, true), type.getName()));
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
            return true;
        }
    }

      return false;
  }

    public static ASTODNamedObject getObjectWithExpr(
            OCLType type, Expr<? extends Sort> expr, ASTODArtifact od) {
        return OCLHelper.getObjectList(od).stream()
                .filter(x -> x.getName().equals(SMTHelper.buildObjectName(expr, type.getName())))
                .findFirst()
                .orElse(null);
    }
}
