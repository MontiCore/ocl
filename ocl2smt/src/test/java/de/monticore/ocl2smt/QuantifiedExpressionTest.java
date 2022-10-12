package de.monticore.ocl2smt;

import com.microsoft.z3.*;


import de.monticore.cd2smt.Helper.Identifiable;
import de.monticore.cd2smt.context.ODArtifacts.SMTObject;
import de.monticore.cd2smt.smt2odgenerator.SMT2ODGenerator;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;


public class QuantifiedExpressionTest extends ExpressionAbstractTest  {

    @BeforeEach
    public void setup() throws IOException {
        parse("MinAuction.cd","QuantifiedExpr.ocl");

        cdContext = cd2SMTGenerator.cd2smt(cdAST, cdContext.getContext());
        ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);
        solver = cdContext.getContext().mkSolver();
    }

    private void checkAttrValue(String value, List<Identifiable<BoolExpr>> oclConstraints) {
        SMT2ODGenerator smt2ODGenerator = new SMT2ODGenerator();
        smt2ODGenerator.buildOd(cdContext,"MyOD",oclConstraints);

        Assertions.assertTrue(smt2ODGenerator.getObjectMap().entrySet().size() >= 2);
        for (SMTObject obj : new ArrayList<>(smt2ODGenerator.getObjectMap().values())) {
            Expr<Sort> attr = new ArrayList<>(obj.getAttributes().values()).get(0);
            Assertions.assertEquals(attr.getSExpr(), value);
        }
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
       Identifiable<BoolExpr> constraint = addConstraint("Auction_two_sat");
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
        List<Identifiable<BoolExpr>> oclConstraints = new ArrayList<>();
        oclConstraints.add(constraint);
        checkAttrValue("10",oclConstraints);
    }

    @Test
    public void Two_auction_and_bool_sat() {
        Identifiable<BoolExpr> constraint = addConstraint("Two_auction_and_bool_sat");
        Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
        List<Identifiable<BoolExpr>> oclConstraints = new ArrayList<>();
        oclConstraints.add(constraint);
        checkAttrValue("14", oclConstraints);
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
