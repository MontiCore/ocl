package de.monticore.ocl2smt.ocldiff;

import com.microsoft.z3.Context;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.ocl2smt.util.OCLConstraint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

  public static ArrayList<IdentifiableBoolExpr> extractInv(Set<OCLConstraint> constraintList) {
    return constraintList.stream()
        .map(OCLConstraint::getInvariant)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static Set<OCLConstraint> negate(Set<OCLConstraint> constraints, Context ctx) {
    return constraints.stream().map(x -> x.negateInv(ctx)).collect(Collectors.toSet());
  }

  public static Set<IdentifiableBoolExpr> negateId(
      Set<IdentifiableBoolExpr> constraints, Context ctx) {
    return constraints.stream().map(x -> x.negate(ctx)).collect(Collectors.toSet());
  }

  public static String buildInvName(IdentifiableBoolExpr constr) {
    return constr.getInvariantName().orElse("NoInvName").split("_____NegInv")[0];
  }

  public static Context buildContext() {
    Map<String, String> cfg = new HashMap<>();
    cfg.put("model", "true");
    return new Context(cfg);
  }
}
