package de.monticore.ocl2smt;



import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.odbasis._ast.ASTODArtifact;
import org.gradle.internal.impldep.org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public class AssociationTest extends ExpressionAbstractTest {
    @BeforeEach
    public void setup() throws IOException {
        parse( "/associations/Association.cd","/associations/Association.ocl");
        cdContext = cd2SMTGenerator.cd2smt(cdAST,(buildContext()));
        ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);
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
    @Ignore
    @Test
    public void transitive_closure() throws  IOException{
        parse( "/Transitive-closure/transitiveClosure.cd","/Transitive-closure/transitiveClosure.ocl");
        cdContext = cd2SMTGenerator.cd2smt(cdAST,(buildContext()));
        ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);
        Set<ASTOCLCompilationUnit> ocls = new HashSet<>();
        ocls.add(oclAST);
        ASTODArtifact od =  OCLDiffGenerator.oclWitness(cdAST,ocls,cdContext.getContext(),false);
        printOD(od);
    }
}
