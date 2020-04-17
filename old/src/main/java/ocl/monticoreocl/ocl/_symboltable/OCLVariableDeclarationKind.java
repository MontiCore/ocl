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

import de.monticore.symboltable.SymbolKind;
import de.monticore.symboltable.types.JAttributeSymbolKind;

public class OCLVariableDeclarationKind extends JAttributeSymbolKind {

	private static final String NAME = OCLVariableDeclarationKind.class.getName();

	protected OCLVariableDeclarationKind() {
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean isKindOf(SymbolKind kind) {
		return NAME.equals(kind.getName()) || super.isKindOf(kind);
	}
}
