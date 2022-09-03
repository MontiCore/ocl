package de.monticore.ocl2smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Status;
import de.monticore.odbasis._ast.ASTODArtifact;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AssociationTest extends ExpressionAbstractTest {
    @BeforeEach
    public void setup() throws IOException {
        parse("/associations/Auction.cd", "/associations/Association.ocl");
        cdContext = cd2SMTGenerator.cd2smt(cdAST);
        ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);
        solver = cdContext.getContext().mkSolver();
    }

    @Test
    public void simple_assoc() {
     BoolExpr constraint =  addConstraint("SimpleAssoc");
     Assertions.assertEquals(solver.check(), Status.SATISFIABLE);

     List<BoolExpr> constraintList = new ArrayList<>();
     constraintList.add(constraint);
       constraintList.add(addConstraint("Exists_Exact_4_Auction"));


        OCLDiffGenerator oclDiffGenerator = new OCLDiffGenerator();
        ASTODArtifact od = oclDiffGenerator.buildOd(cdContext,"SimpleAssoc1",constraintList,
           cdAST.getCDDefinition());

        printOD(od);

    }
}
