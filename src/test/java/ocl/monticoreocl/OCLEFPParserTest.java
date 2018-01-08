/**
 * ******************************************************************************
 *  MontiCAR Modeling Family, www.se-rwth.de
 *  Copyright (c) 2017, Software Engineering Group at RWTH Aachen,
 *  All rights reserved.
 *
 *  This project is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * *******************************************************************************
 */
package ocl.monticoreocl;

import de.se_rwth.commons.logging.Log;
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
