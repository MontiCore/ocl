package de.monticore.ocl2smt;

import com.microsoft.z3.BoolExpr;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.odbasis._ast.ASTODArtifact;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetExpressionsTest  extends ExpressionAbstractTest{
    @BeforeEach
    public void setup() throws IOException {
        parse( "setExpressions/Set.cd","setExpressions/Set.ocl");
    }
    void testInv(String invName){
        Set<ASTOCLCompilationUnit> oclFiles = new HashSet<>();
        oclFiles.add(oclAST);
        List<Pair<String, BoolExpr>> constraintList = OCLDiffGenerator.getPositiveSolverConstraints(cdAST,oclFiles,buildContext());
        ASTODArtifact od = OCLDiffGenerator.buildOd(OCLDiffGenerator.cdContext, invName, constraintList, cdAST.getCDDefinition());
        printOD(od);
    }
    @Test
    public void of_legal_age() {
        testInv("Parent_child");
    }

}
