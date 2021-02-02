// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.setexpressions._symboltable;

import de.monticore.ocl.setexpressions._visitor.SetExpressionsHandler;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsTraverser;
import de.monticore.ocl.util.CompleterUtil;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.basicsymbols._visitor.BasicSymbolsVisitor2;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;

import java.util.List;

public class SetExpressionsSymbolTableCompleter
  implements BasicSymbolsVisitor2, SetExpressionsHandler {
  protected static final String USED_BUT_UNDEFINED = "0xB0028: Type '%s' is used but not defined.";

  protected static final String DEFINED_MUTLIPLE_TIMES = "0xB0031: Type '%s' is defined more than once.";

  protected final List<ASTMCImportStatement> imports;

  protected final String packageDeclaration;

  protected SetExpressionsTraverser traverser;

  public SetExpressionsSymbolTableCompleter(List<ASTMCImportStatement> imports, String packageDeclaration) {
    this.imports = imports;
    this.packageDeclaration = packageDeclaration;
  }

  @Override
  public SetExpressionsTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(SetExpressionsTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void traverse(ISetExpressionsScope node) {
    SetExpressionsHandler.super.traverse(node);
    for (ISetExpressionsScope subscope : node.getSubScopes()) {
      subscope.accept(this.getTraverser());
    }
  }

  @Override
  public void visit(VariableSymbol var) {
    CompleterUtil.visit(var, imports, packageDeclaration);
  }
}
