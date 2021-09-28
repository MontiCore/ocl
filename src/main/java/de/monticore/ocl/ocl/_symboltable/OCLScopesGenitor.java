// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
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
  public  void visit (de.monticore.ocl.ocl._ast.ASTOCLInvariant node)  {
    if (node.isPresentName()) {
      OCLInvariantSymbol symbol = OCLMill.oCLInvariantSymbolBuilder().setName(node.getName()).build();
      if (getCurrentScope().isPresent()) {
        getCurrentScope().get().add(symbol);
      } else {
        Log.warn("0xA50212 Symbol cannot be added to current scope, since no scope exists.");
      }
      IOCLScope scope = createScope(false);
      putOnStack(scope);
      symbol.setSpannedScope(scope);
      // symbol -> ast
      symbol.setAstNode(node);

      // ast -> symbol
      node.setSymbol(symbol);
      node.setEnclosingScope(symbol.getEnclosingScope());

      // ast -> spannedScope
      // scope -> ast
      scope.setAstNode(node);

      // ast -> scope
      node.setSpannedScope(scope);
    }
  }
}
