package de.monticore.ocl2smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Solver;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.context.CDContext;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl2smt.AbstractTest;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLConstraint;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

public abstract class ExpressionAbstractTest extends AbstractTest {
    protected static final String RELATIVE_MODEL_PATH = "src/ocl2smttest/resources/de/monticore/ocl2smt";
    protected CDContext cdContext;

    protected ASTOCLCompilationUnit oclAST;
    protected ASTCDCompilationUnit cdAST;
    protected Solver solver;
    protected OCL2SMTGenerator ocl2SMTGenerator;

    protected CD2SMTGenerator cd2SMTGenerator = new CD2SMTGenerator();

    // Used to make the tests shorter & readable
    protected BoolExpr addConstraint(String search) {
        ASTOCLConstraint constr = oclAST.getOCLArtifact().getOCLConstraintList()
                .stream().map(p -> (ASTOCLInvariant) p)
                .filter(p -> search.equals(p.getName())).findAny().get();
        Pair<Optional<String>,BoolExpr> constraint = ocl2SMTGenerator.convertConstr(constr);
        solver.add(constraint.getRight());
        return constraint.getRight();
    }

    protected void parse(String cdFileName, String oclFileName) throws IOException {
        Log.init();
        OCLMill.init();
        CD4CodeMill.init();
        cdAST = OCL_Loader.loadAndCheckCD(
                Paths.get(RELATIVE_MODEL_PATH,cdFileName ).toFile());

        oclAST = OCL_Loader.loadAndCheckOCL(
                Paths.get(RELATIVE_MODEL_PATH, oclFileName).toFile(),
                cdAST);
    }
}
