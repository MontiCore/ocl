// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.symboltable.ImportStatement;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.stream.Collectors;

import static de.monticore.ocl.ocl._symboltable.OCLSymbolTableHelper.getImportStatements;

public class OCLScopesGenitor extends OCLScopesGenitorTOP {
  public OCLScopesGenitor() {
    super();
  }

  @Override
  public IOCLArtifactScope createFromAST(ASTOCLCompilationUnit node) {
    Log.errorIfNull(node,
      "0xAE884 Error by creating of the OCLScopesGenitor symbol table: top ast node is null");
    IOCLArtifactScope artifactScope = OCLMill.artifactScope();
    artifactScope.setPackageName(node.getPackage());
    List<ImportStatement> imports = getImportStatements(node.getMCImportStatementList());
    artifactScope.setImportsList(imports);
    artifactScope.setName(node.getOCLArtifact().getName());

    putOnStack(artifactScope);
    node.accept(getTraverser());
    return artifactScope;
  }

  @Override
  public void visit(final ASTOCLCompilationUnit compilationUnit) {
    super.visit(compilationUnit);

    final String oclFile = OCLSymbolTableHelper.getNameOfModel(compilationUnit);
    Log.debug("Building Symboltable for OCL: " + oclFile,
      OCLScopesGenitor.class.getSimpleName());

    final String compilationUnitPackage = Names.getQualifiedName(compilationUnit.getPackageList());

    // imports
    final List<ImportStatement> imports = compilationUnit.streamMCImportStatements()
      .map(i -> new ImportStatement(i.getQName(), i.isStar())).collect(Collectors.toList());

    getCurrentScope().get().setAstNode(compilationUnit);

    final OCLArtifactScope enclosingScope = (OCLArtifactScope) compilationUnit.getEnclosingScope();
    enclosingScope.setImportsList(imports);
    enclosingScope.setPackageName(compilationUnitPackage);
  }

  @Override
  public void endVisit(final ASTOCLCompilationUnit compilationUnit) {
    removeCurrentScope();
    super.endVisit(compilationUnit);
  }

  @Override
  public void visit(final ASTOCLInvariant node)  {
    if (!getCurrentScope().isPresent()) {
      Log.debug(String.format("%s: Visiting %s, missing scope on scope stack.",
        node.get_SourcePositionStart(), node.getClass()), "ScopesGenitor");
      return;
    }
    // link the ast with its enclosing scope
    node.setEnclosingScope(getCurrentScope().get());
    // create the spanned scope
    IOCLScope scope = createScope(false);
    // link the ast with the spanned scope
    scope.setAstNode(node);
    node.setSpannedScope(scope);

    if (node.isPresentName()) {
      // create the symbol
      OCLInvariantSymbol symbol = OCLMill.oCLInvariantSymbolBuilder().setName(node.getName()).build();
      // link the symbol with its enclosing scope
      getCurrentScope().get().add(symbol);
      symbol.setEnclosingScope(getCurrentScope().get());
      // link the symbol with its ast
      symbol.setAstNode(node);
      node.setSymbol(symbol);
      // link the symbol with the spanned scope
      scope.setSpanningSymbol(symbol);
      symbol.setSpannedScope(scope);
    }
    putOnStack(scope);
  }
}
