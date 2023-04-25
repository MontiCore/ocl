package de.monticore.ocl2smt.helpers;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLConstraint;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.ocl2smt.ocldiff.invariantDiff.OCLInvDiffResult;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPDiffResult;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.ocl2smt.util.OCL_Loader;
import de.monticore.od4report._prettyprint.OD4ReportFullPrettyPrinter;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.prettyprint.IndentPrinter;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class IOHelper {
  public static Set<ASTOCLCompilationUnit> parseOCl(File cdFile, Set<File> oclFiles) {
    Set<ASTOCLCompilationUnit> res = new HashSet<>();
    for (File oclFile : oclFiles) {

      try {
        res.add(OCL_Loader.loadAndCheckOCL(oclFile, cdFile));
      } catch (IOException e) {
        Log.error(
            "The files " + cdFile.getName() + " and " + oclFile.getName() + " cannot be read");
        throw new RuntimeException(e);
      }
    }
    return res;
  }

  public static ASTCDCompilationUnit parseCD(File cdFile) {

    try {
      return OCL_Loader.loadAndCheckCD(cdFile);
    } catch (IOException e) {
      Log.error("The files " + cdFile + " cannot be read");
      throw new RuntimeException(e);
    }
  }

  public static void printInvDiffResult(OCLInvDiffResult diff, Path output) {
    if (diff.getUnSatCore() != null) {
      printOD(diff.getUnSatCore(), output);
    }
    diff.getDiffWitness().forEach(x -> printOD(x, output));
  }

  public static void printOPDiff(OCLOPDiffResult diff, Path output) {
    if (diff.getUnSatCore() != null) {
      printOD(diff.getUnSatCore(), output);
    }
    diff.getOpDiffWitness()
        .forEach(
            x -> {
              String dir = x.getMethod().getMethodName().getQName();
              Path path = Path.of(output + "/" + dir);
              printOD(x.getPostOD(), path);
              printOD(x.getPreOD(), path);
            });
  }

  public static void printOPWitness(Set<OCLOPWitness> witness, Path output) {
    witness.forEach(
        x -> {
          String dir = x.getMethod().getMethodName().getQName();
          Path path = Path.of(output + "/" + dir);
          printOD(x.getPostOD(), path);
          printOD(x.getPreOD(), path);
        });
  }

  public static void printOD(ASTODArtifact od, Path output) {
    String fileName = od.getObjectDiagram().getName();
    Path outputFile = Paths.get(output.toString(), fileName + ".od");

    try {
      FileUtils.writeStringToFile(
          outputFile.toFile(),
          new OD4ReportFullPrettyPrinter(new IndentPrinter()).prettyprint(od),
          Charset.defaultCharset());
    } catch (Exception e) {
      e.printStackTrace();
      Log.error(
          "It Was Not Possible to Print the Object Diagram " + od.getObjectDiagram().getName());
    }
  }

  public static ASTOCLMethodSignature getMethodSignature(
      Set<ASTOCLCompilationUnit> oclSet, String name) {
    ASTOCLMethodSignature res = null;
    for (ASTOCLCompilationUnit ocl : oclSet) {
      for (ASTOCLConstraint constraint : ocl.getOCLArtifact().getOCLConstraintList()) {
        if (constraint instanceof ASTOCLOperationConstraint) {
          ASTOCLOperationConstraint opConstraint = (ASTOCLOperationConstraint) constraint;
          if (opConstraint.getOCLOperationSignature() instanceof ASTOCLMethodSignature) {
            ASTOCLMethodSignature method =
                (ASTOCLMethodSignature) opConstraint.getOCLOperationSignature();
            if (method.getMethodName().getQName().equals(name)) {
              res = method;
            }
          }
        }
      }
    }
    if (res == null) {
      Log.error("No Operations constraints Specified for the Method " + name);
    }
    return res;
  }
}
