// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.oclexpressions._symboltable;

import de.monticore.ocl.oclexpressions._ast.ASTInDeclaration;
import de.monticore.ocl.oclexpressions._ast.ASTInDeclarationVariable;
import de.monticore.ocl.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.ocl.types.check.OCLTypeCheck;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.se_rwth.commons.logging.Log;

import java.util.Deque;
import java.util.Optional;

public class OCLExpressionsScopesGenitor extends OCLExpressionsScopesGenitorTOP {

  protected DeriveSymTypeOfOCLCombineExpressions typeVisitor;

  public OCLExpressionsScopesGenitor(){
    super();
  }

  public OCLExpressionsScopesGenitor(IOCLExpressionsScope enclosingScope) {
    super(enclosingScope);
  }

  public OCLExpressionsScopesGenitor(Deque<? extends IOCLExpressionsScope> scopeStack) {
    super(scopeStack);
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
  public void visit(ASTOCLVariableDeclaration node){

  }

  @Override
  public void endVisit(ASTOCLVariableDeclaration node){
    VariableSymbol symbol = create_OCLVariableDeclaration(node).build();
    if(getCurrentScope().isPresent()){
      symbol.setEnclosingScope(getCurrentScope().get());
    }
    addToScopeAndLinkWithNode(symbol, node);
  }

  @Override
  public void visit(ASTInDeclaration node){

  }

  @Override
  public void endVisit(ASTInDeclaration node){
    for(int i = 0; i < node.getInDeclarationVariableList().size(); i++){
      VariableSymbol symbol = create_InDeclarationVariable(node.getInDeclarationVariable(i)).build();
      if(getCurrentScope().isPresent()){
        symbol.setEnclosingScope(getCurrentScope().get());
      }
      addToScopeAndLinkWithNode(symbol, node.getInDeclarationVariable(i));
      if (node.isPresentMCType()) {
        node.getMCType().setEnclosingScope(symbol.getEnclosingScope());
        node.getMCType().accept(getTraverser());
      }
    }
  }

  @Override
  public void visit(ASTInDeclarationVariable node){

  }
}
