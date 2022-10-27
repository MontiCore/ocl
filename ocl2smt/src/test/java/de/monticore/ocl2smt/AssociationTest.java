package de.monticore.ocl2smt;


import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.smt2odgenerator.SMT2ODGenerator;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.odbasis._ast.ASTODArtifact;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

public class AssociationTest extends ExpressionAbstractTest {
    List<IdentifiableBoolExpr> constraintList;

    @BeforeEach
    public void setup() throws IOException {
        parse("/associations/Auction.cd", "/associations/Association.ocl");
        Set<ASTOCLCompilationUnit> oclFiles = new HashSet<>();
        oclFiles.add(oclAST);
        constraintList = OCLDiffGenerator.getPositiveSolverConstraints(cdAST, oclFiles, buildContext());
        cd2SMTGenerator.cd2smt(cdAST,ctx);
    }

    void testInv(String invName) {
        List<IdentifiableBoolExpr> solverConstraints = new ArrayList<>();
        solverConstraints.add(addConstraint(invName));
        Solver solver = CD2SMTGenerator.makeSolver(cd2SMTGenerator.getContext(), solverConstraints);
        Assertions.assertSame(solver.check(), Status.SATISFIABLE);

        SMT2ODGenerator smt2ODGenerator = new SMT2ODGenerator();
        Optional<ASTODArtifact> od = smt2ODGenerator.buildOdFromSolver(solver, cd2SMTGenerator, cdAST.getCDDefinition(), invName, false);
        Assertions.assertTrue(od.isPresent());
        printOD(od.get());
    }

    @Test
    public void of_legal_age() {
        testInv("Of_legal_age");
    }

    @Test
    public void different_ids() {
        testInv("Diff_ids");
    }

    @Test
    public void atLeast2Person() {
        testInv("AtLeast_2_Person");
    }

    @Test
    public void Same_Person_in_2_Auction() {
        testInv("Same_Person_in_2_Auction");
    }
}
