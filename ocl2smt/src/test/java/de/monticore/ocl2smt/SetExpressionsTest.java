package de.monticore.ocl2smt;

import com.microsoft.z3.BoolExpr;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.odbasis._ast.ASTODArtifact;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SetExpressionsTest  extends ExpressionAbstractTest{
    @BeforeEach
    public void setup() throws IOException {
        parse( "setExpressions/Set.cd","setExpressions/Set.ocl");
    }
    void testInv(String invName){
        Set<ASTOCLCompilationUnit> oclFiles = new HashSet<>();
        oclFiles.add(oclAST);
        List<Pair<String, BoolExpr>> constraintList = OCLDiffGenerator.getPositiveSolverConstraints(cdAST,oclFiles,buildContext());
        List<Pair<String, BoolExpr>> actualConstraint = constraintList.stream().filter(p-> p.getLeft().equals(invName)).collect(Collectors.toList());
        Assertions.assertTrue(actualConstraint.size() > 0);
        ASTODArtifact od = OCLDiffGenerator.buildOd(OCLDiffGenerator.cdContext, invName, actualConstraint, cdAST.getCDDefinition());
        printOD(od);
    }
    @Test
    public void test_isin_set() {
        testInv("All_Person_in_All_Auctions");
    }

    @Test
    public void test_notin_set() {
        testInv("One_Person_in_Any_Auctions");
    }

}
