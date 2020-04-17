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

import static de.monticore.symboltable.Symbols.sortSymbolsByPosition;

import java.util.Collection;
import java.util.Optional;
import de.monticore.symboltable.CommonScopeSpanningSymbol;

public class OCLFileSymbol extends CommonScopeSpanningSymbol {

	public static final OCLFileSymbolKind KIND = OCLFileSymbolKind.INSTANCE;

	public OCLFileSymbol(String name) {
		super(name, KIND);
	}

	public Collection<OCLMethodSignatureSymbol> getOCLMethSig() {
		return getSpannedScope().resolveLocally(OCLMethodSignatureSymbol.KIND);
	}

	public Optional<OCLMethodSignatureSymbol> getOCLMethSig(String name) {
		return getSpannedScope().resolveLocally(name, OCLMethodSignatureSymbol.KIND);
	}

	public Collection<OCLConstructorSignatureSymbol> getOCLConstructorSig() {
		return getSpannedScope().resolveLocally(OCLConstructorSignatureSymbol.KIND);
	}

	public Optional<OCLConstructorSignatureSymbol> getOCLConstructorSig(String name) {
		return getSpannedScope().resolve(name, OCLConstructorSignatureSymbol.KIND);
	}

	public Optional<OCLVariableDeclarationSymbol> getOCLVariableDecl(String name) {
		return getSpannedScope().resolve(name, OCLVariableDeclarationSymbol.KIND);
	}

	public Collection<OCLVariableDeclarationSymbol> getOCLVariableDecl() {
		return sortSymbolsByPosition(getSpannedScope().resolveLocally(OCLVariableDeclarationSymbol.KIND));
	}

	public Collection<OCLInvariantSymbol> getOCLInvariant() {
		return getSpannedScope().resolveLocally(OCLInvariantSymbol.KIND);
	}

	public Optional<OCLInvariantSymbol> getOCLInvariant(String name) {
		return getSpannedScope().resolveLocally(name, OCLInvariantSymbol.KIND);
	}

	public Collection<OCLPreStatementSymbol> getOCLPreStatement() {
		return getSpannedScope().resolveLocally(OCLPreStatementSymbol.KIND);
	}

	public Optional<OCLPreStatementSymbol> getOCLPreStatement(String name) {
		return getSpannedScope().resolveLocally(name, OCLPreStatementSymbol.KIND);
	}
}
