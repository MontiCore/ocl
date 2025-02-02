/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl2smt.OCL2SMTAbstractTest;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.ocldiff.TraceUnSatCore;
import de.monticore.ocl2smt.util.OCL_Loader;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.logging.Log;
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
  protected static OCL2SMTGenerator ocl2SMTGenerator;

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

  public boolean testInv(String invName, String directory) {
    List<IdentifiableBoolExpr> solverConstraints = new ArrayList<>();
    solverConstraints.add(getConstraint(invName));
    Log.println("checked Invariant: " + solverConstraints.get(0).getValue().toString());

    Solver solver = ocl2SMTGenerator.getCD2SMTGenerator().makeSolver(solverConstraints);
    Status res = solver.check();
    Log.println("Check result: " + res.name());
    Log.println("UnsatCore:" + Arrays.toString(solver.getUnsatCore()));

    Optional<ASTODArtifact> od =
        ocl2SMTGenerator.getCD2SMTGenerator().smt2od(solver.getModel(), false, invName);
    org.junit.jupiter.api.Assertions.assertTrue(od.isPresent());
    IOHelper.printOD(od.get(), Path.of(RELATIVE_TARGET_PATH + directory));
    return res == Status.SATISFIABLE;
  }

  public boolean testUnsatInv(Set<String> invNames, String directory) {
    List<IdentifiableBoolExpr> solverConstraints = new ArrayList<>();
    invNames.forEach(name -> solverConstraints.add(getConstraint(name)));
    Context ctx = ocl2SMTGenerator.getCD2SMTGenerator().getContext();
    Solver solver = ocl2SMTGenerator.getCD2SMTGenerator().makeSolver(solverConstraints);

    boolean res = Status.UNSATISFIABLE == solver.check();
    if (res) {
      ASTODArtifact od =
          TraceUnSatCore.buildUnSatOD(
              new ArrayList<>(),
              List.of(solverConstraints.get(0).negate(ctx)),
              TraceUnSatCore.traceUnSatCore(solver));

      IOHelper.printOD(od, Path.of(RELATIVE_TARGET_PATH + directory));
    }
    return res;
  }
}
