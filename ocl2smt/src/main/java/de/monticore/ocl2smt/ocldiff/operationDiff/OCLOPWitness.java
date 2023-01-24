package de.monticore.ocl2smt.ocldiff.operationDiff;

import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.odbasis._ast.ASTODArtifact;

public class OCLOPWitness {
  private final ASTOCLMethodSignature method;

  private final ASTODArtifact preOD;
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
