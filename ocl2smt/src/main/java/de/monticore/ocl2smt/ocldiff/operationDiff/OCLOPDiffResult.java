package de.monticore.ocl2smt.ocldiff.operationDiff;

import de.monticore.odbasis._ast.ASTODArtifact;
import java.util.HashSet;
import java.util.Set;

/** this class save the result of a semdiff on OCL Operations-constraints. */
public class OCLOPDiffResult {
  /** contains specifications tracing between the two models */
  private final ASTODArtifact unSatCore;

  /** contains set of operations witness from the diff witness */
  private final Set<OCLOPWitness> diffWitness;

  public OCLOPDiffResult(ASTODArtifact unSatCore, Set<OCLOPWitness> diffWitness) {
    this.unSatCore = unSatCore;
    this.diffWitness = diffWitness;
    diffWitness = new HashSet<>();
  }

  public void addDiffWitness(OCLOPWitness witness) {
    this.diffWitness.add(witness);
  }

  public ASTODArtifact getUnSatCore() {
    return unSatCore;
  }

  public Set<OCLOPWitness> getDiffWitness() {
    return diffWitness;
  }
}
