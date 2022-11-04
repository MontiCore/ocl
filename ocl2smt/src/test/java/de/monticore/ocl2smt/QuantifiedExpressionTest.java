package de.monticore.ocl2smt;

import com.microsoft.z3.Status;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;


public class QuantifiedExpressionTest extends ExpressionAbstractTest {

    @BeforeAll
    public static void setup() throws IOException {
        Log.init();
        OCLMill.init();
        CD4CodeMill.init();
        parse("MinAuction.cd", "QuantifiedExpr.ocl");
        ocl2SMTGenerator = new OCL2SMTGenerator(cdAST);
    }


    @Test
    public void forall_boolean_sat() {
        addConstraint("B_fa_sat");
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);

    }

    @Test
    public void forall_boolean_unsat() {
        addConstraint("B_fa_unsat");
        Assertions.assertEquals(solver.check(), Status.UNSATISFIABLE);
    }

    @Test
    public void forall_two_boolean_sat() {
        addConstraint("BC_fa_sat");
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
    }

    @Test
    public void forall_auction_sat() {
        addConstraint("Auction_fa_sat");
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
    }

    @Test
    public void forall_auction_unsat() {
        addConstraint("Auction_fa_unsat");
        Assertions.assertEquals(solver.check(), Status.UNSATISFIABLE);
    }

    @Test
    public void forall_many_forall_sat() {
        addConstraint("Many_fa_sat");
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
    }

    @Test
    public void forall_many_forall_unsat() {
        addConstraint("Many_fa_unsat");
        Assertions.assertEquals(solver.check(), Status.UNSATISFIABLE);
    }

    @Test
    public void exists_boolean_sat() {
        addConstraint("B_ex_sat");
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
    }

    @Test
    public void exists_many_exists_sat() {
        addConstraint("Many_ex_sat");
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
    }

    @Test
    public void exists_many_exists_unsat() {
        addConstraint("Many_ex_unsat");
        Assertions.assertEquals(solver.check(), Status.UNSATISFIABLE);
    }

    @Test
    public void exists_boolean_unsat() {
        addConstraint("B_ex_unsat");
        Assertions.assertEquals(solver.check(), Status.UNSATISFIABLE);
    }

    @Test
    public void exists_two_boolean_unsat() {
        addConstraint("BC_ex_sat");
        Assertions.assertEquals(solver.check(), Status.UNSATISFIABLE);
    }

    @Test
    public void exists_auction_sat() {
        addConstraint("Auction_ex_sat");
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
    }

    @Test
    public void exists_auction_unsat() {
        addConstraint("Auction_ex_unsat");
        Assertions.assertEquals(solver.check(), Status.UNSATISFIABLE);
    }

    @Test
    public void Two_auction_sat() {
        addConstraint("Auction_two_sat");
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
    }

    @Test
    public void Two_auction_and_bool_sat() {
        addConstraint("Two_auction_and_bool_sat");
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
    }

    @Test
    public void Two_auction_and_bool_sat_unsat() {
        addConstraint("Ex_two_auction_and_bool_sat");
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
    }
    @Test
    public void Exists_two_auction_and_bool_sat_sat() {
        addConstraint("Two_auction_and_bool_unsat");
        Assertions.assertEquals(solver.check(), Status.UNSATISFIABLE);
    }
}
