package de.monticore.ocl2smt.gradle;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.OCLDiffGenerator;
import de.monticore.ocl2smt.OCL_Loader;
import de.monticore.od4report.prettyprinter.OD4ReportFullPrettyPrinter;
import de.monticore.odbasis._ast.ASTODArtifact;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

@CacheableTask
public abstract class OCLSemDiffTask extends DefaultTask {

  @InputFile
  @PathSensitive(PathSensitivity.NONE)
  public abstract RegularFileProperty getCD();

  @InputFiles
  @PathSensitive(PathSensitivity.RELATIVE)
  public abstract FileCollection getPositiveOCL();

  @InputFiles
  @PathSensitive(PathSensitivity.RELATIVE)
  public abstract FileCollection getNegativeOCL();

  @OutputDirectory
  public abstract DirectoryProperty getOutputDir();

  protected Set<ASTOCLCompilationUnit> loadOCL(ASTCDCompilationUnit cd, Set<File> oclFiles) throws IOException {
    Set<ASTOCLCompilationUnit> result = new HashSet<>();
    for (File f : oclFiles) {
      result.add(OCL_Loader.loadAndCheckOCL(f, cd));
    }
    return result;
  }

  @TaskAction
  public void run() throws IOException {
    // Load Input
    ASTCDCompilationUnit cd = OCL_Loader.loadAndCheckCD(getCD().get().getAsFile());

    Set<ASTOCLCompilationUnit> positiveOCL = loadOCL(cd, getPositiveOCL().getFiles());
    Set<ASTOCLCompilationUnit> negativeOCL = loadOCL(cd, getNegativeOCL().getFiles());

    // Compute Diff
    Set<ASTODArtifact> witnesses = OCLDiffGenerator.oclDiff(cd, positiveOCL, negativeOCL);

    // Write Results
    for (ASTODArtifact wit : witnesses) {
      String fileName = wit.getObjectDiagram().getName() + ".od";
      FileUtils.writeStringToFile(
          getOutputDir().file(fileName).get().getAsFile(),
          new OD4ReportFullPrettyPrinter().prettyprint(wit), Charset.defaultCharset());
    }
  }
}
