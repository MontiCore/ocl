package de.monticore.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.cd2smt.context.CDContext;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLConstraint;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.util.SymbolTableUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class OCLExpressionTest extends AbstractTest {
  protected static final String RELATIVE_MODEL_PATH = "src/ocl2smttest/resources/de.monticore.ocl2smt";
  protected CDContext cdContext;

  protected ASTOCLCompilationUnit oclAST;
  protected ASTCDCompilationUnit cdAST;
  protected Solver solver;
  protected OCL2SMTGenerator ocl2SMTGenerator;

  @BeforeEach
  public void setup() throws IOException {
    OCLMill.init();
    CD4CodeMill.init();

    cdAST = OCL_Loader.loadAndCheckCD(
        Paths.get(RELATIVE_MODEL_PATH, "MinAuction.cd").toFile());

    oclAST = OCL_Loader.loadAndCheckOCL(
        Paths.get(RELATIVE_MODEL_PATH, "Test02.ocl").toFile(),
        cdAST);

    cdContext = new CDContext(new Context());
    ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);
    solver = cdContext.getContext().mkSolver();
  }

  // Used to make the tests shorter & readable
  private void addConstraint(String search) {
    ASTOCLConstraint constr = oclAST.getOCLArtifact().getOCLConstraintList().stream().map(p -> (ASTOCLInvariant) p).filter(p -> search.equals(p.getName())).findAny().get();
    BoolExpr constraint = ocl2SMTGenerator.convertConstr(constr);

    solver.add(constraint);
  }

  @Test
  public void forall_boolean_sat() {
    addConstraint("b_sat");
    Assertions.assertEquals(solver.check(), Status.SATISFIABLE);

    Model m = solver.getModel();
    System.out.println("MODEL = " + m);
  }

  @Test
  public void forall_boolean_unsat() {
    addConstraint("b_unsat");
    Assertions.assertEquals(solver.check(), Status.UNSATISFIABLE);
  }

  @Test
  public void forall_two_boolean_sat() {
    addConstraint("bc_sat");
    Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
  }

  @Test
  @Disabled
  public void forall_auction() {
    addConstraint("auction_sat");
    Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
  }


}
