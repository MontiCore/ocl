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
package ocl.monticoreocl.ocl._types;

import de.monticore.symboltable.MutableScope;
import de.monticore.types.types._ast.ASTType;
import de.monticore.umlcd4a.symboltable.references.CDTypeSymbolReference;
import ocl.monticoreocl.ocl._ast.ASTOCLNode;
import ocl.monticoreocl.ocl._visitor.OCLVisitor;
import ocl.monticoreocl.oclexpressions._ast.ASTInExpr;
import ocl.monticoreocl.oclexpressions._ast.ASTName2;
import ocl.monticoreocl.oclexpressions._ast.ASTOCLExpressionsNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ocl.monticoreocl.ocl._types.TypeInferringHelper.flattenOnce;

public class FindInExpressionHelper implements OCLVisitor {

  MutableScope scope;
  Map<String, CDTypeSymbolReference> inTypes = new LinkedHashMap<>();

  public FindInExpressionHelper(ASTOCLExpressionsNode node, MutableScope scope) {
    this.scope = scope;
    node.accept(this);
  }

  public void endVisit(final ASTInExpr astInExpr) {
    if (astInExpr.isPresentExpression()) {
      List<String> varNames = astInExpr.getVarNameList();
      OCLExpressionTypeInferingVisitor typeVisitor = new OCLExpressionTypeInferingVisitor(scope, false);
      CDTypeSymbolReference type = typeVisitor.getTypeFromExpression(astInExpr.getExpression());
      CDTypeSymbolReference type2 = flattenOnce(type);
      varNames.stream().forEach(s -> inTypes.put(s, type2));
    }
  }

  public Map<String, CDTypeSymbolReference> getInTypes() { return inTypes; }
}
