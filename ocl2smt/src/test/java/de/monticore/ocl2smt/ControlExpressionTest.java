package de.monticore.ocl2smt;


import com.microsoft.z3.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ControlExpressionTest extends ExpressionAbstractTest {

    @BeforeEach
    public void setup() throws IOException {
        parse("MinAuction.cd", "ControlExpr.ocl");
        cdContext = cd2SMTGenerator.cd2smt(cdAST,cdContext.getContext());
        ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);
        solver = cdContext.getContext().mkSolver();
    }

    @Test
    public void if_then_else_boolean_sat() {
        addConstraint("ITE_sat");
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
    }

    @Test
    public void if_then_else_boolean_unsat1() {
        addConstraint("ITE_unsat1");
        Assertions.assertEquals(solver.check(), Status.UNSATISFIABLE);
    }

    @Test
    public void if_then_else_boolean_unsat2() {
        addConstraint("ITE_unsat2");
        Assertions.assertEquals(solver.check(), Status.UNSATISFIABLE);
    }
}
