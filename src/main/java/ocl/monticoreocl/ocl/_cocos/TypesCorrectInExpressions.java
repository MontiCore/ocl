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

import de.monticore.symboltable.MutableScope;
import ocl.monticoreocl.ocl._ast.ASTOCLInvariant;
import ocl.monticoreocl.ocl._types.OCLTypeCheckingVisitor;

/**
 * Created by Ferdinand Mehlan on 15.01.2018.
 */
public class TypesCorrectInExpressions implements OCLASTOCLInvariantCoCo {

    @Override
    public void check(ASTOCLInvariant astInvariant){
        MutableScope scope = (MutableScope) astInvariant.getSpannedScope().get();
        OCLTypeCheckingVisitor.checkInvariants(astInvariant, scope);
    }

}
