package de.monticore.ocl2smt.visitors;

import de.monticore.ocl.setexpressions._ast.ASTGeneratorDeclaration;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsVisitor2;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SetGeneratorCollector implements SetExpressionsVisitor2 {
  Set<ASTGeneratorDeclaration> variableSet = new HashSet<>();

  public void visit(ASTGeneratorDeclaration node) {
    variableSet.add(node);
  }

  public Set<String> getAllVariableNames() {
    return variableSet.stream().map(ASTGeneratorDeclaration::getName).collect(Collectors.toSet());
  }

  public Set<ASTGeneratorDeclaration> getAllGenrators() {
    return variableSet;
  }
}
