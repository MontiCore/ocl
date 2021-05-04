// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.oclexpressions._symboltable;

import de.monticore.ocl.ocl._symboltable.IOCLScope;
import de.monticore.ocl.oclexpressions.OCLExpressionsMill;
import de.monticore.ocl.oclexpressions._ast.ASTInDeclaration;
import de.monticore.ocl.oclexpressions._ast.ASTInDeclarationVariable;
import de.monticore.ocl.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.se_rwth.commons.logging.Log;

public class OCLExpressionsScopesGenitor extends OCLExpressionsScopesGenitorTOP {

  protected DeriveSymTypeOfOCLCombineExpressions typeVisitor;

  public OCLExpressionsScopesGenitor(){
    super();
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
    VariableSymbol symbol = OCLExpressionsMill.variableSymbolBuilder().setName(node.getName()).build();
    if(getCurrentScope().isPresent()){
      symbol.setEnclosingScope(getCurrentScope().get());
    }
    if (getCurrentScope().isPresent()) {
      getCurrentScope().get().add(symbol);
    } else {
      Log.warn("0xA50212 Symbol cannot be added to current scope, since no scope exists.");
    }
    // symbol -> ast
    symbol.setAstNode(node);

    // ast -> symbol
    node.setSymbol(symbol);
    node.setEnclosingScope(symbol.getEnclosingScope());
  }

  @Override
  public void visit(ASTInDeclaration node){

  }

  @Override
  public void endVisit(ASTInDeclaration node){
    for(int i = 0; i < node.getInDeclarationVariableList().size(); i++){
      VariableSymbol symbol = OCLExpressionsMill.variableSymbolBuilder().setName(node.getInDeclarationVariable(i).getName()).build();
      if(getCurrentScope().isPresent()){
        symbol.setEnclosingScope(getCurrentScope().get());
      }
      if (getCurrentScope().isPresent()) {
        getCurrentScope().get().add(symbol);
      } else {
        Log.warn("0xA50212 Symbol cannot be added to current scope, since no scope exists.");
      }
      // symbol -> ast
      symbol.setAstNode(node.getInDeclarationVariable(i));

      // ast -> symbol
      node.getInDeclarationVariable(i).setSymbol(symbol);
      node.getInDeclarationVariable(i).setEnclosingScope(symbol.getEnclosingScope());
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
