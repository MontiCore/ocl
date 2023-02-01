package de.monticore.ocl2smt.ocldiff;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl2smt.ocldiff.invariantDiff.OCLInvDiffResult;
import de.monticore.ocl2smt.ocldiff.invariantDiff.OCLInvariantDiff;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPDiffResult;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOperationDiff;
import de.monticore.odbasis._ast.ASTODArtifact;
import java.util.*;

public class OCLDiffGenerator {
  /**
   * converts CD + OCL Model in SMT and produces A witness Object Diagram
   *
   * @param cd the class diagram
   * @param ocl Set of OCl constraints
   * @param partial if partial == true, the Object diagram will be partial regarding the attribute
   * @return the witness Object Diagram
   */
  public static ASTODArtifact oclWitness(
      ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> ocl, boolean partial) {
    OCLInvariantDiff operator = new OCLInvariantDiff();
    return operator.oclWitness(cd, ocl, partial);
  }

  public static OCLInvDiffResult oclDiff(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOCL,
      boolean partial) {

    OCLInvariantDiff operator = new OCLInvariantDiff();
    return operator.oclDiff(cd, oldOcl, newOCL, partial);
  }

  public static OCLInvDiffResult oclDiff(
      ASTCDCompilationUnit oldCD,
      ASTCDCompilationUnit newCD,
      Set<ASTOCLCompilationUnit> oldOCL,
      Set<ASTOCLCompilationUnit> newOCL,
      boolean partial) {
    OCLInvariantDiff operator = new OCLInvariantDiff();
    return operator.CDOCLDiff(oldCD, newCD, oldOCL, newOCL, partial);
  }

  public static Set<OCLOPWitness> oclOPWitness(
      ASTCDCompilationUnit ast,
      Set<ASTOCLCompilationUnit> ocl,
      ASTOCLMethodSignature method,
      boolean partial) {
    OCLOperationDiff operator = new OCLOperationDiff();
    return operator.oclWitness(ast, ocl, method, partial);
  }

  public static Set<OCLOPWitness> oclOPWitness(
      ASTCDCompilationUnit ast, Set<ASTOCLCompilationUnit> ocl, boolean partial) {
    OCLOperationDiff operator = new OCLOperationDiff();
    return operator.oclWitness(ast, ocl, partial);
  }

  public static OCLOPDiffResult oclOPDiff(
      ASTCDCompilationUnit ast,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOcl,
      ASTOCLMethodSignature method,
      boolean partial) {

    OCLOperationDiff operator = new OCLOperationDiff();
    return operator.oclDiff(ast, oldOcl, newOcl, method, partial);
  }
}
