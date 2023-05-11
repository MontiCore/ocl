package de.monticore.ocl2smt.ocldiff.operationDiff;

import de.monticore.odbasis._ast.ASTODArtifact;

import java.util.Set;

/** this class saves the result of a semdiff on OCL Operations-constraints. */
public class OCLOPDiffResult {
  /** contains specifications tracing between the two models */
  private final ASTODArtifact unSatCore;

  /** contains a set of operation witness from the diff witness */
  private final Set<OCLOPWitness> opDiffWitness;


  public OCLOPDiffResult(
          ASTODArtifact unSatCore, Set<OCLOPWitness> opDiffWitness) {
    this.unSatCore = unSatCore;
    this.opDiffWitness = opDiffWitness;
  }

  public void addDiffWitness(OCLOPWitness witness) {
    this.opDiffWitness.add(witness);
  }

  public ASTODArtifact getUnSatCore() {
    return unSatCore;
  }

  public Set<OCLOPWitness> getOpDiffWitness() {
    return opDiffWitness;
  }

}
