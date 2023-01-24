package de.monticore.ocl2smt.ocldiff;

import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPDiffResult;
import de.monticore.ocl2smt.ocldiff.operationDiff.OPWitness;
import de.monticore.ocl2smt.util.OCLConstraint;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odlink._ast.ASTODLink;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OCLDiffHelper {
  public static Set<OCLConstraint> invariant2SMT(
      OCL2SMTGenerator ocl2SMTGenerator, Set<ASTOCLCompilationUnit> oclSet) {
    return oclSet.stream()
        .flatMap(
            p ->
                ocl2SMTGenerator.ocl2smt(p.getOCLArtifact()).stream()
                    .filter(OCLConstraint::isInvariant)
                    .map(OCLConstraint::getInvariant))
        .collect(Collectors.toSet())
        .stream()
        .map(OCLConstraint::new)
        .collect(Collectors.toSet());
  }

  public  static ArrayList<IdentifiableBoolExpr> extractInv(Set<OCLConstraint> constraintList){
    return constraintList.stream().map(OCLConstraint::getInvariant)
            .collect(Collectors.toCollection(ArrayList::new));
  }

  private Set<OCLConstraint> negate(Set<OCLConstraint> constraints, Context ctx) {
    return constraints.stream().map(x -> x.negateInv(ctx)).collect(Collectors.toSet());
  }
  private Set<IdentifiableBoolExpr> negateId(Set<IdentifiableBoolExpr> constraints, Context ctx) {
    return constraints.stream().map(x -> x.negate(ctx)).collect(Collectors.toSet());
  }

  protected Pair<ASTODArtifact,> oclDiffOpHelper(
          OCL2SMTGenerator ocl2SMTGenerator,
          Set<IdentifiableBoolExpr> posConstraintList,
          Set<IdentifiableBoolExpr> negConstList,
          boolean partial) {

    List<ASTODLink> traceUnsat = new ArrayList<>();

    // add one by one all Constraints to the Solver and check if  it can always produce a Model
    for (IdentifiableBoolExpr negConstraint : negConstList) {
      posConstraintList.add(negConstraint);
      Solver solver =
              ocl2SMTGenerator.getCD2SMTGenerator().makeSolver(new ArrayList<>(posConstraintList));

      if (solver.check() == Status.SATISFIABLE) {
        Model model = solver.getModel();
        String invName =
                negConstraint.getInvariantName().orElse("NoInvName").split("_____NegInv")[0];

        diffWitness.add(ocl2SMTGenerator.buildOPOd(model, invName, partial));

      } else {
        traceUnsat.addAll(TraceUnsatCore.traceUnsatCore(solver));
      }
      posConstraintList.remove(negConstraint);
    }
    return new OCLOPDiffResult(
            TraceUnsatCore.buildUnsatOD(posConstraintList, negConstList, traceUnsat), diffWitness);
  }
}
