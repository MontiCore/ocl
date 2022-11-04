package de.monticore.ocl2smt;


import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssociationTest extends ExpressionAbstractTest {

    @BeforeAll
    public static void setup() throws IOException {
        Log.init();
        OCLMill.init();
        CD4CodeMill.init();
        parse("/associations/Association.cd", "/associations/Association.ocl");
        ocl2SMTGenerator = new OCL2SMTGenerator(cdAST);
    }

    @Override
    void testInv(String invName) {
        List<IdentifiableBoolExpr> solverConstraints = new ArrayList<>();
        solverConstraints.add(addConstraint(invName));
        Solver solver = ocl2SMTGenerator.cd2smtGenerator.makeSolver(solverConstraints);
        Assertions.assertSame(Status.SATISFIABLE, solver.check());
        Optional<ASTODArtifact> od = ocl2SMTGenerator.cd2smtGenerator.smt2od(solver.getModel(), false, invName);
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

    @Test
    public void TestNestedFieldAccessExpr() {
        testInv("NestedFieldAccessExpr");
    }

   /* @Test
    public void transitive_closure() throws IOException {
        parse("/Transitive-closure/transitiveClosure.cd", "/Transitive-closure/transitiveClosure.ocl");
        ocl2SMTGenerator = new OCL2SMTGenerator(cdAST);
        ocl2SMTGenerator.cd2smtGenerator.cd2smt(cdAST, (buildContext()));
        Set<ASTOCLCompilationUnit> ocls = new HashSet<>();
        ocls.add(oclAST);
        ASTODArtifact od = OCLDiffGenerator.oclWitness(cdAST, ocls, false);
        printOD(od);
    }*/
}
