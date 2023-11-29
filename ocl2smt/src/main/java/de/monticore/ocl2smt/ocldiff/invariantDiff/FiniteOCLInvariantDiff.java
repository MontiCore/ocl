package de.monticore.ocl2smt.ocldiff.invariantDiff;

import static de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy.Strategy.DEFAULT;
import static de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy.Strategy.FINITEDS;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class FiniteOCLInvariantDiff implements OCLInvDiffStrategy {
  private final long max;

  public FiniteOCLInvariantDiff(long max) {
    this.max = max;
  }

  @Override
  public ASTODArtifact oclWitness(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> ocl,
      Set<IdentifiableBoolExpr> addConstrs,
      Context ctx,
      int timeout,
      boolean partial) {
    Stream<Map<ASTCDType, Integer>> cardinalities = CDTypeInitializer.initialize(cd, max, true);
    AtomicReference<Optional<ASTODArtifact>> res = new AtomicReference<>();
    AtomicInteger counter = new AtomicInteger(0);

    boolean found =
        cardinalities.anyMatch(
            card -> {
              Log.info("checking witness..." + counter, this.getClass().getSimpleName());
              CD2SMTMill.init(FINITEDS, ME, DEFAULT, card);
              // TODO:04.09.2023  make strategy choice flexible

              ASTODArtifact witness =
                  oclWitnessInternal(cd, ocl, addConstrs, ctx, timeout, partial);

              counter.incrementAndGet();
              res.set(Optional.ofNullable(witness));
              return res.get().isPresent();
            });

    if (found) {
      Log.info(": Witness found", this.getClass().getSimpleName());
    } else {
      Log.info(": Witness not found", this.getClass().getSimpleName());
    }

    return res.get().get();
  }

  @Override
  public OCLInvDiffResult oclInvDiff(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> newConstr,
      ASTOCLInvariant inv,
      Set<IdentifiableBoolExpr> addConstr,
      Context ctx,
      int timeout,
      boolean partial) {

    AtomicInteger counter = new AtomicInteger(0);
    String invName = inv.isPresentName() ? inv.getName() : "";

    Stream<Map<ASTCDType, Integer>> cardinalities = CDTypeInitializer.initialize(cd, max, true);
    AtomicReference<OCLInvDiffResult> res = new AtomicReference<>();

    boolean found =
        cardinalities.anyMatch(
            card -> {
              Log.info(
                  ": checking invariant " + invName + "..." + counter,
                  this.getClass().getSimpleName());

              CD2SMTMill.init(FINITEDS, ME, DEFAULT, card);
              OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(cd, ctx);

              if (checkConsistency(cd, newConstr, addConstr, ctx, timeout, partial)) {

                List<IdentifiableBoolExpr> posConstraints =
                    invariant2SMT(ocl2SMTGenerator, newConstr);
                posConstraints.addAll(addConstr);

                List<IdentifiableBoolExpr> oldConstr = List.of(ocl2SMTGenerator.convertInv(inv));
                List<IdentifiableBoolExpr> negInvariant = negateId(oldConstr, ctx);

                res.set(
                    computeDiff(ocl2SMTGenerator, posConstraints, negInvariant, timeout, false));
              }
              counter.getAndIncrement();
              return res.get() != null && !res.get().getDiffWitness().isEmpty();
            });

    return res.get();
  }
}
