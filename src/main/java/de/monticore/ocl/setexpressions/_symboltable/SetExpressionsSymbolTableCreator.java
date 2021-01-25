/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.setexpressions._symboltable;

import de.monticore.ocl.setexpressions._ast.ASTGeneratorDeclaration;
import de.monticore.ocl.setexpressions._ast.ASTSetVariableDeclaration;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.ocl.types.check.OCLTypeCheck;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.se_rwth.commons.logging.Log;

import java.util.Deque;
import java.util.Optional;

public class SetExpressionsSymbolTableCreator extends SetExpressionsSymbolTableCreatorTOP {
  private DeriveSymTypeOfOCLCombineExpressions typeVisitor;

  public SetExpressionsSymbolTableCreator(){
    super();
    typeVisitor = new DeriveSymTypeOfOCLCombineExpressions();
  }

  public SetExpressionsSymbolTableCreator(ISetExpressionsScope enclosingScope) {
    super(enclosingScope);
    typeVisitor = new DeriveSymTypeOfOCLCombineExpressions();
  }

  public SetExpressionsSymbolTableCreator(Deque<? extends ISetExpressionsScope> scopeStack) {
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
  public void visit(ASTSetVariableDeclaration node){

  }

  @Override
  public void endVisit(ASTSetVariableDeclaration node){
    VariableSymbol symbol = create_SetVariableDeclaration(node);
    if(getCurrentScope().isPresent()){
      symbol.setEnclosingScope(getCurrentScope().get());
    }
    addToScopeAndLinkWithNode(symbol, node);
    initialize_SetVariableDeclaration(symbol, node);
  }

  @Override
  public void initialize_SetVariableDeclaration(VariableSymbol symbol, ASTSetVariableDeclaration ast) {
    symbol.setIsReadOnly(false);
    if(ast.isPresentMCType()) {
      ast.getMCType().setEnclosingScope(symbol.getEnclosingScope());
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
        ast.getExpression().accept(typeVisitor.getTraverser());
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
  public void visit(ASTGeneratorDeclaration node){

  }

  @Override
  public void endVisit(ASTGeneratorDeclaration node){
    VariableSymbol symbol = create_GeneratorDeclaration(node);
    if (getCurrentScope().isPresent()) {
      symbol.setEnclosingScope(getCurrentScope().get());
    }
    addToScopeAndLinkWithNode(symbol, node);
    initialize_GeneratorDeclaration(symbol, node);
  }

  @Override
  public void initialize_GeneratorDeclaration(VariableSymbol symbol, ASTGeneratorDeclaration ast) {
    symbol.setIsReadOnly(false);
    if(ast.isPresentMCType()) {
      ast.getMCType().setEnclosingScope(symbol.getEnclosingScope());
      ast.getMCType().accept(getRealThis());
      final Optional<SymTypeExpression> typeResult = typeVisitor.calculateType(ast.getMCType());
      if (!typeResult.isPresent()) {
        Log.error(String.format("The type (%s) of the object (%s) could not be calculated", ast.getMCType(), ast.getName()));
      }
      else {
        symbol.setType(typeResult.get());
      }
    } else {
      final Optional<SymTypeExpression> typeResult = typeVisitor.calculateType(ast.getExpression());
      if(!typeResult.isPresent()){
        Log.error(String.format("The type of the object (%s) could not be calculated", ast.getName()));
      }
      else if(typeResult.get().isTypeConstant()){
        Log.error(String.format("Expression of object (%s) has to be a collection", ast.getName()));
      }
      else {
        SymTypeExpression result = OCLTypeCheck.unwrapSet(typeResult.get());
        symbol.setType(result);
      }
    }
  }
}
