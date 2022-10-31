package de.monticore.ocl2smt;

import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.odbasis._ast.ASTODArtifact;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SetExpressionsTest extends ExpressionAbstractTest {
    @BeforeEach
    public void setup() throws IOException {
        parse("setExpressions/Set.cd", "setExpressions/Set.ocl");
        ocl2SMTGenerator = new OCL2SMTGenerator(cdAST);
    }

    @Override
    void testInv(String invName) {
        List<IdentifiableBoolExpr> actualConstraint = new ArrayList<>();
        actualConstraint.add(getConstraint(invName));
        Solver solver = ocl2SMTGenerator.cd2smtGenerator.makeSolver(actualConstraint);
        Assertions.assertSame(Status.SATISFIABLE, solver.check());
        Optional<ASTODArtifact> od = ocl2SMTGenerator.cd2smtGenerator.smt2od(solver.getModel(), false, invName);
        org.junit.jupiter.api.Assertions.assertTrue(od.isPresent());
        printOD(od.get());
    }

    void testUnsatInv(String inVName) {
        List<IdentifiableBoolExpr> constraints = new ArrayList<>();
        constraints.add(getConstraint(inVName));
        Solver solver1 = ocl2SMTGenerator.cd2smtGenerator.makeSolver(constraints);
        Assertions.assertSame(solver1.check(), Status.UNSATISFIABLE);
    }

    public void printSMTScript(String invName) {
        List<IdentifiableBoolExpr> actualConstraint = new ArrayList<>();
        actualConstraint.add(getConstraint(invName));
        //   actualConstraint.add(getConstraint("Only_one_auction"));
        actualConstraint.add(getConstraint("Only_two_Person"));
        Solver solver = ocl2SMTGenerator.cd2smtGenerator.makeSolver(actualConstraint);
        System.out.println(solver);
    }

    @Test
    public void test_isin_set() {
        testInv("All_Person_in_All_Auctions");
    }

    @Test
    public void test_IsIn_notIn() {
        testInv("IsIn_notIn");
    }

    @Test
    public void test_notin_set() {
        testInv("One_Person_in_Any_Auctions");
    }

    @Test
    public void isin_notIn_unsat() {
        testUnsatInv("Counter_Example");
    }
    @Test
    public void test_Notin_isInT() {
        testInv("Notin_isIn");
    }

    @Test
    public void test_SetUnion_sat() {
        testInv("Set_Union_Sat");
    }

    @Test
    public void test_Set_Union_one_side_sat() {
        testInv("Set_Union_one_side_sat");
    }

    @Test
    public void test_Set_Union_one_side_Unsat() {
        testUnsatInv("Set_Union_one_side_Unsat");
    }

    @Test
    public void test_SetIntersect_sat() {
        testInv("Set_Intersection_Sat");
    }

    @Test
    public void test_Set_Intersection_Unsat() {
        testUnsatInv("Set_Intersection_Unsat");
    }

    @Test
    public void test_Set_Minus_sat() {
        testInv("Set_Minus_sat");
    }

    @Test
    public void test_Set_Minus_Unsat() {
        testUnsatInv("Set_Minus_Unsat");
    }

    @Disabled
    @Test
    public void test_Set_Construction() {
        testInv("Set_construction");
    }


}
