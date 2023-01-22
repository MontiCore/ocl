package de.monticore.ocl2smt.ocldiff.operationDiff;

import de.monticore.odbasis._ast.ASTODArtifact;

public class OPWitness {
  private final ASTODArtifact preOD;
  private final ASTODArtifact postOD;

  public OPWitness(ASTODArtifact preOD, ASTODArtifact postOD) {
    this.preOD = preOD;
    this.postOD = postOD;
  }

  public ASTODArtifact getPreOD() {
    return preOD;
  }

  public ASTODArtifact getPostOD() {
    return postOD;
  }
}
