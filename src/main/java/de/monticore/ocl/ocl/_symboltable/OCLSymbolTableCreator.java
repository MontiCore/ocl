/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.ocl._symboltable;

import de.monticore.expressions.expressionsbasis._symboltable.IExpressionsBasisScope;
import de.monticore.ocl.expressions.oclexpressions._ast.ASTLetinExpression;
import de.monticore.ocl.expressions.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.ocl.ocl._ast.ASTOCLParamDeclaration;
import de.monticore.ocl.expressions.oclexpressions._symboltable.IOCLExpressionsScope;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTLocalVariableDeclaration;
import de.monticore.symbols.oosymbols._symboltable.IOOSymbolsScope;
import de.monticore.symboltable.ImportStatement;
import de.monticore.types.check.DefsTypeBasic;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class OCLSymbolTableCreator extends OCLSymbolTableCreatorTOP {

  DeriveSymTypeOfOCLCombineExpressions typeVisitor;

  public OCLSymbolTableCreator(){super(); }

  public OCLSymbolTableCreator(IOCLScope enclosingScope) {
    super(enclosingScope);
  }

  public OCLSymbolTableCreator(Deque<? extends IOCLScope> scopeStack) {
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
  public void visit(ASTOCLMethodSignature node) {
    super.visit(node);
    registerFields(node.getParamsList(), node.getEnclosingScope());
  }

  @Override
  public void visit(ASTOCLConstructorSignature node) {
    super.visit(node);
    registerFields(node.getParamsList(), node.getEnclosingScope());
  }

  @Override
  public void visit(ASTOCLInvariant node) {
    super.visit(node);

    if (node.isEmptyParams()) {
      registerFields(node.getParamsList(), node.getEnclosingScope());
    }
  }

  @Override
  public void endVisit(ASTOCLContextDefinition node) {
    if (node.isPresentMCType()) {
      // TODO `this` is now the extType
    }
    else if (node.isPresentExpression()) {
      ASTLetinExpression inExpression = (ASTLetinExpression) node.getExpression();

      if (!handleOCLInExpressions(node.getEnclosingScope(), Collections.singletonList(inExpression),
        "OCLContextDefinition")) {
        return;
      }
    }

    super.endVisit(node);
  }

  public FieldSymbol handleParamDeclaration(ASTOCLParamDeclaration param) {
    final String paramName = param.getName();
    typeVisitor.setScope((IExpressionsBasisScope) param.getMCType().getEnclosingScope());
    param.getMCType().accept(typeVisitor.getRealThis());
    if (!typeVisitor.getLastResult().isPresentCurrentResult()) {
      Log.error(
        "0xA3250 The type of the OCLDeclaration of the OCLMethodDeclaration could not be calculated");
      return null;
    }
    return DefsTypeBasic.field(paramName, typeVisitor.getLastResult().getCurrentResult());
  }

  public void registerFields(List<ASTOCLParamDeclaration> params,
    IOCLExpressionsScope enclosingScope) {
    List<FieldSymbol> fields = new ArrayList<>();
    for (ASTOCLParamDeclaration param : params) {
      final FieldSymbol fieldSymbol = handleParamDeclaration(param);
      if (fieldSymbol == null) {
        return;
      }
      fields.add(fieldSymbol);
    }
    fields.forEach(f -> DefsTypeBasic.add2scope((IOOSymbolsScope) enclosingScope, f));
  }

  private boolean handleOCLInExpressions(IOCLExpressionsScope scope, List<ASTLetinExpression> exprList,
    String astType) {
    for (ASTLetinExpression expr : exprList) {
      for (ASTOCLVariableDeclaration variable : expr.getOCLVariableDeclarationList()) {
        List<String> varNameList = new ArrayList<>();
        varNameList.add(variable.getName());

        typeVisitor.setScope(expr.getEnclosingScope());
        variable.getMCType().accept(typeVisitor.getRealThis());


        if (typeVisitor.getLastResult().isPresentCurrentResult()) {
          final SymTypeExpression last = typeVisitor.getLastResult().getCurrentResult();
          varNameList.stream().map(name -> DefsTypeBasic.field(name, last))
            .forEach(f -> DefsTypeBasic.add2scope((IOOSymbolsScope) scope, f));
        }
        else {
          Log.error("0xA32A0 The type of the Expression of the OCLInExpression of the " + astType
            + " could not be calculated");
          return false;
        }
      }
    }
    return true;
  }
}
