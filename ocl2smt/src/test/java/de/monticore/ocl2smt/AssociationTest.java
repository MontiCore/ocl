package de.monticore.ocl2smt;


import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.context.CDContext;
import de.monticore.cd2smt.smt2odgenerator.SMT2ODGenerator;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.odbasis._ast.ASTODArtifact;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

import java.util.*;

import java.util.stream.Collectors;

public class AssociationTest extends ExpressionAbstractTest {
    List<IdentifiableBoolExpr> constraintList;

    @BeforeEach
    public void setup() throws IOException {
        parse("/associations/Auction.cd", "/associations/Association.ocl");
        Set<ASTOCLCompilationUnit> oclFiles = new HashSet<>();
        oclFiles.add(oclAST);
        constraintList = OCLDiffGenerator.getPositiveSolverConstraints(cdAST, oclFiles);
    }

    void testInv(String invName) {
        List<IdentifiableBoolExpr> solverConstraints = new ArrayList<>();
        solverConstraints.add(addConstraint(invName));
        Solver solver =ocl2SMTGenerator.cd2smtGenerator.makeSolver(solverConstraints);
        Assertions.assertSame(solver.check(), Status.SATISFIABLE);

        Optional<ASTODArtifact> od =ocl2SMTGenerator.cd2smtGenerator.smt2od(solver.getModel(), false, invName);
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
    public void transitive_closure() throws  IOException{
        parse( "/Transitive-closure/transitiveClosure.cd","/Transitive-closure/transitiveClosure.ocl");
        cdContext = cd2SMTGenerator.cd2smt(cdAST,(buildContext()));
        ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);
        Set<ASTOCLCompilationUnit> ocls = new HashSet<>();
        ocls.add(oclAST);
        ASTODArtifact od =  OCLDiffGenerator.oclWitness(cdAST,ocls,cdContext.getContext(),false);
        printOD(od);
    }
}
