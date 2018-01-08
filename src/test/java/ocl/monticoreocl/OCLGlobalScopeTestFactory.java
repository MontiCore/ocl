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
