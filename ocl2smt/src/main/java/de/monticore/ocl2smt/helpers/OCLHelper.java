package de.monticore.ocl2smt.helpers;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.Helper.SMTHelper;
import de.monticore.cd4analysis.CD4AnalysisMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdbasis._visitor.CDBasisTraverser;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.ocl2smt.ocldiff.operationDiff.OPConstraint;
import de.monticore.ocl2smt.trafo.BuildPreCDTrafo;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class OCLHelper {

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
    Z3ExprAdapter result = opConstraint.getResult();
    if (!opConstraint.isPresentResult()) {
      od.getObjectDiagram().setStereotype(buildStereotype("result", "Unspecified"));
    } else {

      if (result.getType().isNative()) {
        String res = model.evaluate((Expr<? extends Sort>) result.getExpr(), true).getSExpr();
        od.getObjectDiagram().setStereotype(buildStereotype("result", res));
      } else if (result.isObjExpr()) {
        Expr<? extends Sort> resultExpr =
            model.evaluate((Expr<? extends Sort>) result.getExpr(), true);
        ASTODNamedObject obj = getObjectWithExpr(result.getType().getCDType(), resultExpr, od);

        obj.setModifier(buildModifier("result", "true"));
      } else if (result.isSetExpr()) {
        Log.error("Setting result of Operation constraint not fully impelemented "); // todo fixme
        /*   List<Expr<? extends Sort>> resList = new ArrayList<>();
        FuncDecl<BoolSort> seFunc = result.getResultSet(;
        for (Expr<? extends Sort> expr : model.getSortUniverse(seFunc.getDomain()[0])) {
          if (model.evaluate(seFunc.apply(expr), true).getBoolValue() == Z3_lbool.Z3_L_TRUE) {
            resList.add(expr);
          }
        }
        List<ASTODNamedObject> resObjList =
            resList.stream()
                .map(expr -> getObjectWithExpr(result.getExprType().getCDType(), expr, od))
                .collect(Collectors.toList());
        resObjList.forEach(obj -> obj.setModifier(buildModifier("result", "true")));*/
      }
    }
  }

  private static boolean isThis(
      ASTODNamedObject obj, Model model, TypeAdapter type, Expr<? extends Sort> thisObj) {
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

    final CDBasisTraverser traverser = CD4AnalysisMill.inheritanceTraverser();
    traverser.add4CDBasis(preAttributeTrafo);
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
      ASTCDType type, Expr<? extends Sort> expr, ASTODArtifact od) {
    return OCLHelper.getObjectList(od).stream()
        .filter(x -> x.getName().equals(SMTHelper.buildObjectName(expr, type.getName())))
        .findFirst()
        .orElse(null);
  }

  public static List<ASTOCLInvariant> collectInv(Set<ASTOCLCompilationUnit> ocl) {
    return ocl.stream()
        .map(ast -> ast.getOCLArtifact().getOCLConstraintList())
        .flatMap(List::stream)
        .filter(c -> c instanceof ASTOCLInvariant)
        .map(c -> (ASTOCLInvariant) c)
        .collect(Collectors.toList());
  }

  public List<ASTODLink> getLinks(ASTODArtifact od) {
    List<ASTODLink> links = new ArrayList<>();
    for (ASTODElement element : od.getObjectDiagram().getODElementList()) {
      if (element instanceof ASTODLink) {
        links.add((ASTODLink) element);
      }
    }
    return links;
  }
}
