package de.monticore.ocl2smt.ocldiff.operationDiff;

import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.odbasis._ast.ASTODArtifact;

/** this class save object diagram witness of an operation constraint */
public class OCLOPWitness {
  /** method whose constraints which are analysed. */
  private final ASTOCLMethodSignature method;
  /** object diagram before the method execution */
  private final ASTODArtifact preOD;
  /** object diagram after the method execution */
  private final ASTODArtifact postOD;

  public OCLOPWitness(ASTOCLMethodSignature method, ASTODArtifact preOD, ASTODArtifact postOD) {
    this.preOD = preOD;
    this.postOD = postOD;
    this.method = method;
  }

  public ASTODArtifact getPreOD() {
    return preOD;
  }

  public ASTODArtifact getPostOD() {
    return postOD;
  }

  public ASTOCLMethodSignature getMethod() {
    return method;
  }
}
