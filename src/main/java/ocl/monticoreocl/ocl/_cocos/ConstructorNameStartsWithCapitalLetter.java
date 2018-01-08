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


import ocl.monticoreocl.ocl._ast.ASTOCLConstructorSignature;
import ocl.monticoreocl.ocl._cocos.OCLASTOCLConstructorSignatureCoCo;

public class ConstructorNameStartsWithCapitalLetter implements OCLASTOCLConstructorSignatureCoCo {

	@Override
	public void check(ASTOCLConstructorSignature astConstructorSig){
		if (Character.isLowerCase(astConstructorSig.getReferenceType().charAt(0))) {
			Log.error(String.format("0xOCL10 constructor name '%s' after keyword 'new' cannot start in lower-case.", astConstructorSig.getReferenceType()),
					astConstructorSig.get_SourcePositionStart());
		}
	}
}