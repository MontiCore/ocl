/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocldiff;

import com.microsoft.z3.Context;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.util.OCL_Loader;
import de.monticore.ocl2smt.util.OPDiffResult;
import de.monticore.od4report.prettyprinter.OD4ReportFullPrettyPrinter;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odlink._ast.ASTODLink;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;

public abstract class OCLDiffAbstractTest {
  protected static final String RELATIVE_MODEL_PATH =
      "src/test/resources/de/monticore/ocl2smt/OCLDiff";
  protected static final String RELATIVE_TARGET_PATH =
      "target/generated/sources/annotationProcessor/java/ocl2smttest";

  public void printDiff(Pair<ASTODArtifact, Set<ASTODArtifact>> diff) {
    if (diff.getLeft() != null) {
      printOD(diff.getLeft());
    }
    diff.getRight().forEach(this::printOD);
  }

  public void printOPDiff(Pair<ASTODArtifact, Set<OPDiffResult>> diff) {
    if (diff.getLeft() != null) {
      printOD(diff.getLeft());
    }
    diff.getRight()
        .forEach(
            x -> {
              printOD(x.getPostOD());
              printOD(x.getPreOD());
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

  public void printOD(ASTODArtifact od) {
    Path outputFile = Paths.get(RELATIVE_TARGET_PATH, od.getObjectDiagram().getName() + ".od");
    try {
      FileUtils.writeStringToFile(
          outputFile.toFile(),
          new OD4ReportFullPrettyPrinter().prettyprint(od),
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

  public Pair<ASTODArtifact, Set<ASTODArtifact>> computeDiff2CD(
      String posCDn, String negCDn, String posOCLn, String negOCLn) throws IOException {
    ASTCDCompilationUnit posCD = parseCD(posCDn);
    ASTCDCompilationUnit negCD = parseCD(negCDn);
    Set<ASTOCLCompilationUnit> posOCL = new HashSet<>();
    Set<ASTOCLCompilationUnit> negOCL = new HashSet<>();
    posOCL.add(parseOCl(posCDn, posOCLn));
    negOCL.add(parseOCl(negCDn, negOCLn));
    return OCLDiffGenerator.CDOCLDiff(posCD, negCD, posOCL, negOCL, false);
  }

  public Pair<ASTODArtifact, Set<ASTODArtifact>> computeDiffOneCD(
      String posCDn, String posOCLn, String negOCLn) throws IOException {
    ASTCDCompilationUnit posCD = parseCD(posCDn);
    Set<ASTOCLCompilationUnit> posOCL = new HashSet<>();
    Set<ASTOCLCompilationUnit> negOCL = new HashSet<>();
    posOCL.add(parseOCl(posCDn, posOCLn));
    negOCL.add(parseOCl(posCDn, negOCLn));
    return OCLDiffGenerator.oclDiff(posCD, posOCL, negOCL, false);
  }

  public static Context buildContext() {
    Map<String, String> cfg = new HashMap<>();
    cfg.put("model", "true");
    return new Context(cfg);
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
}
