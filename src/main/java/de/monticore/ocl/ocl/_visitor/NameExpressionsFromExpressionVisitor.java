// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._visitor;

import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisVisitor2;
import java.util.LinkedHashSet;
import java.util.Set;

public class NameExpressionsFromExpressionVisitor implements ExpressionsBasisVisitor2 {

  protected Set<String> varNames = new LinkedHashSet<>();

  public Set<String> getVarNames() {
    return varNames;
  }

  @Override
  public void endVisit(ASTNameExpression node) {
    varNames.add(node.getName());
  }
}
