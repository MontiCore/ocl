/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.ocl._symboltable;

import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.expressions.expressionsbasis._symboltable.IExpressionsBasisScope;
import de.monticore.ocl.expressions.oclexpressions._ast.ASTLetinExpression;
import de.monticore.ocl.expressions.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.ocl.expressions.oclexpressions._ast.ASTTypeIfExpression;
import de.monticore.ocl.expressions.oclexpressions._symboltable.OCLExpressionsSymbolTableCreator;
import de.monticore.ocl.ocl._ast.ASTOCLParamDeclaration;
import de.monticore.ocl.expressions.oclexpressions._symboltable.IOCLExpressionsScope;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.IOOSymbolsScope;
import de.monticore.symboltable.ImportStatement;
import de.monticore.types.check.DefsTypeBasic;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SynthesizeSymTypeFromMCSimpleGenericTypes;
import de.monticore.types.check.TypeCheck;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedName;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mccollectiontypes._ast.ASTMCSetType;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.logging.Log;

import java.util.*;
import java.util.stream.Collectors;

public class OCLSymbolTableCreator extends OCLSymbolTableCreatorTOP {

  DeriveSymTypeOfOCLCombineExpressions typeVisitor;

  SynthesizeSymTypeFromMCSimpleGenericTypes typeChecker = new SynthesizeSymTypeFromMCSimpleGenericTypes();

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
      //TODO: add this and super to scope only if it is possible to use them
      addToScope(new VariableSymbol("this"));
      addToScope(new VariableSymbol("super"));
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
    ast.getMCType().accept(this);
    final Optional<SymTypeExpression> typeResult = typeVisitor.calculateType(ast.getMCType());
    if (!typeResult.isPresent()) {
      Log.error(String.format("The type (%s) of the object (%s) could not be calculated", ast.getMCType(), ast.getName()));
    } else {
      symbol.setType(typeResult.get());
    }
  }
}