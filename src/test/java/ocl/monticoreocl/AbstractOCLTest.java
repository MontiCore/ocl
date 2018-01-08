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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

import de.monticore.umlcd4a.CD4AnalysisLanguage;
import ocl.LogConfig;
import org.antlr.v4.runtime.RecognitionException;

import de.monticore.ModelingLanguageFamily;
import de.monticore.cocos.helper.Assert;
import de.monticore.io.paths.ModelPath;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.ResolvingConfiguration;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._ast.ASTCompilationUnit;
import ocl.monticoreocl.ocl._cocos.OCLCoCoChecker;
import ocl.monticoreocl.ocl._symboltable.OCLLanguage;
import ocl.monticoreocl.ocl._symboltable.OCLSymbolTableCreator;

public abstract class AbstractOCLTest {

	private final OCLLanguage ocllang = new OCLLanguage();
	private final  CD4AnalysisLanguage cd4AnalysisLang = new CD4AnalysisLanguage();
	protected static String PARENT_DIR = "src/test/resources/";


	protected Scope cdScope;

	public AbstractOCLTest() {
	}

	abstract protected OCLCoCoChecker getChecker();

	protected void testModelForErrors(String parentDirectory, String model, Collection<Finding> expectedErrors) {
		OCLCoCoChecker checker = getChecker();

		ASTCompilationUnit root = loadModel(parentDirectory, model);
		checker.checkAll(root);
		Assert.assertEqualErrorCounts(expectedErrors, Log.getFindings());
		Assert.assertErrorMsg(expectedErrors, Log.getFindings());
	}

	protected void testModelNoErrors(String parentDirectory, String model) {
		OCLCoCoChecker checker = getChecker();
		ASTCompilationUnit root = loadModel(parentDirectory, model);
		checker.checkAll(root);
		assertEquals(0, Log.getFindings().stream().filter(f -> f.isError()).count());
	}

	protected ASTCompilationUnit loadModel(String parentDirectory, String modelFullQualifiedFilename) {

		LogConfig.init();
		try {
			ModelPath modelPath = new ModelPath(Paths.get(parentDirectory));
			ModelingLanguageFamily modelingLanguageFamily = new ModelingLanguageFamily();
			modelingLanguageFamily.addModelingLanguage(ocllang);
			modelingLanguageFamily.addModelingLanguage(cd4AnalysisLang);
			GlobalScope globalScope = new GlobalScope(modelPath, modelingLanguageFamily);

			ResolvingConfiguration resolvingConfiguration = new ResolvingConfiguration();
			resolvingConfiguration.addDefaultFilters(ocllang.getResolvers());
			resolvingConfiguration.addDefaultFilters(cd4AnalysisLang.getResolvers());

			OCLSymbolTableCreator oclSymbolTableCreator = ocllang.getSymbolTableCreator(resolvingConfiguration, globalScope).get();
			Optional<ASTCompilationUnit> astOCLCompilationUnit = ocllang.getModelLoader().loadModel(modelFullQualifiedFilename, modelPath);

			if(astOCLCompilationUnit.isPresent()) {
				astOCLCompilationUnit.get().accept(oclSymbolTableCreator);
				cdScope = globalScope.getSubScopes().get(0).getSubScopes().get(0);
				return astOCLCompilationUnit.get();
			}
		} catch (RecognitionException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Error during loading of model " + modelFullQualifiedFilename + ".");
	}
}
