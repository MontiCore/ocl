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
package ocl.monticoreocl.ocl._ast;

import de.monticore.types.types._ast.ASTReturnType;
import ocl.monticoreocl.oclexpressions._ast.ASTOCLQualifiedPrimary;

import java.util.Optional;

public class ASTOCLMethodDeclaration extends ASTOCLMethodDeclarationTOP {

  public ASTOCLMethodDeclaration() {
    super();
  }

  protected  ASTOCLMethodDeclaration (/* generated by template ast.ConstructorParametersDeclaration*/
      Optional<ASTReturnType> returnType
      ,
      ocl.monticoreocl.oclexpressions._ast.ASTName2 name2
      ,
      ocl.monticoreocl.ocl._ast.ASTOCLParameters oCLParameters
      ,
      de.monticore.expressionsbasis._ast.ASTExpression expression

  ) {
    super(returnType, name2, oCLParameters, expression);
  }

  public String getName() {
    return ASTOCLQualifiedPrimary.name2String(getName2());
  }
}
