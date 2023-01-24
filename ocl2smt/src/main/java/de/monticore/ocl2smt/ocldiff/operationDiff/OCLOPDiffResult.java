package de.monticore.ocl2smt.ocldiff.operationDiff;

import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.odbasis._ast.ASTODArtifact;
import java.util.Map;
import java.util.Set;

public class OCLOPDiffResult {
  private final ASTODArtifact unSatCore;
  private final Map<ASTOCLMethodSignature, Set<OPWitness>> diffWitness;

  public OCLOPDiffResult(
      ASTODArtifact unSatCore, Map<ASTOCLMethodSignature, Set<OPWitness>> diffWitness) {
    this.unSatCore = unSatCore;
    this.diffWitness = diffWitness;
  }

  public ASTODArtifact getUnSatCore() {
    return unSatCore;
  }

  public Map<ASTOCLMethodSignature, Set<OPWitness>> getDiffWitness() {
    return diffWitness;
  }
}
