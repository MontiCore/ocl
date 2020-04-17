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
