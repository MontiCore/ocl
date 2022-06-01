// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.setexpressions._symboltable;

import de.monticore.ocl.setexpressions._ast.ASTGeneratorDeclaration;
import de.monticore.ocl.setexpressions._ast.ASTSetVariableDeclaration;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsHandler;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsTraverser;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsVisitor2;
import de.monticore.ocl.types.check.OCLDeriver;
import de.monticore.ocl.types.check.OCLSynthesizer;
import de.monticore.ocl.types.check.OCLTypeCheck;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.basicsymbols._visitor.BasicSymbolsVisitor2;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.TypeCheckResult;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.se_rwth.commons.logging.Log;

import java.util.List;

public class SetExpressionsSymbolTableCompleter
  implements SetExpressionsVisitor2, BasicSymbolsVisitor2, SetExpressionsHandler {

  OCLDeriver deriver;

  OCLSynthesizer synthesizer;

  protected final List<ASTMCImportStatement> imports;

  protected final String packageDeclaration;

  protected SetExpressionsTraverser traverser;

  public void setDeriver(OCLDeriver deriver) {
    if (deriver != null) {
      this.deriver = deriver;
    }
    else {
      Log.error("0xA3201 The deriver has to be set");
    }
  }

  public void setSynthesizer(OCLSynthesizer synthesizer) {
    if (synthesizer != null) {
      this.synthesizer = synthesizer;
    }
    else {
      Log.error("0xA3204 The synthesizer has to be set");
    }
  }

  public SetExpressionsSymbolTableCompleter(List<ASTMCImportStatement> imports, String packageDeclaration) {
    this.imports = imports;
    this.packageDeclaration = packageDeclaration;
  }

  @Override
  public SetExpressionsTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(SetExpressionsTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void traverse(ISetExpressionsScope node) {
    SetExpressionsHandler.super.traverse(node);
    for (ISetExpressionsScope subscope : node.getSubScopes()) {
      subscope.accept(this.getTraverser());
    }
  }

  @Override
  public void visit(ASTSetVariableDeclaration node){

  }

  @Override
  public void endVisit(ASTSetVariableDeclaration node){
    initialize_SetVariableDeclaration(node.getSymbol(), node);
  }

  public void initialize_SetVariableDeclaration(VariableSymbol symbol, ASTSetVariableDeclaration ast) {
    symbol.setIsReadOnly(false);
    if(ast.isPresentMCType()) {
      ast.getMCType().setEnclosingScope(symbol.getEnclosingScope());
      ast.getMCType().accept(getTraverser());
      final TypeCheckResult typeResult = synthesizer.synthesizeType(ast.getMCType());
      if (!typeResult.isPresentResult()) {
        Log.error(String.format("The type (%s) of the object (%s) could not be calculated", ast.getMCType(), ast.getName()));
      } else {
        symbol.setType(typeResult.getResult());
      }
    } else {
      if(ast.isPresentExpression()){
        ast.getExpression().accept(getTraverser());
        final TypeCheckResult tcr_expr = deriver.deriveType(ast.getExpression());
        if(tcr_expr.isPresentResult()){
          symbol.setType(tcr_expr.getResult());
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
  public void endVisit(ASTGeneratorDeclaration node){
    initialize_GeneratorDeclaration(node.getSymbol(), node);
  }

  public void initialize_GeneratorDeclaration(VariableSymbol symbol, ASTGeneratorDeclaration ast) {
    symbol.setIsReadOnly(false);
    if(ast.isPresentMCType()) {
      ast.getMCType().setEnclosingScope(symbol.getEnclosingScope());
      ast.getMCType().accept(getTraverser());
      final TypeCheckResult typeResult = synthesizer.synthesizeType(ast.getMCType());
      if (!typeResult.isPresentResult()) {
        Log.error(String.format("The type (%s) of the object (%s) could not be calculated", ast.getMCType(), ast.getName()));
      }
      else {
        symbol.setType(typeResult.getResult());
      }
    } else {
      final TypeCheckResult typeResult = deriver.deriveType(ast.getExpression());
      if(!typeResult.isPresentResult()){
        Log.error(String.format("The type of the object (%s) could not be calculated", ast.getName()));
      }
      else if(typeResult.getResult().isPrimitive()){
        Log.error(String.format("Expression of object (%s) has to be a collection", ast.getName()));
      }
      else {
        SymTypeExpression result = OCLTypeCheck.unwrapSet(typeResult.getResult());
        symbol.setType(result);
      }
    }
  }
}
