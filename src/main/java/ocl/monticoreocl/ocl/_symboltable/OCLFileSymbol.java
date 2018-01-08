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
