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
package ocl.monticoreocl.ocl._visitors;

import de.monticore.expressionsbasis._ast.ASTExpression;
import de.monticore.oclexpressions._ast.ASTOCLComprehensionPrimary;
import de.monticore.oclexpressions._ast.ASTOCLQualifiedPrimary;
import de.monticore.oclexpressions._ast.ASTParenthizedExpression;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Scope;
import de.monticore.umlcd4a.symboltable.references.CDTypeSymbolReference;
import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._ast.*;
import ocl.monticoreocl.ocl._visitor.OCLVisitor;


public class OCLTypeCheckingVisitor implements OCLVisitor{

    private boolean isTypeCorrect;
    private OCLVisitor realThis = this;
    private MutableScope scope;

    public OCLTypeCheckingVisitor(MutableScope scope) {
        this.isTypeCorrect = true;
        this.scope = scope;
    }

    public boolean isTypeCorrect() {
        return isTypeCorrect;
    }

    public static void checkInvariants(ASTOCLInvariant node, MutableScope scope) {
        OCLTypeCheckingVisitor checkingVisitor = new OCLTypeCheckingVisitor(scope);

        for(ASTExpression expr : node.getStatements()){
            expr.accept(checkingVisitor);
            if(!checkingVisitor.isTypeCorrect()) {
                Log.warn("0xOCLI0 Could not infer type from this expression:" + expr.get_SourcePositionStart());
            }
        }
    }

    @Override
    public void traverse(ASTOCLComprehensionPrimary node){
        Log.warn("Todo: implement type checking for comprehensions.");
    }

    @Override
    public void traverse(ASTParenthizedExpression node){
        OCLExpressionTypeInferingVisitor.getTypeFromExpression(node, scope);
    }

    @Override
    public void traverse(ASTOCLQualifiedPrimary node){
        OCLExpressionTypeInferingVisitor.getTypeFromExpression(node, scope);
    }
}
