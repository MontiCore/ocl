/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._symboltable;

import de.monticore.expressions.expressionsbasis._symboltable.ExpressionsBasisSymbolTableCreator;
import de.monticore.expressions.expressionsbasis._symboltable.IExpressionsBasisScope;
import de.monticore.expressions.oclexpressions._symboltable.OCLExpressionsSymbolTableCreator;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.symboltable.ImportStatement;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.Names;

import java.util.stream.Collectors;

public class OCLSymbolTableCreatorDelegator
    extends OCLSymbolTableCreatorDelegatorTOP {
  public OCLSymbolTableCreatorDelegator(IOCLGlobalScope globalScope) {
    super(globalScope);
  }

  public void setTypeVisitor(DeriveSymTypeOfOCLCombineExpressions typesCalculator) {
    ((OCLSymbolTableCreator) this.getOCLVisitor().get()).setTypeVisitor(typesCalculator);
    ((OCLExpressionsSymbolTableCreator) this.getOCLExpressionsVisitor().get()).setTypeVisitor(typesCalculator.getOCLExpressionsVisitorAsTypeVisitor());
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
    // TODO SVa: move to OCLSymbolTableCreator
    artifactScope.setPackageName(Names.getQualifiedName(rootNode.getPackageList()));
    artifactScope.setImportList(rootNode.streamMCImportStatements().map(i -> new ImportStatement(i.getQName(), i.isStar())).collect(Collectors.toList()));
    return artifactScope;
  }
}
