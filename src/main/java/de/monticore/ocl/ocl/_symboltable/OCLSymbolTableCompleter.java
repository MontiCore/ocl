// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import de.monticore.ocl.ocl._ast.ASTOCLContextDefinition;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.ocl.ocl._ast.ASTOCLParamDeclaration;
import de.monticore.ocl.ocl._visitor.OCLHandler;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl._visitor.OCLVisitor2;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.basicsymbols._visitor.BasicSymbolsVisitor2;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.monticore.types.mcbasictypes._ast.ASTMCReturnType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types.mcsimplegenerictypes.MCSimpleGenericTypesMill;
import de.monticore.utils.Names;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;

public class OCLSymbolTableCompleter implements OCLVisitor2, BasicSymbolsVisitor2, OCLHandler {
  protected static final String USED_BUT_UNDEFINED = "0xB0028: Type '%s' is used but not defined.";

  protected static final String DEFINED_MUTLIPLE_TIMES = "0xB0031: Type '%s' is defined more than once.";

  DeriveSymTypeOfOCLCombineExpressions typeVisitor;

  protected final List<ASTMCImportStatement> imports;

  protected final String packageDeclaration;

  protected OCLTraverser traverser;

  public OCLSymbolTableCompleter(List<ASTMCImportStatement> imports, String packageDeclaration) {
    this.imports = imports;
    this.packageDeclaration = packageDeclaration;
  }

  @Override
  public OCLTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(OCLTraverser traverser) {
    this.traverser = traverser;
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
  public void traverse(IOCLScope node) {
    OCLHandler.super.traverse(node);
    for (IOCLScope subscope : node.getSubScopes()) {
      subscope.accept(this.getTraverser());
    }
  }

  public void initialize_OCLParamDeclaration(VariableSymbol symbol, ASTOCLParamDeclaration ast) {
    ast.getMCType().setEnclosingScope(ast.getEnclosingScope());
    ast.getMCType().accept(getTraverser());
    ast.getMCType().accept(typeVisitor.getTraverser());
    final Optional<SymTypeExpression> typeResult = typeVisitor.getResult();
    if (!typeResult.isPresent()) {
      Log.error(String.format("The type (%s) of the object (%s) could not be calculated", ast.getMCType(), ast.getName()));
    } else {
      symbol.setType(typeResult.get());
      symbol.setIsReadOnly(false);
    }
  }

  @Override
  public void visit(ASTOCLParamDeclaration node){
    VariableSymbol symbol = node.getSymbol();
    initialize_OCLParamDeclaration(symbol, node);
  }

  @Override
  public void visit (ASTOCLInvariant node){
    //check whether symbols for "this" and "super" should be introduced
    if (!node.isEmptyOCLContextDefinitions()){
      for (ASTOCLContextDefinition cd : node.getOCLContextDefinitionList()){
        if (cd.isPresentMCType()){
          ASTMCType type = cd.getMCType();
          type.setEnclosingScope(cd.getEnclosingScope());
          type.accept(typeVisitor.getTraverser());
          final Optional<SymTypeExpression> typeResult = typeVisitor.getResult();
          if (!typeResult.isPresent()) {
            Log.error(String.format("The type (%s) could not be calculated", type));
          } else {
            //create VariableSymbols for "this" and "super"
            VariableSymbol t = new VariableSymbol("this");
            t.setType(typeResult.get());
            t.setIsReadOnly(true);
            cd.getEnclosingScope().add(t);
            if(!typeResult.get().getTypeInfo().isEmptySuperTypes()){
              VariableSymbol s = new VariableSymbol("super");
              s.setType(typeResult.get().getTypeInfo().getSuperClass());
              s.setIsReadOnly(true);
              cd.getEnclosingScope().add(s);
            }

            //create VariableSymbol for Name of Type
            VariableSymbol typeName = new VariableSymbol(cd.getMCType().
              printType(MCSimpleGenericTypesMill.mcSimpleGenericTypesPrettyPrinter()).toLowerCase());
            typeName.setType(typeResult.get());
            typeName.setIsReadOnly(true);
            cd.getEnclosingScope().add(typeName);
          }
        }
      }
    }
  }

  @Override
  public void visit(ASTOCLMethodSignature node) {
    String typeName = Names.getQualifier(node.getMethodName().getQName());
    Optional<TypeSymbol> type = node.getEnclosingScope().resolveType(typeName);
    if (type.isPresent()) {
      for (VariableSymbol var : type.get().getVariableList()) {
        node.getEnclosingScope().add(var);
      }
    }

    if (node.isPresentMCReturnType()) {
      //create VariableSymbol for result of method
      final Optional<SymTypeExpression> typeResult;
      if (node.isPresentMCReturnType()) {
        ASTMCReturnType returnType = node.getMCReturnType();
        if (returnType.isPresentMCVoidType()) {
          returnType.getMCVoidType().setEnclosingScope(node.getEnclosingScope());
          returnType.getMCVoidType().accept(getTraverser());
          typeResult = Optional.empty();
        }
        else {
          returnType.getMCType().setEnclosingScope(node.getEnclosingScope());
          returnType.getMCType().accept(getTraverser());
          returnType.getMCType().accept(typeVisitor.getTraverser());
          typeResult = typeVisitor.getResult();
        }
      }
      else {
        // method has no explicit return type
        typeResult = Optional.empty();
      }
      if (typeResult.isPresent()) {
        VariableSymbol result = new VariableSymbol("result");
        result.setType(typeResult.get());
        result.setIsReadOnly(true);
        node.getEnclosingScope().add(result);
      }
    }
  }

  @Override
  public void endVisit(VariableSymbol var) {
    //CompleterUtil.visit(var, imports, packageDeclaration);
  }

  @Override
  public void endVisit(TypeSymbol type) {
    //CompleterUtil.visit(type, imports, packageDeclaration);
  }

  @Override
  public void endVisit(FunctionSymbol function) {
    //CompleterUtil.visit(function, imports, packageDeclaration);
  }
}