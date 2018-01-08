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

import de.monticore.symboltable.types.CommonJFieldSymbol;
import de.monticore.umlcd4a.symboltable.references.CDTypeSymbolReference;

public class OCLVariableDeclarationSymbol extends CommonJFieldSymbol<CDTypeSymbolReference> {

	public static final OCLVariableDeclarationKind KIND = new OCLVariableDeclarationKind();

	protected String varName;
	protected CDTypeSymbolReference typeReference;
	protected String varTypeName;


	public OCLVariableDeclarationSymbol(String varName, CDTypeSymbolReference typeReference) {
		super(varName, KIND, typeReference);
		this.varName = varName;
		this.typeReference = typeReference;
		this.varTypeName = "";
		if (typeReference!=null) {
			this.varTypeName = typeReference.getName();
		}
	}

	public String getExtendedName() {
		return "OCL field " + getName();
	}

	@Override
	public String toString() {
		return  OCLVariableDeclarationSymbol.class.getSimpleName() + " " + getName();
	}

	public void setName(String varName){
		this.varName = varName;
	}

	@Override
	public String getName(){
		return varName;
	}

	@Override
	public void setType(CDTypeSymbolReference typeReference){
		this.typeReference = typeReference;
	}

	@Override
	public CDTypeSymbolReference getType(){
		return typeReference;
	}

	public String getVarTypeName(){
		return varTypeName;
	}
}
