package de.monticore.ocl2smt;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.od4report.prettyprinter.OD4ReportFullPrettyPrinter;
import de.monticore.odbasis._ast.*;
import de.monticore.odlink._ast.ASTODLink;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
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

  protected List<String> getUnsatInvLines(List<String> objNames, ASTODArtifact od) {
    List<String> res = new ArrayList<>();
    for (ASTODElement element : od.getObjectDiagram().getODElementList()) {
      if (element instanceof ASTODNamedObject
          && objNames.contains(((ASTODNamedObject) element).getName())) {
        for (ASTODAttribute attribute :
            ((ASTODNamedObject) element)
                .getODAttributeList().stream()
                    .filter(a -> a.getName().equals("line"))
                    .collect(Collectors.toList())) {
          res.add(((ASTODName) attribute.getODValue()).getName());
        }
      }
    }
    return res;
  }

  protected List<String> getUnsatInvNameList(ASTODArtifact unsatOD) {
    List<ASTODLink> traceLinks =
        unsatOD.getObjectDiagram().getODElementList().stream()
            .filter(e -> e instanceof ASTODLink)
            .collect(Collectors.toList())
            .stream()
            .map(e -> (ASTODLink) e)
            .collect(Collectors.toList());
    List<String> unsatInvNameList = new ArrayList<>();
    traceLinks.forEach(
        l -> {
          unsatInvNameList.addAll(l.getLeftReferenceNames());
          unsatInvNameList.addAll(l.getRightReferenceNames());
        });
    return unsatInvNameList;
  }
}
