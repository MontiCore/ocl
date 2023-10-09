package de.monticore.ocl2smt.ocldiff.invariantDiff;

import static de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy.Strategy.DEFAULT;
import static de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy.Strategy.FINITEDS;
import static de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy.Strategy.FINITESS;
import static de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData.Strategy.ME;

import com.microsoft.z3.Context;
import de.monticore.cd2smt.CDTypeInitializer;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FiniteOCLInvariantDiff extends OCLInvariantDiff {

  public ASTODArtifact oclWitness(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> ocl,
      Set<IdentifiableBoolExpr> addConstrs,
      Context ctx,
      long max,
      boolean partial) {
    this.ctx = ctx;
    Stream<Map<ASTCDType, Integer>> cardinalities = CDTypeInitializer.initialize(cd, max, true);
    AtomicReference<Optional<ASTODArtifact>> res = new AtomicReference<>();

    // iterate over all instantiation of the types universe
    boolean found =
        cardinalities.anyMatch(
            card -> {
              CD2SMTMill.init(FINITEDS, ME, DEFAULT, card);
              res.set(Optional.ofNullable(oclWitnessInternal(cd, ocl, addConstrs, partial)));
              return res.get().isPresent();
            });

    return res.get().get();
  }

  public OCLInvDiffResult oclDiff(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOcl,
      Set<IdentifiableBoolExpr> addConstr,
      Context ctx,
      long max,
      boolean partial) {
    this.ctx = ctx;

    // collect invariants
    List<ASTOCLInvariant> invariantList = collectInv(oldOcl);

    // compute diff witness for each invariant
    Set<ASTODArtifact> witnesses = new HashSet<>();
    invariantList.forEach(
        inv -> oclInvDiff(cd, newOcl, addConstr, inv, max, partial).map(witnesses::add));

    return new OCLInvDiffResult(null, witnesses);
  }

  public Optional<ASTODArtifact> oclInvDiff(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> newConstr,
      Set<IdentifiableBoolExpr> addConstr,
      ASTOCLInvariant inv,
      long max,
      boolean partial) {

    Stream<Map<ASTCDType, Integer>> cardinalities = CDTypeInitializer.initialize(cd, max, true);
    AtomicReference<OCLInvDiffResult> res = new AtomicReference<>();

    boolean found =
        cardinalities.anyMatch(
            card -> {
              Log.info("checking " + inv.getName() + "....", this.getClass().getName());

              CD2SMTMill.init(FINITESS, ME, DEFAULT, card);
              OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(cd, ctx);

              if (checkConsistency(cd, newConstr, addConstr, partial)) {

                List<IdentifiableBoolExpr> posConstraints =
                    invariant2SMT(ocl2SMTGenerator, newConstr);
                posConstraints.addAll(addConstr);

                List<IdentifiableBoolExpr> oldConstr = List.of(ocl2SMTGenerator.convertInv(inv));
                List<IdentifiableBoolExpr> negInvariant = negateId(oldConstr, ctx);

                res.set(oclDiffHelper(ocl2SMTGenerator, posConstraints, negInvariant, false));
              }
              Log.println("done");
              return res.get() != null && !res.get().getDiffWitness().isEmpty();
            });

    return Optional.empty();
  }

  private boolean checkConsistency(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> newOCL,
      Set<IdentifiableBoolExpr> addConstr,
      boolean partial) {
    if (oclWitnessInternal(cd, newOCL, addConstr, partial) == null) {
      Log.info("The Model is Inconsistent with The OCl Constraints", this.getClass().getName());
      return false;
    }

    return true;
  }

  List<ASTOCLInvariant> collectInv(Set<ASTOCLCompilationUnit> ocl) {
    return ocl.stream()
        .map(ast -> ast.getOCLArtifact().getOCLConstraintList())
        .flatMap(List::stream)
        .filter(c -> c instanceof ASTOCLInvariant)
        .map(c -> (ASTOCLInvariant) c)
        .collect(Collectors.toList());
  }
}
