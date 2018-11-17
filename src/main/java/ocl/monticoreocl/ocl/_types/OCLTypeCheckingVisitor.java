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

import de.monticore.commonexpressions._ast.*;
import de.monticore.expressionsbasis._ast.ASTExpression;
import de.monticore.numberunit.prettyprint.UnitsPrinter;
import de.monticore.oclexpressions._ast.ASTExistsExpr;
import de.monticore.oclexpressions._ast.ASTForallExpr;
import de.monticore.oclexpressions._ast.ASTOCLQualifiedPrimary;
import de.monticore.oclexpressions._ast.ASTParenthizedExpression;
import de.monticore.symboltable.MutableScope;
import de.monticore.umlcd4a.symboltable.references.CDTypeSymbolReference;
import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._ast.*;
import ocl.monticoreocl.ocl._visitor.OCLVisitor;

import javax.measure.unit.Unit;
import java.util.Optional;


public class OCLTypeCheckingVisitor implements OCLVisitor {

    private MutableScope scope;

    public OCLTypeCheckingVisitor(MutableScope scope) {
        this.scope = scope;
    }

    public static void checkInvariants(ASTOCLInvariant node, MutableScope scope) {
        OCLTypeCheckingVisitor checkingVisitor = new OCLTypeCheckingVisitor(scope);

        for(ASTExpression expr : node.getStatementsList()){
            checkingVisitor.checkPrefixExpr(expr);
            expr.accept(checkingVisitor);
        }
    }

    @Override
    public void visit(ASTExistsExpr node) {
        checkPrefixExpr(node.getExpression());
    }
    @Override
    public void visit(ASTForallExpr node) {
        checkPrefixExpr(node.getExpression());
    }


    /**
     *  ********** math expressions **********
     */

    public void checkInfixExpr(ASTInfixExpression node){
        OCLExpressionTypeInferingVisitor leftVisitor = new OCLExpressionTypeInferingVisitor(scope);
        CDTypeSymbolReference leftType = leftVisitor.getTypeFromExpression(node.getLeftExpression());
        OCLExpressionTypeInferingVisitor rightVisitor = new OCLExpressionTypeInferingVisitor(scope);
        CDTypeSymbolReference rightType = rightVisitor.getTypeFromExpression(node.getRightExpression());
        CDTypeSymbolReference amountType = new CDTypeSymbolReference("Number", this.scope);

        leftType = TypeInferringHelper.removeAllOptionals(leftType);
        rightType = TypeInferringHelper.removeAllOptionals(rightType);

        if(leftType.existsReferencedSymbol() && rightType.existsReferencedSymbol()) {
            if(leftType.isSameOrSuperType(amountType) && rightType.isSameOrSuperType(amountType)){
                Unit<?> leftUnit = leftVisitor.getReturnUnit().orElse(Unit.ONE);
                Unit<?> rightUnit = rightVisitor.getReturnUnit().orElse(Unit.ONE);
                if(!leftUnit.isCompatible(rightUnit)){
                    Log.error("0xCET03 Units mismatch on infix expression at " + node.get_SourcePositionStart() +
                        " left: " + leftUnit.toString() + " right: " + rightUnit.toString(), node.get_SourcePositionStart());
                }
            }
            else if (!leftType.isSameOrSuperType(rightType) && !rightType.isSameOrSuperType(leftType)) {
                Log.error("0xCET01 Types mismatch on infix expression at " + node.get_SourcePositionStart() +
                        " left: " + leftType.getStringRepresentation() + " right: " + rightType.getStringRepresentation(), node.get_SourcePositionStart());
            }
        }
    }

    public void checkPrefixExpr(ASTExpression node){
        OCLExpressionTypeInferingVisitor exprVisitor = new OCLExpressionTypeInferingVisitor(scope);
        CDTypeSymbolReference exprType = exprVisitor.getTypeFromExpression(node);
        exprType = TypeInferringHelper.removeAllOptionals(exprType);

        if (!exprType.getName().equals("Boolean")) {
            Log.error("0xCET02 type of prefix expression must be Boolean, but is: " + exprType.getStringRepresentation() + " " + node.get_SourcePositionStart()
            , node.get_SourcePositionStart(), node.get_SourcePositionEnd());
        }
    }

    @Override
    public void visit(ASTModuloExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTDivideExpression node) {
        // Todo Amount or Number
    }

    @Override
    public void visit(ASTMultExpression node) {
        // Todo Amount or Number
    }

    @Override
    public void visit(ASTPlusExpression node){
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTMinusExpression node){
        checkInfixExpr(node);
    }

    /**
     *  ********** boolean expressions **********
     */

    @Override
    public void visit(ASTEqualsExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTBooleanNotExpression node) {
        checkPrefixExpr(node.getExpression());
    }

    @Override
    public void visit(ASTLogicalNotExpression node) {
        checkPrefixExpr(node.getExpression());
    }

    @Override
    public void visit(ASTEquivalentExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTLessEqualExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTGreaterEqualExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTLessThanExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTGreaterThanExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTNotEqualsExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTBooleanAndOpExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTBooleanOrOpExpression node) {
        checkInfixExpr(node);
    }
}
