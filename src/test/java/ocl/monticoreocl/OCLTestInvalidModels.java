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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.antlr.v4.runtime.RecognitionException;
import org.junit.Test;

import ocl.monticoreocl.ocl._ast.ASTCompilationUnit;
import ocl.monticoreocl.ocl._ast.ASTOCLInvariant;
import ocl.monticoreocl.ocl._parser.OCLParser;

public class OCLTestInvalidModels {

	@Test
	public void testAssociation1() throws RecognitionException, IOException {
		//TODO: missing file?
		/*
		Path model = Paths.get("src/test/resources/example/invalidGrammarModels/association1.ocl");

		OCLParser parser = new OCLParser();
		Optional<ASTCompilationUnit> compilation = parser.parse(model.toString());
		assertFalse(parser.hasErrors());
		assertTrue(compilation.isPresent());

		assertNotSame("ocl", compilation.get().getOCLFile().getPrefix());
		assertNotSame("association1", compilation.get().getOCLFile().getFileName());

		Path model2 = Paths.get("src/test/resources/example/invalidGrammarModels/association1.ocl");
		OCLParser invParser = new OCLParser();
		Optional<ASTOCLInvariant> inv = invParser.parseOCLInvariant(model2.toString());
		assertFalse(invParser.hasErrors());
		assertTrue(inv.isPresent());
	*/
	}

}
