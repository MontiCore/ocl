package de.monticore.ocl2smt.ocldiff.operationDiff;

import de.monticore.odbasis._ast.ASTODArtifact;

import java.util.Set;

/** this class save the result of a semdiff on OCL Operations-constraints. */
public class OCLOPDiffResult {
  /** contains specifications tracing between the two models */
  private final ASTODArtifact unSatCore;

  /** contains set of operations witness from the diff witness */
  private final Set<OCLOPWitness> opDiffWitness;

  private final Set<OCLOPWitness> invDiffWitness;

  public OCLOPDiffResult(
          ASTODArtifact unSatCore, Set<OCLOPWitness> opDiffWitness, Set<OCLOPWitness> invDiffWitness) {
    this.unSatCore = unSatCore;
    this.opDiffWitness = opDiffWitness;
    this.invDiffWitness = invDiffWitness;
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

  public Set<OCLOPWitness> getInvDiffWitness() {
    return invDiffWitness;
  }
}
