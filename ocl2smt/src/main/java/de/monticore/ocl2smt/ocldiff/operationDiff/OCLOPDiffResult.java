package de.monticore.ocl2smt.ocldiff.operationDiff;

import de.monticore.ocl.ocl._ast.ASTOCLArtifact;
import java.util.Set;

public class OCLOPDiffResult {
  private final ASTOCLArtifact unSatCore;
  private final Set<OPWitness> diffWitness;

  public OCLOPDiffResult(ASTOCLArtifact unSatCore, Set<OPWitness> diffWitness) {
    this.unSatCore = unSatCore;
    this.diffWitness = diffWitness;
  }

  public ASTOCLArtifact getUnSatCore() {
    return unSatCore;
  }

  public Set<OPWitness> getDiffWitness() {
    return diffWitness;
  }
}
