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
package ocl.monticoreocl._cocos;

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
