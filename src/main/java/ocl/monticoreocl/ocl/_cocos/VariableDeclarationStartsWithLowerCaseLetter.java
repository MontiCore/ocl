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
package ocl.monticoreocl.ocl._cocos;

import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._ast.ASTOCLVariableDeclaration;
import ocl.monticoreocl.ocl._cocos.OCLASTOCLVariableDeclarationCoCo;

public class VariableDeclarationStartsWithLowerCaseLetter implements OCLASTOCLVariableDeclarationCoCo {

	@Override 
	public void check(ASTOCLVariableDeclaration astVariableDeclaration){
		String varName = astVariableDeclaration.getName();
		boolean startsWithUpperCase = Character.isUpperCase(varName.charAt(0));

		if (startsWithUpperCase) {
			Log.error(
					String.format("0xOCL09 variable declaration name '%s' must start with a lower-case letter.", varName),
					astVariableDeclaration.get_SourcePositionStart());
		}
	}
}