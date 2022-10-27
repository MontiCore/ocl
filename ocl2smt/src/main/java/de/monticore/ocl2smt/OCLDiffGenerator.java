package de.monticore.ocl2smt;


import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.smt2odgenerator.SMT2ODGenerator;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odlink._ast.ASTODLink;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class OCLDiffGenerator {
    protected static CD2SMTGenerator cd2SMTGenerator = new CD2SMTGenerator();
    protected static Context ctx;
    protected static OCL2SMTGenerator ocl2SMTGenerator;

    protected static List<IdentifiableBoolExpr> getPositiveSolverConstraints(ASTCDCompilationUnit astcd, Set<ASTOCLCompilationUnit> in, Context context) {
        //convert the cd to an SMT context
        cd2SMTGenerator.cd2smt(astcd, context);

        //transform positive ocl files    in a list of SMT BoolExpr
        ocl2SMTGenerator = new OCL2SMTGenerator(cd2SMTGenerator);

        return in.stream().flatMap(p -> ocl2SMTGenerator.ocl2smt(p.getOCLArtifact()).stream())
                .collect(Collectors.toList());
    }


    public static ASTODArtifact oclWitness(ASTCDCompilationUnit astcd, Set<ASTOCLCompilationUnit> in, Context context, boolean partial) {
        List<IdentifiableBoolExpr> solverConstraints = getPositiveSolverConstraints(astcd, in, context);

        //check if they exist a model for the list of positive Constraint
        Solver solver = CD2SMTGenerator.makeSolver(ctx, solverConstraints);
        if (solver.check() != Status.SATISFIABLE) {
            Log.error("there are no Model for the List Of Positive Constraints");
        }

        return buildOd(solver, astcd.getCDDefinition(), "Witness", partial).get();
    }

    public static ASTODArtifact oclWitness(ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> in, Context context) {
        return oclWitness(cd, in, context, false);
    }


    public static Pair<ASTODArtifact, Set<ASTODArtifact>> oclDiff(ASTCDCompilationUnit astcd, Set<ASTOCLCompilationUnit> in, Set<ASTOCLCompilationUnit> notIn, Context context, boolean partial) {
        Set<ASTODArtifact> satOdList = new HashSet<>();
        List<ASTODLink> traceUnsat = new ArrayList<>();
        List<IdentifiableBoolExpr> solverConstraints = getPositiveSolverConstraints(astcd, in, context);

        //negative ocl constraints
        List<IdentifiableBoolExpr> negConstList = new ArrayList<>();
        notIn.forEach(p -> ocl2SMTGenerator.ocl2smt(p.getOCLArtifact()).forEach(idf -> negConstList.add(idf.negate(ctx))));

        //check if they exist a model for the list of positive Constraint
        oclWitness(astcd, in, context, false);

        //add one by one all Constraints to the Solver and check if  it can always produce a Model
        for (IdentifiableBoolExpr negConstraint : negConstList) {
            solverConstraints.add(negConstraint);
            Solver solver = CD2SMTGenerator.makeSolver(ctx, solverConstraints);

            if (solver.check() == Status.SATISFIABLE) {
                satOdList.add(buildOd(solver, astcd.getCDDefinition(), negConstraint.getInvariantName().orElse("NoInvName").split("_____NegInv")[0], partial).get());
            } else {
                traceUnsat.addAll(TraceUnsatCore.traceUnsatCore(solver));
            }
            solverConstraints.remove(negConstraint);
        }
        return new ImmutablePair<>(TraceUnsatCore.buildUnsatOD(solverConstraints, negConstList, traceUnsat), satOdList);
    }

    public static Pair<ASTODArtifact, Set<ASTODArtifact>> oclDiff(ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> in, Set<ASTOCLCompilationUnit> notIn, Context context) {
        return oclDiff(cd, in, notIn, context, false);
    }

    protected static Optional<ASTODArtifact> buildOd(Solver solver, ASTCDDefinition cd, String ODName, boolean partial) {
        SMT2ODGenerator smt2ODGenerator = new SMT2ODGenerator();
        return smt2ODGenerator.buildOdFromSolver(solver, cd2SMTGenerator, cd, ODName, partial);
    }
}
