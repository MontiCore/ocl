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

import static org.junit.Assert.assertEquals;

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
			resolvingConfiguration.addDefaultFilters(ocllang.getResolvingFilters());
			resolvingConfiguration.addDefaultFilters(cd4AnalysisLang.getResolvingFilters());

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
