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

public class OCLInvariantSymbol extends CommonScopeSpanningSymbol {

	public static final OCLInvariantKind KIND = OCLInvariantKind.INSTANCE;

	protected String className;
	protected String classObject;
	protected boolean context = false;
	protected boolean importing = false;

	public OCLInvariantSymbol(String name) {
		super(name, KIND);
	}

	public void setContext(boolean context){
		this.context = context;
	}

	public void setImport(boolean importing){
		this.importing = importing;

	}

	public boolean getContext(){
		return context;
	}

	public boolean getImport(){
		return importing;
	}

	public void setClassN(String className){
		this.className = className;
	}

	public String getClassN(){
		return className;
	}

	public void setClassO(String classObject){
		this.classObject=classObject;

	}

	public String getClassO(){
		return classObject;
	}

	public Collection<OCLVariableDeclarationSymbol> getOCLVariableDecl() {
		return getSpannedScope().resolveLocally(OCLVariableDeclarationSymbol.KIND);
	}

	public Optional<OCLVariableDeclarationSymbol> getOCLVariableDecl(String name) {
		return getSpannedScope().resolve(name, OCLVariableDeclarationSymbol.KIND);
	}


	public Collection<OCLMethodDeclarationSymbol> getOCLMethodDecl() {
		return getSpannedScope().resolveLocally(OCLMethodDeclarationSymbol.KIND);
	}

	public Optional<OCLMethodDeclarationSymbol> getOCLMethodDecl(String name) {
		return getSpannedScope().resolve(name, OCLMethodDeclarationSymbol.KIND);
	}

	public Collection<OCLParameterDeclarationSymbol> getOCLParamDecl() {
		return sortSymbolsByPosition(getSpannedScope().resolveLocally(OCLParameterDeclarationSymbol.KIND));
	}

	public Optional<OCLParameterDeclarationSymbol> getOCLParamDecl(String name) {
		return getSpannedScope().resolve(name, OCLParameterDeclarationSymbol.KIND);
	} 
}
