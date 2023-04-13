/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocldiff;

import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocldiff.invariantDiff.OCLInvDiffResult;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPDiffResult;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.ocl2smt.util.OCL_Loader;
import de.monticore.od4report._prettyprint.OD4ReportFullPrettyPrinter;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odbasis._ast.ASTODAttribute;
import de.monticore.odbasis._ast.ASTODName;
import de.monticore.odbasis._ast.ASTODNamedObject;
import de.monticore.odlink._ast.ASTODLink;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.umlstereotype._ast.ASTStereotype;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;

public abstract class OCLDiffAbstractTest {
  protected static final String RELATIVE_MODEL_PATH =
      "src/test/resources/de/monticore/ocl2smt/OCLDiff";
  protected static final String RELATIVE_TARGET_PATH =
      "target/generated/sources/annotationProcessor/java/ocl2smttest";

  public void printResult(OCLInvDiffResult diff, String directory) {
    if (diff.getUnSatCore() != null) {
      printOD(diff.getUnSatCore(), directory);
    }
    diff.getDiffWitness().forEach(x -> printOD(x, directory));
  }

  public void printOPDiff(OCLOPDiffResult diff, String directory) {
    if (diff.getUnSatCore() != null) {
      printOD(diff.getUnSatCore(), directory);
    }
    diff.getDiffWitness()
        .forEach(
            x -> {
              printOD(x.getPostOD(), directory);
              printOD(x.getPreOD(), directory);
            });
  }

  public void printOPWitness(Set<OCLOPWitness> witness, String directory) {
    witness.forEach(
        x -> {
          printOD(x.getPostOD(), directory);
          printOD(x.getPreOD(), directory);
        });
  }

  protected ASTOCLCompilationUnit parseOCl(String cdFileName, String oclFileName)
      throws IOException {

    return OCL_Loader.loadAndCheckOCL(
        Paths.get(RELATIVE_MODEL_PATH, oclFileName).toFile(),
        Paths.get(RELATIVE_MODEL_PATH, cdFileName).toFile());
  }

  protected ASTCDCompilationUnit parseCD(String cdFileName) throws IOException {

    return OCL_Loader.loadAndCheckCD(Paths.get(RELATIVE_MODEL_PATH, cdFileName).toFile());
  }

  public void printOD(ASTODArtifact od, String directory) {
    Path outputFile =
        Paths.get(RELATIVE_TARGET_PATH + "/" + directory, od.getObjectDiagram().getName() + ".od");
    try {
      FileUtils.writeStringToFile(
          outputFile.toFile(),
          new OD4ReportFullPrettyPrinter(new IndentPrinter()).prettyprint(od),
          Charset.defaultCharset());
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail("It Was Not Possible to Print the Object Diagram");
    }
  }

  protected boolean checkLink(String left, String right, ASTODArtifact od) {
    Set<ASTODLink> links =
        od.getObjectDiagram().getODElementList().stream()
            .filter(x -> x instanceof ASTODLink)
            .map(x -> (ASTODLink) x)
            .collect(Collectors.toSet());

    return links.stream()
            .filter(
                x ->
                    x.getLeftReferenceNames().get(0).contains(left)
                        && x.getRightReferenceNames().get(0).contains(right))
            .collect(Collectors.toSet())
            .size()
        == 1;
  }

  protected int countLinks(ASTODArtifact od) {
    return (int)
        od.getObjectDiagram().getODElementList().stream()
            .filter(x -> (x instanceof ASTODLink))
            .count();
  }

  public OCLInvDiffResult computeDiff2CD(
      String oldCDn, String newCDn, String oldOCLn, String newOCLn) throws IOException {
    ASTCDCompilationUnit oldCD = parseCD(oldCDn);
    ASTCDCompilationUnit newCD = parseCD(newCDn);
    Set<ASTOCLCompilationUnit> oldOCL = new HashSet<>();
    Set<ASTOCLCompilationUnit> newOCL = new HashSet<>();
    oldOCL.add(parseOCl(oldCDn, oldOCLn));
    newOCL.add(parseOCl(newCDn, newOCLn));
    return OCLDiffGenerator.oclDiff(oldCD, newCD, oldOCL, newOCL, false);
  }

  public OCLInvDiffResult computeDiffOneCD(String cdName, String oldOCLName, String newOCLName)
      throws IOException {
    ASTCDCompilationUnit cd = parseCD(cdName);
    Set<ASTOCLCompilationUnit> newOCL = new HashSet<>();
    Set<ASTOCLCompilationUnit> oldOCL = new HashSet<>();
    newOCL.add(parseOCl(cdName, newOCLName));
    oldOCL.add(parseOCl(cdName, oldOCLName));
    return OCLDiffGenerator.oclDiff(cd, oldOCL, newOCL, false);
  }

  public boolean containsAttribute(ASTCDClass c, String attribute) {
    for (ASTCDAttribute attr : c.getCDAttributeList()) {
      if (attr.getName().equals(attribute)) {
        return true;
      }
    }
    return false;
  }

  public ASTCDClass getClass(ASTCDCompilationUnit cd, String className) {
    for (ASTCDClass astcdClass : cd.getCDDefinition().getCDClassesList()) {
      if (astcdClass.getName().equals(className)) {
        return astcdClass;
      }
    }
    return null;
  }

  public boolean containsAssoc(
      ASTCDCompilationUnit cd, String left, String leftRole, String right, String rightRole) {
    for (ASTCDAssociation assoc : cd.getCDDefinition().getCDAssociationsList()) {
      if (left.equals(assoc.getLeftQualifiedName().getQName())
          && right.equals(assoc.getRightQualifiedName().getQName())
          && leftRole.equals(assoc.getLeft().getCDRole().getName())
          && rightRole.equals(assoc.getRight().getCDRole().getName())) {
        return true;
      }
    }
    return false;
  }

  ASTODNamedObject getThisObj(ASTODArtifact od) {
    for (ASTODNamedObject obj : OCLHelper.getObjectList(od)) {
      if (obj.getModifier().isPresentStereotype()) {
        ASTStereotype stereotype = obj.getModifier().getStereotype();
        if (stereotype.contains("this") && stereotype.getValue("this").equals("true")) {
          System.out.println(stereotype.getValue("this"));
          return obj;
        }
      }
    }
    return null;
  }

  String getAttribute(ASTODNamedObject obj, String attributeName) {
    assert obj != null;
    for (ASTODAttribute attribute : obj.getODAttributeList()) {
      if (attribute.getName().equals(attributeName)) {
        ASTODName value = (ASTODName) attribute.getODValue();
        return value.getName();
      }
    }
    return null;
  }

  List<ASTODNamedObject> getLinkedObjects(ASTODNamedObject obj, ASTODArtifact od) {
    List<String> res = new ArrayList<>();
    for (ASTODLink link : OCLHelper.getLinkList(od)) {
      if (link.getRightReferenceNames().contains(obj.getName())) {
        res.addAll(link.getLeftReferenceNames());
      }
      if (link.getLeftReferenceNames().contains(obj.getName())) {
        res.addAll(link.getRightReferenceNames());
      }
    }
    return res.stream().map(name -> getObject(od, name)).collect(Collectors.toList());
  }

  public ASTODNamedObject getObject(ASTODArtifact od, String objectName) {
    for (ASTODNamedObject obj : OCLHelper.getObjectList(od)) {
      if (obj.getName().equals(objectName)) {
        return obj;
      }
    }
    return null;
  }

  public ASTOCLMethodSignature getMethodSignature(Set<ASTOCLCompilationUnit> oclSet, String name) {
    ASTOCLMethodSignature res = null;
    for (ASTOCLCompilationUnit ocl : oclSet) {
      for (ASTOCLConstraint constraint : ocl.getOCLArtifact().getOCLConstraintList()) {
        if (constraint instanceof ASTOCLOperationConstraint) {
          ASTOCLOperationConstraint opConstraint = (ASTOCLOperationConstraint) constraint;
          if (opConstraint.getOCLOperationSignature() instanceof ASTOCLMethodSignature) {
            ASTOCLMethodSignature method =
                (ASTOCLMethodSignature) opConstraint.getOCLOperationSignature();
            if (method.getMethodName().getQName().contains(name)) {
              res = method;
            }
          }
        }
      }
    }
    return res;
  }
}
