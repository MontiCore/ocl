/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.visitors;

import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpressionTOP;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisVisitor2;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class NameExpressionCollector implements ExpressionsBasisVisitor2 {
  Set<ASTNameExpression> variableNameSet = new HashSet<>();

  @Override
  public void visit(ASTNameExpression node) {
    variableNameSet.add(node);
  }

  public Set<String> getVariableNameSet() {
    return variableNameSet.stream().map(ASTNameExpressionTOP::getName).collect(Collectors.toSet());
  }

  public Set<ASTNameExpression> getVariableNames() {
    return variableNameSet;
  }
}
