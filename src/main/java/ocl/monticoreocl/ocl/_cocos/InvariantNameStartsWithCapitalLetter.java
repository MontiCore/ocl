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
import ocl.monticoreocl.ocl._ast.ASTOCLInvariant;

import ocl.monticoreocl.ocl._cocos.OCLASTOCLInvariantCoCo;

public class InvariantNameStartsWithCapitalLetter implements OCLASTOCLInvariantCoCo {

	@Override
	public void check(ASTOCLInvariant astInv){
		if (astInv.nameIsPresent() && Character.isLowerCase(astInv.getName().get().charAt(0))) {
			Log.error(String.format("0xOCL02 invariant name" + " '" + astInv.getName().get() + "' " + "cannot start in lower-case."),
					astInv.get_SourcePositionStart());
		}

	}
}
