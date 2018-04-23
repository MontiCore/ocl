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

package ocl.monticoreocl.ocl._symboltable;


import java.util.Optional;

import de.monticore.ModelingLanguage;
import de.monticore.ast.ASTNode;
import de.monticore.modelloader.ModelingLanguageModelLoader;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.ResolvingConfiguration;
import de.monticore.symboltable.resolving.CommonResolvingFilter;


public class OCLLanguage extends OCLLanguageTOP implements ModelingLanguage {

	public static final String FILE_ENDING = "ocl";

	public OCLLanguage() {
		super("OCL Language", FILE_ENDING);
	}

	@Override
	protected void initResolvingFilters() {
		super.initResolvingFilters();

		addResolvingFilter(new CommonResolvingFilter<OCLFileSymbol>(OCLFileSymbol.KIND));
		addResolvingFilter(new CommonResolvingFilter<OCLInvariantSymbol>(OCLInvariantSymbol.KIND));
		addResolvingFilter(new CommonResolvingFilter<OCLMethodSignatureSymbol>(OCLMethodSignatureSymbol.KIND));
		addResolvingFilter(new CommonResolvingFilter<OCLConstructorSignatureSymbol>(OCLConstructorSignatureSymbol.KIND));
		addResolvingFilter(new CommonResolvingFilter<OCLThrowsClauseSymbol>(OCLThrowsClauseSymbol.KIND));
		addResolvingFilter(new CommonResolvingFilter<OCLParameterDeclarationSymbol>(OCLParameterDeclarationSymbol.KIND));
		addResolvingFilter(new CommonResolvingFilter<OCLVariableDeclarationSymbol>(OCLVariableDeclarationSymbol.KIND));
		addResolvingFilter(new CommonResolvingFilter<OCLMethodDeclarationSymbol>(OCLMethodDeclarationSymbol.KIND));
		addResolvingFilter(new CommonResolvingFilter<OCLPreStatementSymbol>(OCLPreStatementSymbol.KIND));
		addResolvingFilter(new CommonResolvingFilter<OCLPostStatementSymbol>(OCLPostStatementSymbol.KIND));

		setModelNameCalculator(new OCLModelNameCalculator());
	}

	@Override
	protected ModelingLanguageModelLoader<? extends ASTNode> provideModelLoader() {
		return new OCLModelLoader(this);
	}
	
	@Override
	public Optional<OCLSymbolTableCreator> getSymbolTableCreator(ResolvingConfiguration resolvingConfiguration, MutableScope enclosingScope) {
		return Optional.of(new OCLSymbolTableCreator(resolvingConfiguration, enclosingScope));
	}
}