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
   * Converts CD + OCL Model in SMT and produces a witness Object Diagram as proof for the
   * consistency.
   *
   * @param cd the class diagram.
   * @param ocl the Set of OCl constraints.
   * @param partial if partial == true, the Object diagram will be partial regarding the attribute.
   * @return the witness Object Diagram.
   */
  public static ASTODArtifact oclWitness(
      ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> ocl, boolean partial) {
    OCLInvariantDiff operator = new OCLInvariantDiff();
    return operator.oclWitness(cd, ocl, partial);
  }

  /**
   * compute the semantic difference ( of OCL-invariant) between a new ocl-Model and an old one. The
   * result is a set of witness object diagram and specifications tracing.
   *
   * @param cd the class diagram.
   * @param oldOcl the old OCL-Model.
   * @param newOCL the new OCl-Model.
   * @param partial if partial == true, the Object diagram will be partial regarding the attributes.
   */
  public static OCLInvDiffResult oclDiff(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOCL,
      boolean partial) {

    OCLInvariantDiff operator = new OCLInvariantDiff();
    return operator.oclDiff(cd, oldOcl, newOCL, partial);
  }

  /**
   * Computes the semantic difference ( of OCL-invariant) between a new CD/OCL-Model and an old one.
   * The result is a set of witness Object Diagrams and specifications tracing.
   *
   * @param oldCD the old class diagram.
   * @param newCD the new class diagram.
   * @param newOCL the new OCl-Model.
   * @param oldOCL the old OCL-Model.
   * @param partial if partial == true, the Object Diagram will be partial regarding the attributes.
   */
  public static OCLInvDiffResult oclDiff(
      ASTCDCompilationUnit oldCD,
      ASTCDCompilationUnit newCD,
      Set<ASTOCLCompilationUnit> oldOCL,
      Set<ASTOCLCompilationUnit> newOCL,
      boolean partial) {
    OCLInvariantDiff operator = new OCLInvariantDiff();
    return operator.CDOCLDiff(oldCD, newCD, oldOCL, newOCL, partial);
  }

  /**
   * Converts CD + OCL-Model with Operation constraints in SMT and produces a set of witness Object
   * Diagrams as witness for the operation.
   *
   * @param cd the class diagram.
   * @param ocl Set of OCl constraints.
   * @param method method whose constraints must be analysed.
   * @param partial if partial == true, the Object Diagrams will be partial regarding the
   *     attributes.
   * @return the witness Object Diagram.
   */
  public static Set<OCLOPWitness> oclOPWitness(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> ocl,
      ASTOCLMethodSignature method,
      boolean partial) {
    OCLOperationDiff operator = new OCLOperationDiff();
    return operator.oclWitness(cd, ocl, method, partial);
  }

  /**
   * Converts CD + OCL-Model with Operation constraints in SMT and produces a set of witness Object
   * Diagrams as witness for the operations.
   *
   * @param cd the class diagram.
   * @param ocl Set of OCl constraints.
   * @param partial if partial == true, the Object diagram will be partial regarding the attributes.
   * @return the witness Object Diagram.
   */
  public static Set<OCLOPWitness> oclOPWitness(
      ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> ocl, boolean partial) {
    OCLOperationDiff operator = new OCLOperationDiff();
    return operator.oclWitness(cd, ocl, partial);
  }

  /**
   * Computes the semantic difference(of an operation constraint) between a new OCL-Model and an old
   * one. The result is a set of witness object diagrams and specifications tracing.
   *
   * @param cd the new class diagram.
   * @param newOcl the new OCl-Model.
   * @param oldOcl the old OCL-Model.
   * @param method the method for which the diff of the constraints has to be calculated.
   * @param partial if partial == true, the Object Diagrams will be partial regarding the
   *     attributes.
   */
  public static OCLOPDiffResult oclOPDiff(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOcl,
      ASTOCLMethodSignature method,
      boolean partial) {

    OCLOperationDiff operator = new OCLOperationDiff();
    return operator.oclDiff(cd, oldOcl, newOcl, method, partial);
  }
}
