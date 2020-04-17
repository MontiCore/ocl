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

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import de.monticore.symboltable.SymbolKind;

public class OCLModelNameCalculator extends de.monticore.CommonModelNameCalculator {

	@Override
	public Set<String> calculateModelNames(final String name, final SymbolKind kind) {

		final Set<String> calculatedModelNames = new LinkedHashSet<>();

		if (OCLFileSymbol.KIND.isKindOf(kind)) {
			calculatedModelNames.addAll(calculateModelNameForOCLFrame(name));
		}  
		else if (OCLInvariantSymbol.KIND.isKindOf(kind)) {
			calculatedModelNames.addAll(calculateModelNameForOCLInvariant(name));
		}
		else if (OCLPreStatementSymbol.KIND.isKindOf(kind)) {
			calculatedModelNames.addAll(calculateModelNameForOCLPreStatement(name));
		}
		else if (OCLThrowsClauseSymbol.KIND.isKindOf(kind)) {
			calculatedModelNames.addAll(calculateModelNameForOCLThrowsClause(name));
		}
		else if (OCLMethodSignatureSymbol.KIND.isKindOf(kind)) {
			calculatedModelNames.addAll(calculateModelNameForOCLMethodSignature(name));
		}
		else if (OCLConstructorSignatureSymbol.KIND.isKindOf(kind)) {
			calculatedModelNames.addAll(calculateModelNameForOCLConstructorSignature(name));
		}
		else if (OCLMethodDeclarationSymbol.KIND.isKindOf(kind)) {
			calculatedModelNames.addAll(calculateModelNameForOCLMethodDeclaration(name));
		}
		else if (OCLPostStatementSymbol.KIND.isKindOf(kind)) {
			calculatedModelNames.addAll(calculateModelNameForOCLPostStatement(name));
		}
		else if (OCLVariableDeclarationSymbol.KIND.isKindOf(kind)) {
			calculatedModelNames.addAll(calculateModelNameForOCLVariableDeclaration(name));
		}
		else if (OCLParameterDeclarationSymbol.KIND.isKindOf(kind)) {
			calculatedModelNames.addAll(calculateModelNameForOCLParameterDeclaration(name));
		}

		return calculatedModelNames;
	}

	protected Set<String> calculateModelNameForOCLFrame(String name) {
		return ImmutableSet.of(name);
	}
	
	protected Set<String> calculateModelNameForOCLPreStatement(String name) {
		return ImmutableSet.of(name);
	}
	
	protected Set<String> calculateModelNameForOCLThrowsClause(String name) {
		return ImmutableSet.of(name);
	}
	
	protected Set<String> calculateModelNameForOCLInvariant(String name) {
		return ImmutableSet.of(name);
	}
	
	protected Set<String> calculateModelNameForOCLMethodSignature(String name) {
		return ImmutableSet.of(name);
	}
	
	protected Set<String> calculateModelNameForOCLConstructorSignature(String name) {
		return ImmutableSet.of(name);
	}
	
	protected Set<String> calculateModelNameForOCLMethodDeclaration(String name) {
		return ImmutableSet.of(name);
	}
	
	protected Set<String> calculateModelNameForOCLPostStatement(String name) {
		return ImmutableSet.of(name);
	}
	
	protected Set<String> calculateModelNameForOCLVariableDeclaration(String name) {
		return ImmutableSet.of(name);
	}
	
	protected Set<String> calculateModelNameForOCLParameterDeclaration(String name) {
		return ImmutableSet.of(name);
	}
}
