package de.monticore.ocl2smt.visitors;

import de.monticore.ocl.setexpressions._ast.ASTSetVariableDeclaration;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsVisitor2;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SetVariableCollector implements SetExpressionsVisitor2 {
  Set<ASTSetVariableDeclaration> variableSet = new HashSet<>();

  @Override
  public void visit(ASTSetVariableDeclaration node) {
    variableSet.add(node);
  }

  public Set<String> getAllVariableNames() {
    return variableSet.stream().map(ASTSetVariableDeclaration::getName).collect(Collectors.toSet());
  }

  public Set<ASTSetVariableDeclaration> getAllVariables() {
    return variableSet;
  }
}
