/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocldiff;

import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.OCL2SMTAbstractTest;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocldiff.invariantDiff.OCLInvDiffResult;
import de.monticore.ocl2smt.util.OCL_Loader;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odbasis._ast.ASTODAttribute;
import de.monticore.odbasis._ast.ASTODName;
import de.monticore.odbasis._ast.ASTODNamedObject;
import de.monticore.odlink._ast.ASTODLink;
import de.monticore.umlstereotype._ast.ASTStereotype;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class OCLDiffAbstractTest extends OCL2SMTAbstractTest {
  protected static final String RELATIVE_MODEL_PATH =
      "src/test/resources/de/monticore/ocl2smt/OCLDiff";
  protected final String TARGET_DIR = "target/generated-test/oclDiff/";

  protected ASTOCLCompilationUnit parseOCl(String cdFileName, String oclFileName)
      throws IOException {

    return OCL_Loader.loadAndCheckOCL(
        Paths.get(RELATIVE_MODEL_PATH, oclFileName).toFile(),
        Paths.get(RELATIVE_MODEL_PATH, cdFileName).toFile());
  }

  protected ASTCDCompilationUnit parseCD(String cdFileName) throws IOException {

    return OCL_Loader.loadAndCheckCD(Paths.get(RELATIVE_MODEL_PATH, cdFileName).toFile());
  }

  protected ASTODArtifact parseOD(String cdFileName) throws IOException {

    return OCL_Loader.loadAndCheckOD(Paths.get(RELATIVE_MODEL_PATH, cdFileName).toFile());
  }

  protected boolean checkLink(String left, String right, ASTODArtifact od) {
    Set<ASTODLink> links =
        od.getObjectDiagram().getODElementList().stream()
            .filter(x -> x instanceof ASTODLink)
            .map(x -> (ASTODLink) x)
            .collect(Collectors.toSet());

    return links.stream()
        .anyMatch(
            x ->
                x.getLeftReferenceNames().get(0).contains(left)
                    && x.getRightReferenceNames().get(0).contains(right));
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
    return OCLDiffGenerator.oclDiff(
        oldCD, newCD, oldOCL, newOCL, new HashSet<>(), new HashSet<>(), false);
  }

  public OCLInvDiffResult computeDiffOneCDFinite(
      String cdName, String oldOCLName, String newOCLName, long max) throws IOException {
    return OCLDiffGenerator.oclDiffComp(
        parseCD(cdName),
        new HashSet<>(Set.of(parseOCl(cdName, oldOCLName))),
        new HashSet<>(Set.of(parseOCl(cdName, newOCLName))),
        new HashSet<>(),
        new HashSet<>(),
        max,
        false);
  }

  public OCLInvDiffResult computeDiffOneCD(String cdName, String oldOCLName, String newOCLName)
      throws IOException {
    return OCLDiffGenerator.oclDiff(
        parseCD(cdName),
        new HashSet<>(Set.of(parseOCl(cdName, oldOCLName))),
        new HashSet<>(Set.of(parseOCl(cdName, newOCLName))),
        new HashSet<>(),
        new HashSet<>(),
        false);
  }

  public OCLInvDiffResult computeDiffOneCD(
      String cdName, String oldOCLName, String newOCLName, String posODName, String negODName)
      throws IOException {

    return OCLDiffGenerator.oclDiff(
        parseCD(cdName),
        new HashSet<>(Set.of(parseOCl(cdName, oldOCLName))),
        new HashSet<>(Set.of(parseOCl(cdName, newOCLName))),
        new HashSet<>(Set.of(parseOD(posODName))),
        new HashSet<>(Set.of(parseOD(negODName))),
        false);
  }

  public ASTODArtifact computeWitness(String cdName, String oldOCLName) throws IOException {
    ASTCDCompilationUnit cd = parseCD(cdName);
    Set<ASTOCLCompilationUnit> oldOCL = new HashSet<>();
    oldOCL.add(parseOCl(cdName, oldOCLName));
    return OCLDiffGenerator.oclWitness(cd, oldOCL, new HashSet<>(), new HashSet<>(), false);
  }

  public boolean containsAttribute(ASTCDClass c, String attribute) {
    for (ASTCDAttribute attr : c.getCDAttributeList()) {
      if (attr.getName().equals(attribute)) {
        return true;
      }
    }
    return false;
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
}
