package de.monticore.ocl.ocl;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

public class OCLDiffGenerator {

  public static void oclWitness(File cd, Set<File> ocl, Path output, boolean partial) {}

  public static void oclDiff(
      File cd, Set<File> oldOcl, Set<File> newOCL, Path output, boolean partial) {}

  public static void oclDiff(
      File oldCd, File newCd, Set<File> oldModel, Set<File> newOcl, Path output, boolean partial) {}

  public static void oclOPWitness(
      File cd, Set<File> ocl, String method, Path output, boolean partial) {}

  public static void oclOPWitness(File cd, Set<File> ocl, Path output, boolean partial) {}

  public static void oclOPDiff(
      File cd,
      Set<File> oldOclDirectory,
      Set<File> newOclDirectory,
      String method,
      Path output,
      boolean partial) {}
  ;

  public static void oclOPDiff(
      File cd,
      Set<File> oldOclDirectory,
      Set<File> newOclDirectory,
      Path output,
      boolean partial) {}
  ;
}
