package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Sort;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd4analysis.CD4AnalysisMill;
import de.monticore.cdassociation._ast.ASTCDAssocLeftSide;
import de.monticore.cdassociation._ast.ASTCDAssocRightSide;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdbasis._visitor.CDBasisTraverser;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.trafo.BuildPreCDTrafo;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.ocl2smt.util.OPDiffResult;
import de.monticore.od4report.OD4ReportMill;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odbasis._ast.ASTODAttribute;
import de.monticore.odbasis._ast.ASTODElement;
import de.monticore.odbasis._ast.ASTODNamedObject;
import de.monticore.odlink._ast.ASTODLink;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.prettyprint.MCBasicTypesFullPrettyPrinter;
import de.monticore.umlmodifier._ast.ASTModifier;
import de.monticore.umlstereotype._ast.ASTStereotype;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class OCL2SMTStrategy {
  private boolean isPreStrategy = false;
  private boolean isPreCond = false;

  private Expr<? extends Sort> thisObj;

  public void setThis(Expr<? extends Sort> thisObj) {
    this.thisObj = thisObj;
  }

  public List<Expr<? extends Sort>> linkedObj = new ArrayList<>();

  public void enterPre() {
    isPreStrategy = true;
  }

  public void exitPre() {
    if (!isPreCond) {
      this.isPreStrategy = false;
    }
  }

  public void enterPreCond() {
    isPreCond = true;
    isPreStrategy = true;
  }

  public void exitPreCond() {
    isPreCond = false;
    isPreStrategy = false;
  }

  public boolean isPreStrategy() {
    return isPreStrategy;
  }

  public BoolExpr evaluateLink(
      ASTCDAssociation association,
      Expr<? extends Sort> obj1,
      Expr<? extends Sort> obj2,
      CD2SMTGenerator cd2SMTGenerator,
      ConstConverter cc,
      boolean ispre) {

    ASTCDDefinition cd = cd2SMTGenerator.getClassDiagram().getCDDefinition();
    if (ispre) {
      association = getPreAssociation(association, cd);
    }
    assert association != null;
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

  public ASTCDAssociation getAssociation(OCLType thisType, String otherRole, ASTCDDefinition cd) {
    ASTCDAssociation association = OCLHelper.getAssociation(thisType, otherRole, cd);
    if (isPreStrategy) {
      association = getPreAssociation(association, cd);
      exitPre();
    }
    return association;
  }

  public Expr<? extends Sort> getAttribute(
      Expr<? extends Sort> obj,
      OCLType type,
      String attributeName,
      CD2SMTGenerator cd2SMTGenerator,
      boolean isPre) {
    if (isPre) {
      attributeName = mkPre(attributeName);
    }

    Expr<? extends Sort> res =
        cd2SMTGenerator.getAttribute(
            CDHelper.getASTCDType(
                type.getName(), cd2SMTGenerator.getClassDiagram().getCDDefinition()),
            attributeName,
            obj);
    exitPre();

    return res;
  }

  public ASTModifier mkThisModifier() {
    return buildModifier("This", "true");
  }

  public ASTModifier mkLinkModifier() {
    return buildModifier("Link", "true");
  }

  public ASTModifier mkResultModifier(String value) {
    return buildModifier("Result", "true");
  }

  public ASTStereotype mkResultStereotype(String value) {
    return buildStereotype("Result", value);
  }

  public ASTModifier buildModifier(String stereotypeName, String stereotypeValue) {
    return OD4ReportMill.modifierBuilder()
        .setStereotype(buildStereotype(stereotypeName, stereotypeValue))
        .build();
  }

  public ASTStereotype buildStereotype(String stereotypeName, String stereotypeValue) {
    return OD4ReportMill.stereotypeBuilder()
        .addValues(
            OD4ReportMill.stereoValueBuilder()
                .setName(stereotypeName)
                .setContent(" ")
                .setText(OD4ReportMill.stringLiteralBuilder().setSource(stereotypeValue).build())
                .build())
        .build();
  }

  public OPDiffResult splitPreOD(ASTODArtifact od, Model model) {
    List<ASTODElement> preOdElements = new ArrayList<>();
    List<ASTODElement> postOdElements = new ArrayList<>();
    Set<Expr<? extends Sort>> elements =
        linkedObj.stream().map(e -> model.evaluate(e, true)).collect(Collectors.toSet());
    System.out.println(elements);
    ASTModifier modifier =
        OD4ReportMill.modifierBuilder()
            .setStereotype(
                OD4ReportMill.stereotypeBuilder()
                    .addValues(
                        OD4ReportMill.stereoValueBuilder()
                            .setName("Modified")
                            .setContent(" ")
                            .setText(OD4ReportMill.stringLiteralBuilder().setSource("true").build())
                            .build())
                    .build())
            .build();

    for (ASTODElement element : od.getObjectDiagram().getODElementList()) {
      if (element instanceof ASTODLink) {
        ASTODLink link = (ASTODLink) element;
        if (isPreLink(link)) {
          preOdElements.add(preLink2Link(link));
        } else {
          postOdElements.add(link);
        }
      }

      if (element instanceof ASTODNamedObject) {
        ASTODNamedObject obj = (ASTODNamedObject) element;
        obj.setModifier(modifier);

        Pair<ASTODNamedObject, ASTODNamedObject> objects = splitPreObject(obj);
        objects.getLeft().setModifier(modifier);
        objects.getRight().setModifier(modifier);
        preOdElements.add(objects.getLeft());
        postOdElements.add(objects.getRight());
      }
    }
    ASTODArtifact preOD =
        de.monticore.cd2smt.Helper.ODHelper.buildOD(
            "pre_" + od.getObjectDiagram().getName(), preOdElements);
    ASTODArtifact postOD =
        de.monticore.cd2smt.Helper.ODHelper.buildOD(
            "post_" + od.getObjectDiagram().getName(), postOdElements);

    return new OPDiffResult(preOD, postOD);
  }

  private static boolean isPreLink(ASTODLink link) {
    return isPre(link.getODLinkLeftSide().getRole()) && isPre(link.getODLinkRightSide().getRole());
  }

  private static Pair<ASTODNamedObject, ASTODNamedObject> splitPreObject(ASTODNamedObject object) {
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
        object.getMCObjectType().printType(new MCBasicTypesFullPrettyPrinter(new IndentPrinter()));
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

  public static ASTCDAssociation getPreAssociation(
      ASTCDAssociation association, ASTCDDefinition cd) {

    ASTCDAssocRightSide right = association.getRight();
    ASTCDAssocLeftSide left = association.getLeft();
    for (ASTCDAssociation preAssoc : cd.getCDAssociationsList()) {
      ASTCDAssocRightSide preRight = preAssoc.getRight();
      ASTCDAssocLeftSide preLeft = preAssoc.getLeft();
      if (right.getMCQualifiedType().equals(preRight.getMCQualifiedType())
          && right.getMCQualifiedType().equals(preRight.getMCQualifiedType())
          && OCL2SMTStrategy.mkPre(left.getCDRole().getName()).equals(preLeft.getCDRole().getName())
          && OCL2SMTStrategy.mkPre(right.getCDRole().getName())
              .equals(preRight.getCDRole().getName())) {
        return preAssoc;
      }
    }
    Log.info(
        "pre-association "
            + association.getLeftQualifiedName().getQName()
            + " -- "
            + association.getRightQualifiedName().getQName()
            + " not found ",
        "Pre Assoc Not Found");
    return null;
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

  public String mkObjName(String name, boolean isPre) {
    if (isPre) {
      return mkPre(name);
    }
    return name;
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

  public void addLink(Expr<? extends Sort> expr) {
    linkedObj.add(expr);
  }
}
