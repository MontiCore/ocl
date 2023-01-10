package de.monticore.ocl2smt.util;

import de.monticore.odbasis._ast.ASTODArtifact;

public class OPDiffResult {
  private final ASTODArtifact preOD;
  private final ASTODArtifact postOD;

  public OPDiffResult(ASTODArtifact preOD, ASTODArtifact postOD) {
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
