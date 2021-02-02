// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.oclexpressions._symboltable;

import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.ocl.util.CompleterUtil;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.basicsymbols._visitor.BasicSymbolsVisitor2;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;

import java.util.List;

public class OCLExpressionsSymbolTableCompleter
  implements BasicSymbolsVisitor2, OCLExpressionsHandler {
  protected static final String USED_BUT_UNDEFINED = "0xB0028: Type '%s' is used but not defined.";

  protected static final String DEFINED_MUTLIPLE_TIMES = "0xB0031: Type '%s' is defined more than once.";

  protected final List<ASTMCImportStatement> imports;

  protected final String packageDeclaration;

  protected OCLExpressionsTraverser traverser;

  public OCLExpressionsSymbolTableCompleter(List<ASTMCImportStatement> imports, String packageDeclaration) {
    this.imports = imports;
    this.packageDeclaration = packageDeclaration;
  }

  @Override
  public OCLExpressionsTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(OCLExpressionsTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void traverse(IOCLExpressionsScope node) {
    OCLExpressionsHandler.super.traverse(node);
    for (IOCLExpressionsScope subscope : node.getSubScopes()) {
      subscope.accept(this.getTraverser());
    }
  }

  @Override
  public void visit(VariableSymbol var) {
    CompleterUtil.visit(var, imports, packageDeclaration);
  }
}
