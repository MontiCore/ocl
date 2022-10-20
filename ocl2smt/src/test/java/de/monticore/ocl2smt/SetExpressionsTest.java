package de.monticore.ocl2smt;

import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.context.CDContext;
import de.monticore.odbasis._ast.ASTODArtifact;
import org.gradle.internal.impldep.org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SetExpressionsTest extends ExpressionAbstractTest {
    @BeforeEach
    public void setup() throws IOException {
        parse("setExpressions/Set.cd", "setExpressions/Set.ocl");
        cdContext = cd2SMTGenerator.cd2smt(cdAST, (buildContext()));
        ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);
    }

    @Override
    void testInv(String invName) {
        List<IdentifiableBoolExpr> actualConstraint = new ArrayList<>();
        actualConstraint.add(getConstraint(invName));
       // actualConstraint.add(getConstraint("Only_one_auction"));
        actualConstraint.add(getConstraint("Only_two_Person"));
        Solver solver = CDContext.makeSolver(cdContext.getContext(),actualConstraint);
        Assertions.assertSame(Status.SATISFIABLE, solver.check());
        Optional<ASTODArtifact> od = OCLDiffGenerator.buildOd(solver,cdContext, invName,false);
        org.junit.jupiter.api.Assertions.assertTrue(od.isPresent());
        printOD(od.get());
    }
    void testUnsatInv(String inVName){
        List<IdentifiableBoolExpr> constraints = new ArrayList<>(cdContext.getAssociationConstraints());
        constraints.addAll(cdContext.getInheritanceConstraints());
        constraints.add(getConstraint(inVName));
        Solver solver1 = CDContext.makeSolver(cdContext.getContext(), constraints);
        Assertions.assertSame(solver1.check(), Status.UNSATISFIABLE);
    }

    public void printSMTScript(String invName){
        List<IdentifiableBoolExpr> actualConstraint = new ArrayList<>();
        actualConstraint.add(getConstraint(invName));
     //   actualConstraint.add(getConstraint("Only_one_auction"));
        actualConstraint.add(getConstraint("Only_two_Person"));
        Solver solver = CDContext.makeSolver(cdContext.getContext(),actualConstraint);
        System.out.println(solver);
    }

    @Test
    public void test_isin_set() {
        testInv("All_Person_in_All_Auctions");
    }

    @Test
    public void test_notin_set() {
        testInv("One_Person_in_Any_Auctions");
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
    @Ignore
    @Test
    public void test_Set_Construction() {
        testInv("Set_construction");
    }

    @Test
    public void test_printSMTScript(){
        printSMTScript("All_Person_in_All_Auctions");
    }
    @Test
    public void test_printSMT(){
        printSMTScript("NoName");
        testInv("NoName");
    }
    @Test
    public void test_printSMT2(){
        printSMTScript("Test2");
        testInv("Test2");
    }


}
