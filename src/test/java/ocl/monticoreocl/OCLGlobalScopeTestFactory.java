/* (c) https://github.com/MontiCore/monticore */
package ocl.monticoreocl;

import java.nio.file.Paths;

import de.monticore.symboltable.ResolvingConfiguration;
import de.monticore.umlcd4a.CD4AnalysisLanguage;
import ocl.monticoreocl.ocl._symboltable.OCLLanguage;
import de.monticore.ModelingLanguageFamily;
import de.monticore.io.paths.ModelPath;
import de.monticore.symboltable.GlobalScope;


public class OCLGlobalScopeTestFactory {
	public static GlobalScope create(String modelPath) {
		OCLLanguage ocllang = new OCLLanguage();
		CD4AnalysisLanguage cd4AnalysisLang = new CD4AnalysisLanguage();

		ModelingLanguageFamily fam = new ModelingLanguageFamily();
		fam.addModelingLanguage(ocllang);
		fam.addModelingLanguage(cd4AnalysisLang);

		final ModelPath mp = new ModelPath(Paths.get(modelPath));
		GlobalScope scope = new GlobalScope(mp, fam);
		return scope;
	}
}
