// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import de.monticore.ocl.ocl._visitor.OCLHandler;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.util.CompleterUtil;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.basicsymbols._visitor.BasicSymbolsVisitor2;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;

import java.util.List;

public class OCLSymbolTableCompleter implements BasicSymbolsVisitor2, OCLHandler {
  protected static final String USED_BUT_UNDEFINED = "0xB0028: Type '%s' is used but not defined.";

  protected static final String DEFINED_MUTLIPLE_TIMES = "0xB0031: Type '%s' is defined more than once.";

  protected final List<ASTMCImportStatement> imports;

  protected final String packageDeclaration;

  protected OCLTraverser traverser;

  public OCLSymbolTableCompleter(List<ASTMCImportStatement> imports, String packageDeclaration) {
    this.imports = imports;
    this.packageDeclaration = packageDeclaration;
  }

  @Override
  public OCLTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(OCLTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void traverse(IOCLScope node) {
    OCLHandler.super.traverse(node);
    for (IOCLScope subscope : node.getSubScopes()) {
      subscope.accept(this.getTraverser());
    }
  }

  @Override
  public void visit(VariableSymbol var) {
    CompleterUtil.visit(var, imports, packageDeclaration);
  }

  @Override
  public void visit(TypeSymbol type) {
    CompleterUtil.visit(type, imports, packageDeclaration);
  }

  @Override
  public void visit(FunctionSymbol function) {
    CompleterUtil.visit(function, imports, packageDeclaration);
  }
}