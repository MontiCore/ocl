package de.monticore.ocl2smt.ocldiff.operationDiff;

import de.monticore.odbasis._ast.ASTODArtifact;

import java.util.HashSet;
import java.util.Set;

public class OCLOPDiffResult {
  private final ASTODArtifact unSatCore;
  private final Set<OCLOPWitness> diffWitness;

  public OCLOPDiffResult(
      ASTODArtifact unSatCore, Set<OCLOPWitness> diffWitness) {
    this.unSatCore = unSatCore;
    this.diffWitness = diffWitness  ;
    diffWitness = new HashSet<>() ;
  }

  public  void addDiffWitness( OCLOPWitness witness){
    this.diffWitness.add(witness);
  }

  public ASTODArtifact getUnSatCore() {
    return unSatCore;
  }

  public  Set<OCLOPWitness> getDiffWitness() {
    return diffWitness;
  }
}
