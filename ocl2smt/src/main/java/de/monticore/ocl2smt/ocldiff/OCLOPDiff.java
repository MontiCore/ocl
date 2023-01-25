package de.monticore.ocl2smt.ocldiff;

import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator;
import de.monticore.ocl2smt.ocl2smt.OCL2SMTStrategy;
import de.monticore.ocl2smt.util.OCLConstraint;
import de.monticore.ocl2smt.util.OPDiffResult;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odlink._ast.ASTODLink;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class OCLOPDiff {

  protected static Context ctx;

  protected static OPDiffResult oclWitness(
      ASTCDCompilationUnit ast, Set<ASTOCLCompilationUnit> in, boolean partial) {

    resetContext();
    OCL2SMTStrategy.buildPreCD(ast);
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(ast, ctx);

    Set<OCLConstraint> constraints = opConst2smt(ocl2SMTGenerator, in);

    // add the pre-condition
    List<IdentifiableBoolExpr> solversConstraints =
        constraints.stream()
            .filter(OCLConstraint::isOpConstraint)
            .map(OCLConstraint::getPreCond)
            .collect(Collectors.toList());

    // add the post condition
    solversConstraints.addAll(
        constraints.stream()
            .filter(OCLConstraint::isOpConstraint)
            .map(OCLConstraint::getPostCond)
            .collect(Collectors.toList()));

    // add the invariants    TODO:prePost with Invariants
    solversConstraints.addAll(
        constraints.stream()
            .filter(OCLConstraint::isInvariant)
            .map(OCLConstraint::getInvariant)
            .collect(Collectors.toList()));

    Solver solver = ocl2SMTGenerator.getCD2SMTGenerator().makeSolver(solversConstraints);

    if (solver.check() != Status.SATISFIABLE) {
      Log.info("there are no Model for the List Of Positive Constraints", "NOWitnessOD");
      return null;
    }

    Model model = solver.getModel();
    return ocl2SMTGenerator.buildOPOd(model, "Witness", partial);
  }

  public static Pair<ASTODArtifact, Set<OPDiffResult>> oclDiffOp(
      ASTCDCompilationUnit ast,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOcl,
      boolean partial) {

    // setup
    resetContext();
    Pair<ASTODArtifact, Set<OPDiffResult>> res = null;
    OCL2SMTStrategy.buildPreCD(ast);
    OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(ast, ctx);

    Map<ASTOCLMethodSignature, List<ASTOCLOperationConstraint>> newOpMap = sortOPConstraint(newOcl);
    Map<ASTOCLMethodSignature, List<ASTOCLOperationConstraint>> oldOpMap = sortOPConstraint(oldOcl);
    // TODO: fix this  in case of many operations
    for (Map.Entry<ASTOCLMethodSignature, List<ASTOCLOperationConstraint>> newOp :
        newOpMap.entrySet()) {
      for (Map.Entry<ASTOCLMethodSignature, List<ASTOCLOperationConstraint>> oldOp :
          oldOpMap.entrySet()) {
        if (newOp.getKey().deepEquals(oldOp.getKey())) {
          res = oclDiffSingleOp(oldOp.getValue(), newOp.getValue(), ocl2SMTGenerator, partial);
        }
      }
    }

    return res;
  }
  // TODO: fix this  in case of many  constraints per operation
  private static Pair<ASTODArtifact, Set<OPDiffResult>> oclDiffSingleOp(
      List<ASTOCLOperationConstraint> oldConstraintList,
      List<ASTOCLOperationConstraint> newConstraintList,
      OCL2SMTGenerator ocl2SMTGenerator,
      boolean partial) {
    OCLConstraint newConstraint = ocl2SMTGenerator.convertOpConst(newConstraintList.get(0));
    OCLConstraint oldConstraint = ocl2SMTGenerator.convertOpConst(oldConstraintList.get(0));

    Set<IdentifiableBoolExpr> posConstraint = new HashSet<>();
    posConstraint.add(newConstraint.getPreCond());
    posConstraint.add(newConstraint.getPostCond());
    posConstraint.add(oldConstraint.getPreCond());

    Set<IdentifiableBoolExpr> negConstraints = new HashSet<>();
    negConstraints.add(oldConstraint.getPostCond().negate(ctx));

    return oclDiffOpHelper(ocl2SMTGenerator, posConstraint, negConstraints, partial);
  }

  private static Map<ASTOCLMethodSignature, List<ASTOCLOperationConstraint>> sortOPConstraint(
      Set<ASTOCLCompilationUnit> oclSet) {
    List<ASTOCLConstraint> constraintList =
        oclSet.stream()
            .map(ocl -> ocl.getOCLArtifact().getOCLConstraintList())
            .collect(Collectors.toList())
            .stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
    Map<ASTOCLMethodSignature, List<ASTOCLOperationConstraint>> res = new HashMap<>();
    for (ASTOCLConstraint constraint : constraintList) {

      if (constraint instanceof ASTOCLOperationConstraint) {
        ASTOCLOperationConstraint opConstraint = (ASTOCLOperationConstraint) constraint;
        ASTOCLOperationSignature opSignature = opConstraint.getOCLOperationSignature();

        if (opSignature instanceof ASTOCLMethodSignature) {

          if (containsKey(res, (ASTOCLMethodSignature) opSignature)) {
            res.get((ASTOCLMethodSignature) opSignature).add(opConstraint);
          } else {
            List<ASTOCLOperationConstraint> opConstraintList = new ArrayList<>();
            opConstraintList.add(opConstraint);
            res.put((ASTOCLMethodSignature) opSignature, opConstraintList);
          }
        }
      }
    }
    return res;
  }

  public static boolean containsKey(
      Map<ASTOCLMethodSignature, List<ASTOCLOperationConstraint>> map,
      ASTOCLMethodSignature method) {
    return map.keySet().stream().anyMatch(key -> key.deepEquals(method));
  }

  protected static Pair<ASTODArtifact, Set<OPDiffResult>> oclDiffOpHelper(
      OCL2SMTGenerator ocl2SMTGenerator,
      Set<IdentifiableBoolExpr> posConstraintList,
      Set<IdentifiableBoolExpr> negConstList,
      boolean partial) {

    Set<OPDiffResult> satOdList = new HashSet<>();
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

        satOdList.add(ocl2SMTGenerator.buildOPOd(model, invName, partial));

      } else {
        traceUnsat.addAll(TraceUnsatCore.traceUnsatCore(solver));
      }
      posConstraintList.remove(negConstraint);
    }
    return new ImmutablePair<>(
        TraceUnsatCore.buildUnsatOD(posConstraintList, negConstList, traceUnsat), satOdList);
  }

  protected static Set<OCLConstraint> opConst2smt(
      OCL2SMTGenerator ocl2SMTGenerator, Set<ASTOCLCompilationUnit> in) {
    return in.stream()
        .flatMap(p -> ocl2SMTGenerator.ocl2smt(p.getOCLArtifact()).stream())
        .collect(Collectors.toSet());
  }

  public static void resetContext() {
    Map<String, String> cfg = new HashMap<>();
    cfg.put("model", "true");
    ctx = new Context(cfg);
  }
}
