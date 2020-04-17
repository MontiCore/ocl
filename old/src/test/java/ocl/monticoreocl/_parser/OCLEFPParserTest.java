/*
 *  * (c) https://github.com/MontiCore/monticore
 *  *
 *  * The license generally applicable for this project
 *  * can be found under https://github.com/MontiCore/monticore.
 */

/**
 *
 * (c) https://github.com/MontiCore/monticore
 *
 * The license generally applicable for this project
 * can be found under https://github.com/MontiCore/monticore.
 */
/**
 *
 * /* (c) https://github.com/MontiCore/monticore */
 *
 * The license generally applicable for this project
 * can be found under https://github.com/MontiCore/monticore.
 */
/* (c) https://github.com/MontiCore/monticore */
package ocl.monticoreocl._parser;

 import ocl.monticoreocl.ocl._ast.ASTCompilationUnit;
import ocl.monticoreocl.ocl._parser.OCLParser;
import org.antlr.v4.runtime.RecognitionException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class OCLEFPParserTest {

    private void test(Path model) throws RecognitionException, IOException {
        OCLParser parser = new OCLParser();
        Optional<ASTCompilationUnit> cdDef = parser.parse(model.toString());
        assertFalse(parser.hasErrors());
        assertTrue(cdDef.isPresent());
    }

    @Test
    public void ruleInstTraceTest() throws RecognitionException, IOException {
        Path model = Paths.get("src/test/resources/example/validEFPConstraints/ruleInstTrace.ocl");
        test(model);
    }

    @Test
    public void ruleInstEncryptionTest() throws RecognitionException, IOException {
        Path model = Paths.get("src/test/resources/example/validEFPConstraints/ruleInstEncryption.ocl");
        test(model);
    }

    @Test
    public void ruleInstAuthenticationTest() throws RecognitionException, IOException {
        Path model = Paths.get("src/test/resources/example/validEFPConstraints/ruleInstAuthentication.ocl");
        test(model);
    }

    @Test
    public void ruleInstCertificatesTest() throws RecognitionException, IOException {
        Path model = Paths.get("src/test/resources/example/validEFPConstraints/ruleInstCertificates.ocl");
        test(model);
    }

    @Ignore
    @Test
    public void ruleCompEncryptionTest() throws RecognitionException, IOException {
        Path model = Paths.get("src/test/resources/example/validEFPConstraints/ruleCompEncryption.ocl");
        test(model);
    }

    @Test
    public void rulePortEnergyTest() throws RecognitionException, IOException {
        Path model = Paths.get("src/test/resources/example/validEFPConstraints/rulePortEnergy.ocl");
        test(model);
    }

    @Test
    public void ruleWCETSingleCoreTest() throws RecognitionException, IOException {
        Path model = Paths.get("src/test/resources/example/validEFPConstraints/ruleWCETSingleCore.ocl");
        test(model);
    }
}
