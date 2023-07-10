package de.monticore.ocl2smt.ocldiff.invariantDiff;

import com.microsoft.z3.Context;
import de.monticore.cd2smt.CDTypeInitializer;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData;
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

    public OCLInvDiffResult oclDiffFinite(
            ASTCDCompilationUnit cd,
            Set<ASTOCLCompilationUnit> oldOcl,
            Set<ASTOCLCompilationUnit> newOcl,
            Set<IdentifiableBoolExpr> additionalConstraints,
            Context ctx,
            long max,
            boolean partial) {
        this.ctx = ctx;
        List<ASTOCLInvariant> collectInv =
                oldOcl.stream()
                        .map(ast -> ast.getOCLArtifact().getOCLConstraintList())
                        .flatMap(List::stream)
                        .filter(c -> c instanceof ASTOCLInvariant)
                        .map(c -> (ASTOCLInvariant) c)
                        .collect(Collectors.toList());

        Set<ASTODArtifact> witnesses = new HashSet<>();
        for (ASTOCLInvariant inv : collectInv) {
            Optional<ASTODArtifact> diffResult = oclInvDiff(cd, newOcl, additionalConstraints, inv, max);
            diffResult.ifPresent(witnesses::add);
        }
        return new OCLInvDiffResult(null, witnesses);
    }

    public Optional<ASTODArtifact> oclInvDiff(
            ASTCDCompilationUnit cd,
            Set<ASTOCLCompilationUnit> newOCL,
            Set<IdentifiableBoolExpr> additionalConstraints,
            ASTOCLInvariant oldInvariant,
            long max) {
        Stream<Map<ASTCDType, Integer>> cardinalities = CDTypeInitializer.initialize(cd, max, true);
        AtomicReference<OCLInvDiffResult> res = new AtomicReference<>();

        boolean b =
                cardinalities.anyMatch(
                        card -> {
                            Log.info("Solving " + oldInvariant.getName() + "....", this.getClass().getName());
                            CD2SMTMill.init(
                                    ClassStrategy.Strategy.FINITEDS,
                                    InheritanceData.Strategy.ME,
                                    AssociationStrategy.Strategy.DEFAULT,
                                    card);

                            OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(cd, ctx);
                            if (oclWitnessInternal(cd, newOCL, new HashSet<>(), false) == null) {
                                Log.info("XXXXXXXXX-Inconsistence-------XXXXXXXX", this.getClass().getName());
                            }
                            List<IdentifiableBoolExpr> newConstraints = invariant2SMT(ocl2SMTGenerator, newOCL);
                            newConstraints.addAll(additionalConstraints);

                            List<IdentifiableBoolExpr> oldConstraints =
                                    List.of(ocl2SMTGenerator.convertInv(oldInvariant));
                            List<IdentifiableBoolExpr> negInvariant = negateId(oldConstraints, ctx);
                            res.set(oclDiffHelper(ocl2SMTGenerator, newConstraints, negInvariant, false));
                            if (res.get().getDiffWitness().size() != 0) {
                                return true;
                            } else {
                                return false;
                            }
                        });

        if (res.get() != null && !res.get().getDiffWitness().isEmpty()) {
            return Optional.ofNullable(res.get().getDiffWitness().iterator().next());
        }
        return Optional.empty();
    }
}
