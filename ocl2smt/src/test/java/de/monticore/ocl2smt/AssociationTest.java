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

public class AssociationTest extends ExpressionAbstractTest {
    @BeforeEach
    public void setup() throws IOException {
        parse( "/associations/Auction.cd","/associations/Association.ocl");
    }

   ASTODArtifact testInv(String invName){
       Set<ASTOCLCompilationUnit> oclFiles = new HashSet<>();
       oclFiles.add(oclAST);
        List<Pair<String,BoolExpr>> constraintList = OCLDiffGenerator.getPositiveSolverConstraints(cdAST,oclFiles,buildContext());
        ASTODArtifact od = OCLDiffGenerator.buildOd(OCLDiffGenerator.cdContext, invName, constraintList, cdAST.getCDDefinition());
        printOD(od);
        return od;
    }
    @Test
    public void of_legal_age() {
      testInv("Of_legal_age");
    }
    @Test
    public void different_ids() {
        testInv("Diff_ids");
    }
    @Test
    public void atLeast2Person(){ testInv("AtLeast_2_Person");}
    @Test
    public void Same_Person_in_2_Auction(){ testInv("Same_Person_in_2_Auction");}
}
