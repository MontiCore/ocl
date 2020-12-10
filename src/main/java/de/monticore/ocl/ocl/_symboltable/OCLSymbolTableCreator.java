/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.ocl._symboltable;


import de.monticore.ocl.ocl._ast.ASTOCLParamDeclaration;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symboltable.ImportStatement;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.logging.Log;

import java.util.*;
import java.util.stream.Collectors;

public class OCLSymbolTableCreator extends OCLSymbolTableCreatorTOP {

  DeriveSymTypeOfOCLCombineExpressions typeVisitor;

  public OCLSymbolTableCreator(){super(); }

  public OCLSymbolTableCreator(IOCLScope enclosingScope) {
    super(enclosingScope);
  }

  public OCLSymbolTableCreator(Deque<? extends IOCLScope> scopeStack) {
    super(scopeStack);
  }

  @Override
  public IOCLArtifactScope createFromAST(ASTOCLCompilationUnit node){
    if(typeVisitor == null){
      Log.error("Set the typeVisitor before building the symboltable");
      return null;
    }
    else{
      Log.errorIfNull(node, "0xA7004x51423 Error by creating of the OCLSymbolTableCreator symbol table: top ast node is null");
      IOCLArtifactScope artifactScope = de.monticore.ocl.ocl.OCLMill.oCLArtifactScopeBuilder()
              .setPackageName("")
              .setImportsList(new ArrayList<>())
              .build();
      putOnStack(artifactScope);
      node.accept(getRealThis());
      return artifactScope;
    }
  }

  public void setTypeVisitor(DeriveSymTypeOfOCLCombineExpressions typesCalculator) {
    if (typesCalculator != null) {
      this.typeVisitor = typesCalculator;
    }
    else {
      Log.error("0xA3201 The typesVisitor has to be set");
    }
  }

  @Override
  public void visit(final ASTOCLCompilationUnit compilationUnit) {
    super.visit(compilationUnit);

    final String oclFile = OCLSymbolTableHelper.getNameOfModel(compilationUnit);
    Log.debug("Building Symboltable for OCL: " + oclFile,
      OCLSymbolTableCreator.class.getSimpleName());

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
  public void visit(ASTOCLParamDeclaration node){
    VariableSymbol symbol = create_OCLParamDeclaration(node);
    if(getCurrentScope().isPresent()){
      symbol.setEnclosingScope(getCurrentScope().get());
    }
    addToScopeAndLinkWithNode(symbol, node);
    initialize_OCLParamDeclaration(symbol, node);
  }

  @Override
  public void initialize_OCLParamDeclaration(VariableSymbol symbol, ASTOCLParamDeclaration ast) {
    ast.getMCType().setEnclosingScope(ast.getEnclosingScope());
    ast.getMCType().accept(getRealThis());
    final Optional<SymTypeExpression> typeResult = typeVisitor.calculateType(ast.getMCType());
    if (!typeResult.isPresent()) {
      Log.error(String.format("The type (%s) of the object (%s) could not be calculated", ast.getMCType(), ast.getName()));
    } else {
      symbol.setType(typeResult.get());
      symbol.setIsReadOnly(false);
    }
  }

  @Override
  public void visit (ASTOCLInvariant node){
    if (getCurrentScope().isPresent()) {
      node.setEnclosingScope(getCurrentScope().get());
    }
    else {
      Log.error("Could not set enclosing scope of ASTNode \"" + node
              + "\", because no scope is set yet!");
    }
    //check whether symbols for "this" and "super" should be introduced
    if (!node.isEmptyOCLContextDefinitions()){
      for (ASTOCLContextDefinition cd : node.getOCLContextDefinitionList()){
        if (!cd.isPresentExpression()){
          //TODO: Only create symbols if cd is MCType?, use supertype for super instead of type?, case of 2 context definitions
          ASTMCType type;
          if (cd.isPresentOCLParamDeclaration()){
            type = cd.getOCLParamDeclaration().getMCType();
          }
          else {
            type = cd.getMCType();
          }
          type.setEnclosingScope(getCurrentScope().get());
          final Optional<SymTypeExpression> typeResult = typeVisitor.calculateType(type);
          if (!typeResult.isPresent()) {
            Log.error(String.format("The type (%s) could not be calculated", type));
          } else {
            //create VariableSymbols for "this" and "super"
            VariableSymbol t = new VariableSymbol("this");
            t.setType(typeResult.get());
            t.setIsReadOnly(false);
            addToScope(t);
            VariableSymbol s = new VariableSymbol("super");
            s.setType(typeResult.get());
            s.setIsReadOnly(false);
            addToScope(s);
          }
        }
      }
    }
  }
}