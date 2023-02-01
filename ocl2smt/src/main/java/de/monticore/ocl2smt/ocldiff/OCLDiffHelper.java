package de.monticore.ocl2smt.ocldiff;

import com.microsoft.z3.Context;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.ocl2smt.util.OCLConstraint;
import java.util.*;
import java.util.stream.Collectors;

public class OCLDiffHelper {
  public static List<IdentifiableBoolExpr> invariant2SMT(
      OCL2SMTGenerator ocl2SMTGenerator, Set<ASTOCLCompilationUnit> oclSet) {
    return oclSet.stream()
        .flatMap(x -> ocl2SMTGenerator.inv2smt(x.getOCLArtifact()).stream())
        .collect(Collectors.toList());
  }

  public static ArrayList<IdentifiableBoolExpr> extractInv(Set<OCLConstraint> constraintList) {
    return constraintList.stream()
        .map(OCLConstraint::getInvariant)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static Set<OCLConstraint> negate(Set<OCLConstraint> constraints, Context ctx) {
    return constraints.stream().map(x -> x.negateInv(ctx)).collect(Collectors.toSet());
  }

  public static List<IdentifiableBoolExpr> negateId(
      List<IdentifiableBoolExpr> constraints, Context ctx) {
    return constraints.stream().map(x -> x.negate(ctx)).collect(Collectors.toList());
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
