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

public class OCLMethodDeclarationSymbol extends CommonScopeSpanningSymbol {

	public static final OCLMethodDeclarationKind KIND = OCLMethodDeclarationKind.INSTANCE;

	public OCLMethodDeclarationSymbol(String name) {
		super(name, KIND);
	}

	protected String returnType;

	public void setReturnType(String returnType){
		this.returnType = returnType; 

	}

	public String getReturnType(){
		return returnType;
	}

	public Collection<OCLMethodDeclarationSymbol> getOCLMethodDeclaration() {
		return sortSymbolsByPosition(getSpannedScope().resolveLocally(OCLMethodDeclarationSymbol.KIND));
	}
	
	public Optional<OCLParameterDeclarationSymbol> getOCLParamDecl(String name) {
		return getSpannedScope().resolve(name, OCLParameterDeclarationSymbol.KIND);
	}

	public Collection<OCLParameterDeclarationSymbol> getOCLParamDecl() {
		return sortSymbolsByPosition(getSpannedScope().resolveLocally(OCLParameterDeclarationSymbol.KIND));
	}
}
