package de.monticore.ocl2smt.ocldiff.invarianteDiff;

import de.monticore.odbasis._ast.ASTODArtifact;
import java.util.Set;

public class OCLInvDiffResult {
  private ASTODArtifact unSatCore;
  private Set<ASTODArtifact> diffWitness;

  public OCLInvDiffResult(ASTODArtifact unSatCore, Set<ASTODArtifact> diffWitness) {
    this.diffWitness = diffWitness;
    this.unSatCore = unSatCore;
  }

  public ASTODArtifact getUnSatCore() {
    return unSatCore;
  }

  public Set<ASTODArtifact> getDiffWitness() {
    return diffWitness;
  }
}
