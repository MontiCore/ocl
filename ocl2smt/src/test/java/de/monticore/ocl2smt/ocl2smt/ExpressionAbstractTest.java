/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl2smt.OCL2SMTAbstractTest;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.ocl2smt.expr2smt.Z3ExprAdapter;
import de.monticore.ocl2smt.ocldiff.TraceUnSatCore;
import de.monticore.ocl2smt.util.OCL_Loader;
import de.monticore.odbasis._ast.ASTODArtifact;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public abstract class ExpressionAbstractTest extends OCL2SMTAbstractTest {
  protected static final String RELATIVE_MODEL_PATH = "src/test/resources/de/monticore/ocl2smt";
  protected static final String RELATIVE_TARGET_PATH =
      "target/generated/sources/annotationProcessor/java/ocl2smttest/";

  protected static ASTOCLCompilationUnit oclAST;
  protected static ASTCDCompilationUnit cdAST;
  protected static Solver solver;
  protected static OCL2SMTGenerator<Z3ExprAdapter> ocl2SMTGenerator;

  protected void parse(String cdFileName, String oclFileName) throws IOException {
    oclAST =
        OCL_Loader.loadAndCheckOCL(
            Paths.get(RELATIVE_MODEL_PATH, oclFileName).toFile(),
            Paths.get(RELATIVE_MODEL_PATH, cdFileName).toFile());
    cdAST = OCL_Loader.loadAndCheckCD(Path.of(RELATIVE_MODEL_PATH, cdFileName).toFile());
  }

  // Used to make the tests shorter & readable
  protected IdentifiableBoolExpr addConstraint(String search) {
    IdentifiableBoolExpr constraint = getConstraint(search);
    solver = ocl2SMTGenerator.getCD2SMTGenerator().makeSolver(List.of(constraint));
    return constraint;
  }

  protected IdentifiableBoolExpr getConstraint(String search) {
    ASTOCLInvariant constr =
        oclAST.getOCLArtifact().getOCLConstraintList().stream()
            .map(p -> (ASTOCLInvariant) p)
            .filter(p -> search.equals(p.getName()))
            .findAny()
            .get();
    return ocl2SMTGenerator.convertInv(constr);
  }

  public void testInv(String invName, String directory) {
    List<IdentifiableBoolExpr> solverConstraints = new ArrayList<>();
    solverConstraints.add(getConstraint(invName));
    Solver solver = ocl2SMTGenerator.getCD2SMTGenerator().makeSolver(solverConstraints);
    org.junit.jupiter.api.Assertions.assertSame(Status.SATISFIABLE, solver.check());
    Optional<ASTODArtifact> od =
        ocl2SMTGenerator.getCD2SMTGenerator().smt2od(solver.getModel(), false, invName);
    org.junit.jupiter.api.Assertions.assertTrue(od.isPresent());
    IOHelper.printOD(od.get(), Path.of(RELATIVE_TARGET_PATH + directory));
  }

  public void testUnsatInv(Set<String> invNames, String directory) {
    List<IdentifiableBoolExpr> solverConstraints = new ArrayList<>();
    invNames.forEach(name -> solverConstraints.add(getConstraint(name)));

    IOHelper.printOD(checkUnSat(solverConstraints), Path.of(RELATIVE_TARGET_PATH + directory));
  }

  public ASTODArtifact checkUnSat(List<IdentifiableBoolExpr> solverConstraints) {
    Solver solver = ocl2SMTGenerator.getCD2SMTGenerator().makeSolver(solverConstraints);
    org.junit.jupiter.api.Assertions.assertSame(Status.UNSATISFIABLE, solver.check());
    return TraceUnSatCore.buildUnSatOD(
        new ArrayList<>(),
        List.of(
            solverConstraints.get(0).negate(ocl2SMTGenerator.getCD2SMTGenerator().getContext())),
        TraceUnSatCore.traceUnSatCore(solver));
  }
}
