// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.util;

import de.monticore.expressions.bitexpressions._ast.ASTBinaryAndExpression;
import de.monticore.expressions.bitexpressions._ast.ASTBinaryOrOpExpression;
import de.monticore.expressions.bitexpressions._ast.ASTBinaryXorExpression;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.expressions.uglyexpressions._ast.ASTInstanceofExpression;
import de.monticore.expressions.uglyexpressions._ast.ASTTypeCastExpression;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.setexpressions._ast.*;

public class SideEffectFreeExpressions {
  public static boolean sideEffectFree(ASTExpression e) {
    if (e instanceof ASTTypeCastExpression) {
      return true;
    }
    if (e instanceof ASTTypeIfExpression) {
      return true;
    }
    if (e instanceof ASTIfThenElseExpression) {
      return true;
    }
    if (e instanceof ASTImpliesExpression) {
      return true;
    }
    if (e instanceof ASTEquivalentExpression) {
      return true;
    }
    if (e instanceof ASTForallExpression) {
      return true;
    }
    if (e instanceof ASTExistsExpression) {
      return true;
    }
    if (e instanceof ASTAnyExpression) {
      return true;
    }
    if (e instanceof ASTLetinExpression) {
      return true;
    }
    if (e instanceof ASTIterateExpression) {
      return true;
    }
    if (e instanceof ASTInstanceofExpression) {
      return true;
    }
    if (e instanceof ASTArrayAccessExpression) {
      return true;
    }
    if (e instanceof ASTOCLTransitiveQualification) {
      return true;
    }
    if (e instanceof ASTOCLAtPreQualification) {
      return true;
    }
    if (e instanceof ASTSetInExpression) {
      return true;
    }
    if (e instanceof ASTSetNotInExpression) {
      return true;
    }
    if (e instanceof ASTUnionExpression) {
      return true;
    }
    if (e instanceof ASTIntersectionExpression) {
      return true;
    }
    if (e instanceof ASTSetUnionExpression) {
      return true;
    }
    if (e instanceof ASTSetIntersectionExpression) {
      return true;
    }
    if (e instanceof ASTSetMinusExpression) {
      return true;
    }
    if (e instanceof ASTSetAndExpression) {
      return true;
    }
    if (e instanceof ASTSetOrExpression) {
      return true;
    }
    if (e instanceof ASTSetComprehension) {
      return true;
    }
    if (e instanceof ASTSetEnumeration) {
      return true;
    }
    if (e instanceof ASTInfixExpression) {
      return true;
    }
    if (e instanceof ASTPlusPrefixExpression) {
      return true;
    }
    if (e instanceof ASTMinusPrefixExpression) {
      return true;
    }
    if (e instanceof ASTBooleanNotExpression) {
      return true;
    }
    if (e instanceof ASTLogicalNotExpression) {
      return true;
    }
    if (e instanceof ASTConditionalExpression) {
      return true;
    }
    if (e instanceof ASTBracketExpression) {
      return true;
    }
    if (e instanceof ASTLiteralExpression) {
      return true;
    }
    if (e instanceof ASTFieldAccessExpression) {
      return true;
    }
    if (e instanceof ASTNameExpression) {
      return true;
    }
    if (e instanceof ASTCallExpression) {
      return true;
    }
    if (e instanceof ASTBinaryOrOpExpression) {
      return true;
    }
    if (e instanceof ASTBinaryAndExpression) {
      return true;
    }
    if (e instanceof ASTBinaryXorExpression) {
      return true;
    }
    return false;
  }
}
