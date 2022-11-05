package de.monticore.ocl2smt;

import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd4analysis.prettyprint.CD4AnalysisFullPrettyPrinter;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLConstraint;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.od4report.prettyprinter.OD4ReportFullPrettyPrinter;
import de.monticore.odbasis._ast.ASTODArtifact;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public abstract class ExpressionAbstractTest {
    protected static final String RELATIVE_MODEL_PATH = "src/test/resources/de/monticore/ocl2smt";
    protected static final String RELATIVE_TARGET_PATH = "target/generated/sources/annotationProcessor/java/ocl2smttest";

    protected static ASTOCLCompilationUnit oclAST;
    protected static ASTCDCompilationUnit cdAST;
    protected static Solver solver;
    protected static OCL2SMTGenerator ocl2SMTGenerator;

    protected static void parse(String cdFileName, String oclFileName) throws IOException {
        oclAST = OCL_Loader.loadAndCheckOCL(
                Paths.get(RELATIVE_MODEL_PATH, oclFileName).toFile(),
                Paths.get(RELATIVE_MODEL_PATH, cdFileName).toFile());

        cdAST = OCL_Loader.loadAndCheckCD(
                Path.of(RELATIVE_MODEL_PATH, cdFileName).toFile());
    }

    public static void printCD(ASTCDCompilationUnit cd, String name) {
        Path outputFile = Paths.get(RELATIVE_TARGET_PATH, name + ".od");
        try {
            FileUtils.writeStringToFile(outputFile.toFile(), new CD4AnalysisFullPrettyPrinter().prettyprint(cd), Charset.defaultCharset());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("It Was Not Possible to Print the Class Diagram");
        }
    }

    // Used to make the tests shorter & readable
    protected IdentifiableBoolExpr addConstraint(String search) {
        IdentifiableBoolExpr constraint = getConstraint(search);
        solver = ocl2SMTGenerator.cd2smtGenerator.makeSolver(List.of(constraint));
        return constraint;
    }

    protected IdentifiableBoolExpr getConstraint(String search) {
        ASTOCLConstraint constr = oclAST.getOCLArtifact().getOCLConstraintList()
                .stream().map(p -> (ASTOCLInvariant) p)
                .filter(p -> search.equals(p.getName())).findAny().get();
        return ocl2SMTGenerator.convertConstr(constr);
    }

    public void printOD(ASTODArtifact od) {
        Path outputFile = Paths.get(RELATIVE_TARGET_PATH, od.getObjectDiagram().getName() + ".od");
        try {
            FileUtils.writeStringToFile(outputFile.toFile(), new OD4ReportFullPrettyPrinter().prettyprint(od), Charset.defaultCharset());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("It Was Not Possible to Print the Object Diagram");
        }
    }



    void testInv(String invName) {
        List<IdentifiableBoolExpr> solverConstraints = new ArrayList<>();
        solverConstraints.add(getConstraint(invName));
        Solver solver = ocl2SMTGenerator.cd2smtGenerator.makeSolver(solverConstraints);
        org.junit.jupiter.api.Assertions.assertSame(Status.SATISFIABLE, solver.check());
        Optional<ASTODArtifact> od = ocl2SMTGenerator.cd2smtGenerator.smt2od(solver.getModel(), false, invName);
        org.junit.jupiter.api.Assertions.assertTrue(od.isPresent());
        printOD(od.get());
    }
    public void printSMTScript(String invName) {
        List<IdentifiableBoolExpr> actualConstraint = new ArrayList<>();
        actualConstraint.add(getConstraint(invName));
        Solver solver = ocl2SMTGenerator.cd2smtGenerator.makeSolver(actualConstraint);
        System.out.println(solver);
    }

    void testUnsatInv(String invName) {
        List<IdentifiableBoolExpr> solverConstraints = new ArrayList<>();
        solverConstraints.add(getConstraint(invName));
        Solver solver = ocl2SMTGenerator.cd2smtGenerator.makeSolver(solverConstraints);

        org.junit.jupiter.api.Assertions.assertSame(Status.UNSATISFIABLE, solver.check());
        ASTODArtifact od1 = TraceUnsatCore.buildUnsatOD(new ArrayList<>(), List.of(solverConstraints.get(0).negate(ocl2SMTGenerator.cd2smtGenerator.getContext())), TraceUnsatCore.traceUnsatCore(solver));
        printOD(od1);
    }



}
