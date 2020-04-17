/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._symboltable;

import de.monticore.expressions.expressionsbasis._symboltable.ExpressionsBasisSymbolTableCreator;
import de.monticore.expressions.expressionsbasis._symboltable.IExpressionsBasisScope;
import de.monticore.expressions.oclexpressions._symboltable.OCLExpressionsSymbolTableCreator;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.symboltable.ImportStatement;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.se_rwth.commons.Joiners;

import java.util.stream.Collectors;

public class OCLSymbolTableCreatorDelegator
    extends OCLSymbolTableCreatorDelegatorTOP {
  public OCLSymbolTableCreatorDelegator(IOCLGlobalScope globalScope) {
    super(globalScope);
  }

  public void setTypeVisitor(DeriveSymTypeOfOCLCombineExpressions typesCalculator) {
    ((OCLSymbolTableCreator)this.getOCLVisitor().get()).setTypeVisitor(typesCalculator);
    ((OCLExpressionsSymbolTableCreator) this.getOCLExpressionsVisitor().get()).setTypeVisitor(typesCalculator.getOCLExpressionsVisitorAsDerivedType());
  }

  public IExpressionsBasisScope createScope(boolean shadowing) {
    final IExpressionsBasisScope subScope = ((ExpressionsBasisSymbolTableCreator) this.getExpressionsBasisVisitor().get()).createScope(shadowing);
    return subScope;
  }

  public IOCLGlobalScope getGlobalScope() {
    return globalScope;
  }

  @Override
  public OCLArtifactScope createFromAST(ASTOCLCompilationUnit rootNode) {
    final OCLArtifactScope artifactScope = super.createFromAST(rootNode);
    artifactScope.setPackageName(Joiners.DOT.join(rootNode.getPackageList()));
    artifactScope.setImportList(rootNode.streamMCImportStatements().map(i -> new ImportStatement(i.getQName(), i.isStar())).collect(Collectors.toList()));
    return artifactScope;
  }

  @Override
  public void handle(ASTMCImportStatement node) {
    super.handle(node);
/*
    final CD4AnalysisGlobalScope globalScope = CD4AnalysisSymTabMill.cD4AnalysisGlobalScopeBuilder().setModelPath(node.getQName()).build();
    final CD4AnalysisArtifactScope scope = CD4AnalysisSymTabMill.cD4AnalysisArtifactScopeBuilder().setEnclosingScope(globalScope).build();
    final CDDefinitionSymbolLoader loader = new CDDefinitionSymbolLoader(node.getQName(), scope);
    final Optional<CDDefinitionSymbol> cdDefinitionSymbol = loader.loadSymbol();
    System.out.println(cdDefinitionSymbol);

 */
  }
}
