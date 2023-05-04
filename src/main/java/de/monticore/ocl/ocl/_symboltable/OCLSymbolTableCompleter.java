// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import de.monticore.ocl.ocl._ast.ASTOCLContextDefinition;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.ocl.ocl._ast.ASTOCLParamDeclaration;
import de.monticore.ocl.ocl._visitor.OCLHandler;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl._visitor.OCLVisitor2;
import de.monticore.ocl.types.check.OCLSynthesizer;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.basicsymbols._visitor.BasicSymbolsVisitor2;
import de.monticore.types.check.TypeCheckResult;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.monticore.types.mcbasictypes._ast.ASTMCReturnType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.logging.Log;
import java.util.List;
import java.util.Optional;

public class OCLSymbolTableCompleter implements OCLVisitor2, BasicSymbolsVisitor2, OCLHandler {
  protected static final String USED_BUT_UNDEFINED = "0xB0028: Type '%s' is used but not defined.";

  protected static final String DEFINED_MUTLIPLE_TIMES =
      "0xB0031: Type '%s' is defined more than once.";
  protected final List<ASTMCImportStatement> imports;
  protected final String packageDeclaration;
  protected OCLTraverser traverser;
  OCLSynthesizer synthesizer;

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

  public void setSynthesizer(OCLSynthesizer typesCalculator) {
    if (typesCalculator != null) {
      this.synthesizer = typesCalculator;
    } else {
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
    final TypeCheckResult typeResult = synthesizer.synthesizeType(ast.getMCType());
    if (!typeResult.isPresentResult()) {
      Log.error(
          String.format(
              "The type (%s) of the object (%s) could not be calculated",
              ast.getMCType(), ast.getName()));
    } else {
      symbol.setType(typeResult.getResult());
      symbol.setIsReadOnly(true);
    }
  }

  @Override
  public void visit(ASTOCLParamDeclaration node) {
    VariableSymbol symbol = node.getSymbol();
    initialize_OCLParamDeclaration(symbol, node);
  }

  @Override
  public void visit(ASTOCLInvariant node) {
    // check whether symbols for "this" and "super" should be introduced
    if (!node.isEmptyOCLContextDefinitions()) {
      for (ASTOCLContextDefinition cd : node.getOCLContextDefinitionList()) {
        if (cd.isPresentMCType()) {
          ASTMCType type = cd.getMCType();
          type.setEnclosingScope(cd.getEnclosingScope());
          final TypeCheckResult typeResult = synthesizer.synthesizeType(type);
          if (!typeResult.isPresentResult()) {
            Log.error(String.format("The type (%s) could not be calculated", type));
          } else {
            // create VariableSymbols for "this" and "super"
            VariableSymbol t = new VariableSymbol("this");
            t.setType(typeResult.getResult());
            t.setIsReadOnly(true);
            cd.getEnclosingScope().add(t);
            if (!typeResult.getResult().getTypeInfo().isEmptySuperTypes()) {
              VariableSymbol s = new VariableSymbol("super");
              s.setType(typeResult.getResult().getTypeInfo().getSuperClass());
              s.setIsReadOnly(true);
              cd.getEnclosingScope().add(s);
            }

            // create VariableSymbol for Name of Type
            VariableSymbol typeName = new VariableSymbol(cd.getMCType().printType().toLowerCase());
            typeName.setType(typeResult.getResult());
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
      // create VariableSymbol for result of method
      final TypeCheckResult typeResult;
      if (node.isPresentMCReturnType()) {
        ASTMCReturnType returnType = node.getMCReturnType();
        if (returnType.isPresentMCVoidType()) {
          returnType.getMCVoidType().setEnclosingScope(node.getEnclosingScope());
          returnType.getMCVoidType().accept(getTraverser());
          typeResult = new TypeCheckResult();
          typeResult.setResultAbsent();
        } else {
          returnType.getMCType().setEnclosingScope(node.getEnclosingScope());
          returnType.getMCType().accept(getTraverser());
          typeResult = synthesizer.synthesizeType(returnType.getMCType());
        }
      } else {
        // method has no explicit return type
        typeResult = new TypeCheckResult();
        typeResult.setResultAbsent();
      }
      if (typeResult.isPresentResult()) {
        VariableSymbol result = new VariableSymbol("result");
        result.setType(typeResult.getResult());
        result.setIsReadOnly(true);
        node.getEnclosingScope().add(result);
      }
    }
  }

  @Override
  public void endVisit(VariableSymbol var) {
    // CompleterUtil.visit(var, imports, packageDeclaration);
  }

  @Override
  public void endVisit(TypeSymbol type) {
    // CompleterUtil.visit(type, imports, packageDeclaration);
  }

  @Override
  public void endVisit(FunctionSymbol function) {
    // CompleterUtil.visit(function, imports, packageDeclaration);
  }
}
