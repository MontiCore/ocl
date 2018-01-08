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
