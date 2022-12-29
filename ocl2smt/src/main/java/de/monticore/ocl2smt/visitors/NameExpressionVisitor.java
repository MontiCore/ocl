/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.visitors;

import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisVisitor2;
import java.util.HashSet;
import java.util.Set;

public class NameExpressionVisitor implements ExpressionsBasisVisitor2 {
  Set<String> variableNameSet = new HashSet<>();

  @Override
  public void visit(ASTNameExpression node) {
    variableNameSet.add(node.getName());
  }

  public Set<String> getVariableNameSet() {
    return variableNameSet;
  }
}
