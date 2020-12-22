package de.monticore.ocl.ocl._visitor;

import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class NameExpressionsFromExpressionVisitor implements OCLInheritanceVisitor {

  private Set<String> varNames;
  private OCLVisitor realThis;

  public NameExpressionsFromExpressionVisitor(){
    varNames = new HashSet<>();
    realThis = this;
  }

  public Set<String> getVarNames(){
    return varNames;
  }

  @Override
  public void endVisit(ASTNameExpression node){
    varNames.add(node.getName());
  }
}
