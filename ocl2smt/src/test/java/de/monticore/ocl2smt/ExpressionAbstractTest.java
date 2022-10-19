package de.monticore.ocl2smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;

import com.microsoft.z3.Status;
import de.monticore.cd2smt.Helper.Identifiable;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.context.CDContext;
import de.monticore.cd2smt.smt2odgenerator.SMT2ODGenerator;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLConstraint;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.od4report.prettyprinter.OD4ReportFullPrettyPrinter;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public abstract class ExpressionAbstractTest  {
    protected static final String RELATIVE_MODEL_PATH = "src/test/resources/de/monticore/ocl2smt";
    protected  static  final String RELATIVE_TARGET_PATH = "target/generated/sources/annotationProcessor/java/ocl2smttest";
    protected CDContext cdContext  = new CDContext(buildContext());

    protected ASTOCLCompilationUnit oclAST;
    protected ASTCDCompilationUnit cdAST;
    protected Solver solver;
    protected OCL2SMTGenerator ocl2SMTGenerator ;
    protected CD2SMTGenerator cd2SMTGenerator = new CD2SMTGenerator();

    // Used to make the tests shorter & readable
    protected Identifiable<BoolExpr> addConstraint(String search) {
        Identifiable<BoolExpr> constraint = getConstraint(search);
        solver = cdContext.getContext().mkSolver();
        solver.add(constraint.getValue());
        return constraint;
    }
    protected Identifiable<BoolExpr> getConstraint(String search) {
        ASTOCLConstraint constr = oclAST.getOCLArtifact().getOCLConstraintList()
                .stream().map(p -> (ASTOCLInvariant) p)
                .filter(p -> search.equals(p.getName())).findAny().get();
        return ocl2SMTGenerator.convertConstr(constr);
    }

    protected void parse(String cdFileName, String oclFileName) throws IOException {
        Log.init();
        OCLMill.init();
        CD4CodeMill.init();
        Path cdPath = Path.of(RELATIVE_MODEL_PATH, cdFileName);
        cdAST = OCL_Loader.loadAndCheckCD(
                cdPath.toFile());

        oclAST = OCL_Loader.loadAndCheckOCL(
                Paths.get(RELATIVE_MODEL_PATH, oclFileName).toFile(),
                cdPath.toFile()  );
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


    void testInv(String invName){
        List<Identifiable<BoolExpr>> solverConstraints = new ArrayList<>();
        solverConstraints.add(addConstraint(invName));
        Solver solver = CDContext.makeSolver(cdContext.getContext(),solverConstraints);
        org.junit.jupiter.api.Assertions.assertSame(solver.check(), Status.SATISFIABLE);

        SMT2ODGenerator smt2ODGenerator = new SMT2ODGenerator();
        Optional<ASTODArtifact> od = smt2ODGenerator.buildOdFromSolver(solver,cdContext,invName,false);
        org.junit.jupiter.api.Assertions.assertTrue(od.isPresent());
        printOD(od.get());

    }


   public Context buildContext(){
       Map<String, String> cfg = new HashMap<>();
       cfg.put("model", "true");
       return  new Context(cfg);
   }
}
