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

import de.monticore.ast.ASTNode;
import de.monticore.commonexpressions._ast.*;
import de.monticore.expressionsbasis._ast.ASTExpression;
import de.monticore.numberunit._ast.ASTI;
import de.monticore.numberunit.prettyprint.UnitsPrinter;
import de.monticore.oclexpressions._ast.*;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Symbol;
import de.monticore.types.types._ast.ASTSimpleReferenceType;
import de.monticore.umlcd4a.symboltable.CDTypeSymbol;
import de.monticore.umlcd4a.symboltable.references.CDTypeSymbolReference;
import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._ast.*;
import ocl.monticoreocl.ocl._symboltable.OCLVariableDeclarationKind;
import ocl.monticoreocl.ocl._symboltable.OCLVariableDeclarationSymbol;
import ocl.monticoreocl.ocl._visitor.OCLVisitor;

import javax.measure.unit.Unit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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

  /********************************************** handle typeif special case including automatic type casting,
   * cf. http://mbse.se-rwth.de/book1/index.php?c=chapter3-3#x1-550003.3.5 */
  boolean isTypeIf = false;
  boolean isInstanceOf = false;
  boolean isThenExpressionPart = false;
  List<String> typeIfNames; // typeif x instanceof Component ... -> "Component" is stored in typeIfNames
  List<String> qualifiedPrimaryNames; // typeif x instanceof Component ... -> "x" is stored in qualifiedPrimaryNames
  CDTypeSymbolReference thenType;

  @Override
  public void visit(ASTTypeIfExpr node) {
    isTypeIf = true;
    typeIfNames = null;
    qualifiedPrimaryNames = null;
  }

  @Override
  public void endVisit(ASTTypeIfExpr node) {
    isTypeIf = false;
    OCLExpressionTypeInferingVisitor elseVisitor = new OCLExpressionTypeInferingVisitor(scope);
    CDTypeSymbolReference elseType = elseVisitor.getTypeFromExpression(node.getElseExpressionPart().getElseExpression());
    if (!thenType.isSameOrSuperType(elseType) && !elseType.isSameOrSuperType(thenType)) {
      Log.error("0xCET04 Types mismatch on typeif expression (then type differs from else type) at " + node.get_SourcePositionStart() +
          " then: " + thenType.getStringRepresentation() + " right: " + elseType.getStringRepresentation(), node.get_SourcePositionStart());
    }
  }

  @Override
  public void visit(ASTInstanceOfExpression node) {
    isInstanceOf = true;
  }

  @Override
  public void endVisit(ASTInstanceOfExpression node) {
    isInstanceOf = false;
  }

  private CDTypeSymbolReference createTypeRef(String typeName) {
    CDTypeSymbolReference typeReference = new CDTypeSymbolReference(typeName, scope);
    // Check if type was found in CD loaded CD models
    if (!typeReference.existsReferencedSymbol()) {
      Log.error("0xOCLS2 This type could not be found: " + typeName);
    }
    return typeReference;
  }


  @Override
  public void visit(ASTThenExpressionPart node) {
    String oldFullTypeName = null;
    Optional<OCLVariableDeclarationSymbol> symbol = scope.resolve(qualifiedPrimaryNames.stream().collect(Collectors.joining(".")), OCLVariableDeclarationSymbol.KIND);
    if (symbol.isPresent()) {
      oldFullTypeName = symbol.get().getType().getFullName();
      symbol.get().setType(createTypeRef(typeIfNames.stream().collect(Collectors.joining("."))) );
    }
    OCLExpressionTypeInferingVisitor thenVisitor = new OCLExpressionTypeInferingVisitor(scope);
    thenType = thenVisitor.getTypeFromExpression(node.getThenExpression());
    if (oldFullTypeName != null) {
      symbol.get().setType(createTypeRef(oldFullTypeName));
    }
  }

  @Override
  public void visit(ASTSimpleReferenceType node) {
    if (isTypeIf && isInstanceOf)
      typeIfNames = node.getNameList();
    else
      typeIfNames = null;
  }

  @Override
  public void visit(ASTOCLQualifiedPrimary node) {
    qualifiedPrimaryNames = node.getNameList();
  }

  /*************************** end handling special case typeif ********************************************/

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
            else if (!leftType.isSameOrSuperType(rightType) && !rightType.isSameOrSuperType(leftType) && !existsCommonSubClass(leftType, rightType)) {
                Log.error("0xCET01 Types mismatch on infix expression at " + node.get_SourcePositionStart() +
                        " left: " + leftType.getStringRepresentation() + " right: " + rightType.getStringRepresentation(), node.get_SourcePositionStart());
            }
        }
    }

  /**
   * interface Parameter;
   * class NaturalNumber;
   * class NaturalNumberParameter extends NaturalNumber implements Parameter;
   *
   * context Parameter p, NaturalNumber n inv:
   *   p == n // exist a subclass of both so that this conditions can be satisfied; if p instanceof NaturalNumberParameter and n instanceof NaturalNumberParameter they can be compared
   * @param type1
   * @param type2
   * @return
   */
    protected boolean existsCommonSubClass(CDTypeSymbolReference type1, CDTypeSymbolReference type2) {
      Collection<CDTypeSymbol> allTypeSymbols = type1.getEnclosingScope().resolveLocally(CDTypeSymbol.KIND);
      return allTypeSymbols.stream().filter(s -> !s.getFullName().equals(type1.getName()) && !s.getFullName().equals(type2.getFullName()))
          .anyMatch(s -> s.hasSuperTypeByFullName(type1.getFullName()) && s.hasSuperTypeByFullName(type2.getFullName()));
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
