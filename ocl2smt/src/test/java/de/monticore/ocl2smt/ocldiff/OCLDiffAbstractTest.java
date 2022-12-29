/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocldiff;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.util.OCL_Loader;
import de.monticore.od4report.prettyprinter.OD4ReportFullPrettyPrinter;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odlink._ast.ASTODLink;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
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

  public void setUp() {
    Log.init();
    OCLMill.init();
    CD4CodeMill.init();
  }

  public void printDiff(Pair<ASTODArtifact, Set<ASTODArtifact>> diff) {
    if (diff.getLeft() != null) {
      printOD(diff.getLeft());
    }
    diff.getRight().forEach(this::printOD);
  }

  protected ASTOCLCompilationUnit parseOCl(String cdFileName, String oclFileName)
      throws IOException {
    setUp();
    return OCL_Loader.loadAndCheckOCL(
        Paths.get(RELATIVE_MODEL_PATH, oclFileName).toFile(),
        Paths.get(RELATIVE_MODEL_PATH, cdFileName).toFile());
  }

  protected ASTCDCompilationUnit parseCD(String cdFileName) throws IOException {
    setUp();
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
}
