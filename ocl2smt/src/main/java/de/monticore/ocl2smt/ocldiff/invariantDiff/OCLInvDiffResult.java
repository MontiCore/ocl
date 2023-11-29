package de.monticore.ocl2smt.ocldiff.invariantDiff;

import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.odbasis._ast.ASTODArtifact;
import java.util.Set;

/** this class save the result of a semdiff on OCL-invariants */
public class OCLInvDiffResult {
  /** contains specifications tracing between the two models */
  private final ASTODArtifact unSatCore;

  /** contains a set of Object diagram from the diff witness */
  private final Set<ASTODArtifact> diffWitness;

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

  public boolean isPresentTrace() {
    return unSatCore != null && !OCLHelper.getLinkList(unSatCore).isEmpty();
  }

  public boolean isPresentWitness() {
    return !diffWitness.isEmpty();
  }
}
