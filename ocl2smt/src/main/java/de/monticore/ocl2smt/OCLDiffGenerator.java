package de.monticore.ocl2smt;



import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odlink._ast.ASTODLink;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class OCLDiffGenerator {

    protected static OCL2SMTGenerator ocl2SMTGenerator;

    protected static List<IdentifiableBoolExpr> getPositiveSolverConstraints(ASTCDCompilationUnit astcd, Set<ASTOCLCompilationUnit> in) {
        ocl2SMTGenerator = new OCL2SMTGenerator(astcd);

        return in.stream().flatMap(p -> ocl2SMTGenerator.ocl2smt(p.getOCLArtifact()).stream())
                .collect(Collectors.toList());
    }


    public static ASTODArtifact oclWitness(ASTCDCompilationUnit astcd, Set<ASTOCLCompilationUnit> in, boolean partial) {
        List<IdentifiableBoolExpr> solverConstraints = getPositiveSolverConstraints(astcd, in);

        //check if they exist a model for the list of positive Constraint
        Solver solver = ocl2SMTGenerator.cd2smtGenerator.makeSolver(solverConstraints);
        if (solver.check() != Status.SATISFIABLE) {
            Log.error("there are no Model for the List Of Positive Constraints");
        }

        return buildOd(solver.getModel(), "Witness", partial).get();
    }

    public static ASTODArtifact oclWitness(ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> in) {
        return oclWitness(cd, in, false);
    }


    public static Pair<ASTODArtifact, Set<ASTODArtifact>> oclDiff(ASTCDCompilationUnit astcd, Set<ASTOCLCompilationUnit> in, Set<ASTOCLCompilationUnit> notIn,boolean partial) {
        //check if they exist a model for the list of positive Constraint
        oclWitness(astcd, in, false);

        Set<ASTODArtifact> satOdList = new HashSet<>();
        List<ASTODLink> traceUnsat = new ArrayList<>();
        List<IdentifiableBoolExpr> solverConstraints = getPositiveSolverConstraints(astcd, in);

        //negative ocl constraints
        List<IdentifiableBoolExpr> negConstList = new ArrayList<>();
        notIn.forEach(p -> ocl2SMTGenerator.ocl2smt(p.getOCLArtifact()).forEach(idf -> negConstList.add(idf.negate(ocl2SMTGenerator.cd2smtGenerator.getContext()))));

        //add one by one all Constraints to the Solver and check if  it can always produce a Model
        for (IdentifiableBoolExpr negConstraint : negConstList) {
            solverConstraints.add(negConstraint);
            Solver solver =ocl2SMTGenerator.cd2smtGenerator.makeSolver(solverConstraints);

            if (solver.check() == Status.SATISFIABLE) {
                satOdList.add(buildOd(solver.getModel(), negConstraint.getInvariantName().orElse("NoInvName").split("_____NegInv")[0], partial).get());
            } else {
                traceUnsat.addAll(TraceUnsatCore.traceUnsatCore(solver));
            }
            solverConstraints.remove(negConstraint);
        }
        return new ImmutablePair<>(TraceUnsatCore.buildUnsatOD(solverConstraints, negConstList, traceUnsat), satOdList);
    }

    public static Pair<ASTODArtifact, Set<ASTODArtifact>> oclDiff(ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> in, Set<ASTOCLCompilationUnit> notIn) {
        return oclDiff(cd, in, notIn, false);
    }

    protected static Optional<ASTODArtifact> buildOd(Model model, String ODName, boolean partial) {
        return ocl2SMTGenerator.cd2smtGenerator.smt2od(model, partial, ODName);
    }
}
