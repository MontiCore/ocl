package de.monticore.ocl2smt.gradle;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;

import de.monticore.ocl2smt.OCLDiffGenerator;
import de.monticore.ocl2smt.OCL_Loader;

import de.monticore.od4report.prettyprinter.OD4ReportFullPrettyPrinter;
import de.monticore.odbasis._ast.ASTODArtifact;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;
// import org.gradle.work.NormalizeLineEndings;

import java.io.File;
import java.io.IOException;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

@CacheableTask
public abstract class OCLSemDiffTask extends DefaultTask {

  @InputFile
  @PathSensitive(PathSensitivity.NONE)
  // @NormalizeLineEndings
  public abstract RegularFileProperty getCd();

  @InputFiles
  @Optional
  @PathSensitive(PathSensitivity.RELATIVE)
  // @NormalizeLineEndings
  public abstract ConfigurableFileCollection getPositiveOCL();

  @InputFiles
  @Optional
  @PathSensitive(PathSensitivity.RELATIVE)
  // @NormalizeLineEndings
  public abstract ConfigurableFileCollection getNegativeOCL();

  @OutputDirectory
  public abstract DirectoryProperty getOutputDir();

  protected Set<ASTOCLCompilationUnit> loadOCL(File cdFile, Set<File> oclFiles) throws IOException {
    Set<ASTOCLCompilationUnit> result = new HashSet<>();
    for (File f : oclFiles) {
      result.add(OCL_Loader.loadAndCheckOCL(f, cdFile));
    }
    return result;
  }

  @TaskAction
  public void run() throws IOException {
    OCLMill.init();
    CD4CodeMill.init();

    FileUtils.deleteDirectory(getOutputDir().get().getAsFile());  // TODO: Is this required?


    // Load Input
    ASTCDCompilationUnit cd = OCL_Loader.loadAndCheckCD(getCd().get().getAsFile());

    Set<ASTOCLCompilationUnit> positiveOCL = loadOCL(getCd().get().getAsFile(), getPositiveOCL().getFiles());
    Set<ASTOCLCompilationUnit> negativeOCL = loadOCL(getCd().get().getAsFile(), getNegativeOCL().getFiles());


    Set<ASTODArtifact> witnesses;
    // Compute Diff
    if (negativeOCL.isEmpty()) {
      witnesses = new HashSet<>();
      witnesses.add(OCLDiffGenerator.oclWitness(cd, positiveOCL));
    } else {
      witnesses = OCLDiffGenerator.oclDiff(cd, positiveOCL, negativeOCL);
    }


    // Write Results
    for (ASTODArtifact wit : witnesses) {
      String fileName = wit.getObjectDiagram().getName() + ".od";
      FileUtils.writeStringToFile(getOutputDir().file(fileName).get().getAsFile(), new OD4ReportFullPrettyPrinter().prettyprint(wit), Charset.defaultCharset());
    }
  }
}
