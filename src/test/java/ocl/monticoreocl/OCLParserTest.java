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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import de.se_rwth.commons.logging.Log;
import org.antlr.v4.runtime.RecognitionException;
import org.junit.Ignore;
import org.junit.Test;

import ocl.monticoreocl.ocl._ast.ASTCompilationUnit;
import ocl.monticoreocl.ocl._parser.OCLParser;

public class OCLParserTest {


	private void test(Path model) throws RecognitionException, IOException {
		Log.debug("Parsing model: " + model.toString(), "OCLParserTest");
		OCLParser parser = new OCLParser();
		Optional<ASTCompilationUnit> cdDef = parser.parse(model.toString());
		assertFalse(parser.hasErrors());
		assertTrue(cdDef.isPresent());
		//Log.debug(cdDef.toString(), "OCLParserTest");
	}

	@Test
	public void association1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/association1.ocl");
		test(model);
	}

	@Test
	public void association2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/association2.ocl");
		test(model);
	}

	@Test
	public void cases1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/cases1.ocl");
		test(model);
	}

	@Test
	public void cases2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/cases2.ocl");
		test(model);
	}

	@Test
	public void comprehension1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/comprehension1.ocl");
		test(model);
	}

	@Test
	public void comprehension2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/comprehension2.ocl");
		test(model);
	}

	@Test
	public void comprehension3Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/comprehension3.ocl");
		test(model);
	}

	@Test
	public void comprehension4Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/comprehension4.ocl");
		test(model);
	}

	@Test
	public void comprehension5Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/comprehension5.ocl");
		test(model);
	}

	@Test
	public void comprehension6Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/comprehension6.ocl");
		test(model);	}

	@Test
	public void comprehension7Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/comprehension7.ocl");
		test(model);	}

	@Test
	public void comprehension8Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/comprehension8.ocl");
		test(model);	}

	@Test
	public void comprehension9Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/comprehension9.ocl");
		test(model);	}

	@Test
	public void comprehension10Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/comprehension10.ocl");
		test(model);	}

	@Test
	public void comprehension11Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/comprehension11.ocl");
		test(model);	}

	@Test
	public void comprehension12Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/comprehension12.ocl");
		test(model);	}

	@Test
	public void comprehension13Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/comprehension13.ocl");
		test(model);	}

	@Test
	public void context1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/context1.ocl");
		test(model);
	}

	@Test
	public void context2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/context2.ocl");
		test(model);
	}

	@Test
	public void context3Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/context3.ocl");
		test(model);
	}

	@Test
	public void context4Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/context4.ocl");
		test(model);
	}

	@Test
	public void context5Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/context5.ocl");
		test(model);
	}

	@Test
	public void context6Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/context6.ocl");
		test(model);
	}

	@Test
	public void context7Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/context7.ocl");
		test(model);
	}

	@Test
	public void context8Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/context8.ocl");
		test(model);
	}

	@Test
	public void context9Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/context9.ocl");
		test(model);
	}

	@Test
	public void container1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/container1.ocl");
		test(model);
	}

	@Test
	public void container2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/container2.ocl");
		test(model);
	}

	@Test
	public void container3Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/container3.ocl");
		test(model);
	}

	@Test
	public void let1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/let1.ocl");
		test(model);
	}

	@Test
	public void let2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/let2.ocl");
		test(model);
	}

	@Test
	public void let3Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/let3.ocl");
		test(model);
	}

	@Test
	public void let4Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/let4.ocl");
		test(model);
	}


	@Test
	public void let5Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/let5.ocl");
		test(model);
	}

	@Test
	public void numbers1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/numbers1.ocl");
		test(model);
	}

	@Test
	public void numbers2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/numbers2.ocl");
		test(model);
	}

	@Test
	public void numbers3Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/numbers3.ocl");
		test(model);
	}

	@Test
	public void numbers4Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/numbers4.ocl");
		test(model);
	}

	@Test
	public void numbers5Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/numbers5.ocl");
		test(model);
	}

	@Test
	public void numbers6Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/numbers6.ocl");
		test(model);
	}

	@Test
	public void prePost1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/prepost1.ocl");
		test(model);
	}

	@Test
	public void prePost2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/prepost2.ocl");
		test(model);
	}

	@Test
	public void prePost3Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/prepost3.ocl");
		test(model);
	}

	@Test
	public void prePost5Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/prepost5.ocl");
		test(model);
	}

	@Test
	public void prePost4Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/prepost4.ocl");
		test(model);
	}

	@Test
	public void prePost6Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/prepost6.ocl");
		test(model);
	}

	@Test
	public void prePost7Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/prepost7.ocl");
		test(model);
	}

	@Test
	public void prePost8Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/prepost8.ocl");
		test(model);
	}

	@Test
	public void prePost9Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/prepost9.ocl");
		test(model);
	}

	@Test
	public void prePost10Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/prepost10.ocl");
		test(model);
	}

	@Test
	public void prePost11Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/prepost11.ocl");
		test(model);
	}

	@Test
	public void prePost12Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/prepost12.ocl");
		test(model);
	}

	@Test
	public void prePost13Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/prepost13.ocl");
		test(model);
	}

	@Test
	public void quantifiers1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/quantifiers1.ocl");
		test(model);
	}

	@Test
	public void quantifiers2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/quantifiers2.ocl");
		test(model);
	}

	@Test
	public void quantifiers3Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/quantifiers3.ocl");
		test(model);
	}

	@Test
	public void quantifiers4Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/quantifiers4.ocl");
		test(model);
	}

	@Test
	public void quantifiers5Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/quantifiers5.ocl");
		test(model);
	}

	@Test
	public void quantifiers6Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/quantifiers6.ocl");
		test(model);
	}

	@Test
	public void quantifiers7Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/quantifiers7.ocl");
		test(model);
	}

	@Test
	public void quantifiers8Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/quantifiers8.ocl");
		test(model);
	}

	@Test
	public void quantifiers9Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/quantifiers9.ocl");
		test(model);
	}

	@Test
	public void quantifiers10Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/quantifiers10.ocl");
		test(model);
	}

	@Test
	public void quantifiers11Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/quantifiers11.ocl");
		test(model);
	}

	@Test
	public void quantifiers12Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/quantifiers12.ocl");
		test(model);
	}

	@Test
	public void quantifiers13Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/quantifiers13.ocl");
		test(model);
	}

	@Test
	public void quantifiers14Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/quantifiers14.ocl");
		test(model);
	}

	@Test
	public void query1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/query1.ocl");
		test(model);
	}

	@Test
	public void query2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/query2.ocl");
		test(model);
	}

	@Test
	public void setOperations1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations1.ocl");
		test(model);
	}

	@Test
	public void setOperations2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations2.ocl");
		test(model);
	}

	@Test
	public void setOperations3Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations3.ocl");
		test(model);
	}

	@Test
	public void setOperations4Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations4.ocl");
		test(model);
	}

	@Test
	public void setOperations5Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations5.ocl");
		test(model);
	}

	@Test
	public void setOperations6Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations6.ocl");
		test(model);
	}

	@Test
	public void setOperations7Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations7.ocl");
		test(model);
	}

	@Test
	public void setOperations8Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations8.ocl");
		test(model);
	}

	@Test
	public void setOperations9Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations9.ocl");
		test(model);
	}

	@Test
	public void setOperations10Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations10.ocl");
		test(model);
	}

	@Test
	public void setOperations11Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations11.ocl");
		test(model);
	}

	@Test
	public void setOperations12Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations12.ocl");
		test(model);
	}

	@Test
	public void setOperations13Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations13.ocl");
		test(model);
	}

	@Test
	public void setOperations14Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations14.ocl");
		test(model);
	}

	@Test
	public void setOperations15Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations15.ocl");
		test(model);
	}

	@Test
	public void setOperations16Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/setoperations16.ocl");
		test(model);
	}

	@Test
	public void sor1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/sor1.ocl");
		test(model);
	}

	@Test
	public void sor2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/sor2.ocl");
		test(model);
	}

	@Test
	public void sor3Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/sor3.ocl");
		test(model);
	}

	@Test
	public void sor4Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/sor4.ocl");
		test(model);
	}

	@Test
	public void sor5Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/sor5.ocl");
		test(model);
	}

	@Test
	public void sor6Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/sor6.ocl");
		test(model);
	}

	@Test
	public void sor7Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/sor7.ocl");
		test(model);
	}

	@Test
	public void special1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/special1.ocl");
		test(model);
	}

	@Test
	public void special2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/special2.ocl");
		test(model);
	}

	@Test
	public void special3Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/special3.ocl");
		test(model);
	}

	@Test
	public void special4Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/special4.ocl");
		test(model);
	}

	@Test
	public void special5Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/special5.ocl");
		test(model);
	}

	@Ignore
	@Test
	public void taggedValues1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/taggedvalues1.ocl");
		test(model);
	}

	@Ignore
	@Test
	public void taggedValues2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/taggedvalues2.ocl");
		test(model);
	}

	@Test
	public void transitiveClosure1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/transitiveclosure1.ocl");
		test(model);
	}

	@Test
	public void transitiveClosure2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/transitiveclosure2.ocl");
		test(model);
	}

	@Test
	public void transitiveClosure3Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/transitiveclosure3.ocl");
		test(model);
	}

	@Test
	public void transitiveClosure4Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/transitiveclosure4.ocl");
		test(model);
	}

	@Test
	public void unit1Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/unit1.ocl");
		test(model);
	}

	@Test
	public void unit2Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/unit2.ocl");
		test(model);
	}

	@Test
	public void unit3Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/unit3.ocl");
		test(model);
	}

	@Test
	public void unit4Test() throws RecognitionException, IOException {
		Path model = Paths.get("src/test/resources/example/validGrammarModels/unit4.ocl");
		test(model);
	}
}
