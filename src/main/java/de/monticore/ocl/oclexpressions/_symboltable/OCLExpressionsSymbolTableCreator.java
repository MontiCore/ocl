/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.oclexpressions._symboltable;

import de.monticore.ocl.oclexpressions._ast.ASTInDeclaration;
import de.monticore.ocl.oclexpressions._ast.ASTInDeclarationVariable;
import de.monticore.ocl.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfObject;
import de.se_rwth.commons.logging.Log;

import java.util.Deque;
import java.util.Optional;

public class OCLExpressionsSymbolTableCreator extends OCLExpressionsSymbolTableCreatorTOP {

  private DeriveSymTypeOfOCLCombineExpressions typeVisitor;

  public OCLExpressionsSymbolTableCreator(){
    super();
    typeVisitor = new DeriveSymTypeOfOCLCombineExpressions();
  }

  public OCLExpressionsSymbolTableCreator(IOCLExpressionsScope enclosingScope) {
    super(enclosingScope);
    typeVisitor = new DeriveSymTypeOfOCLCombineExpressions();
  }

  public OCLExpressionsSymbolTableCreator(Deque<? extends IOCLExpressionsScope> scopeStack) {
    super(scopeStack);
    typeVisitor = new DeriveSymTypeOfOCLCombineExpressions();
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
    VariableSymbol symbol = create_OCLVariableDeclaration(node);
    if(getCurrentScope().isPresent()){
      symbol.setEnclosingScope(getCurrentScope().get());
    }
    addToScopeAndLinkWithNode(symbol, node);
    initialize_OCLVariableDeclaration(symbol, node);
  }

  @Override
  public void initialize_OCLVariableDeclaration(VariableSymbol symbol, ASTOCLVariableDeclaration ast) {
    symbol.setIsReadOnly(false);
    if(ast.isPresentMCType()) {
      ast.getMCType().setEnclosingScope(ast.getEnclosingScope());
      ast.getMCType().accept(getRealThis());
      final Optional<SymTypeExpression> typeResult = typeVisitor.calculateType(ast.getMCType());
      if (!typeResult.isPresent()) {
        Log.error(String.format("The type (%s) of the object (%s) could not be calculated", ast.getMCType(), ast.getName()));
      } else {
        symbol.setType(typeResult.get());
      }
    } else {
      if(ast.isPresentExpression()){
        ast.getExpression().accept(getRealThis());
        ast.getExpression().accept(typeVisitor);
        if(typeVisitor.getTypeCheckResult().isPresentCurrentResult()){
          symbol.setType(typeVisitor.getTypeCheckResult().getCurrentResult());
        } else {
          Log.error(String.format("The type of the object (%s) could not be calculated", ast.getName()));
        }
      }
      else {
        symbol.setType(SymTypeExpressionFactory.createTypeObject("Object", ast.getEnclosingScope()));
      }
    }
  }

  @Override
  public void visit(ASTInDeclaration node){
    for(int i = 0; i < node.getInDeclarationVariableList().size(); i++){
      VariableSymbol symbol = create_InDeclarationVariable(node.getInDeclarationVariable(i));
      if(getCurrentScope().isPresent()){
        symbol.setEnclosingScope(getCurrentScope().get());
      }
      addToScopeAndLinkWithNode(symbol, node.getInDeclarationVariable(i));
      initialize_InDeclarationVariable(symbol, node);
    }
  }

  @Override
  public void visit(ASTInDeclarationVariable node){

  }

  @Override
  public void initialize_InDeclarationVariable(VariableSymbol symbol, ASTInDeclarationVariable ast){

  }

  public void initialize_InDeclarationVariable(VariableSymbol symbol, ASTInDeclaration ast) {
    symbol.setIsReadOnly(false);
    if(ast.isPresentMCType()){
      ast.getMCType().setEnclosingScope(ast.getEnclosingScope());
      ast.getMCType().accept(getRealThis());
      final Optional<SymTypeExpression> typeResult = typeVisitor.calculateType(ast.getMCType());
      if (!typeResult.isPresent()) {
        Log.error(String.format("The type (%s) of the object (%s) could not be calculated", ast.getMCType(), symbol.getName()));
      } else {
        symbol.setType(typeResult.get());
      }
    } else {
      if(ast.isPresentExpression()){
        ast.getExpression().accept(getRealThis());
        ast.getExpression().accept(typeVisitor);
        if(typeVisitor.getTypeCheckResult().isPresentCurrentResult()){
          symbol.setType(typeVisitor.getTypeCheckResult().getCurrentResult());
        } else {
          Log.error(String.format("The type of the object (%s) could not be calculated", symbol.getName()));
        }
      }
      else {
        symbol.setType(SymTypeExpressionFactory.createTypeObject("Object", ast.getEnclosingScope()));
      }
    }
  }
}
