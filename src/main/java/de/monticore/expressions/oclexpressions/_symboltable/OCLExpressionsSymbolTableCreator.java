/* (c) https://github.com/MontiCore/monticore */

package de.monticore.expressions.oclexpressions._symboltable;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.expressions.oclexpressions._ast.*;
import de.monticore.literals.mcliteralsbasis._ast.ASTLiteral;
import de.monticore.types.check.DefsTypeBasic;
import de.monticore.types.check.DeriveSymTypeOfOCLExpressions;
import de.monticore.types.check.ITypesCalculator;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.typesymbols._symboltable.FieldSymbol;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class OCLExpressionsSymbolTableCreator
    extends OCLExpressionsSymbolTableCreatorTOP {
  DeriveSymTypeOfOCLExpressions typeVisitor;

  public OCLExpressionsSymbolTableCreator(IOCLExpressionsScope enclosingScope) {
    super(enclosingScope);
  }

  public OCLExpressionsSymbolTableCreator(Deque<? extends IOCLExpressionsScope> scopeStack) {
    super(scopeStack);
  }

  public void setTypeVisitor(DeriveSymTypeOfOCLExpressions typesCalculator) {
    if (typesCalculator != null) {
      this.typeVisitor = typesCalculator;
    }
    else {
      Log.error("0xA3201 The typesVisitor has to be set");
    }
  }

  @Override
  public OCLExpressionsArtifactScope createFromAST(ASTExpression rootNode) {
    if (typeVisitor == null) {
      Log.error("0xA3202 The typeVisitor has to be set");
      return null;
    }

    return super.createFromAST(rootNode);
  }

  @Override
  public void endVisit(ASTOCLTypeIfExpression node) {
    if (!(node.getCondition() instanceof ASTOCLInstanceOfExpression)) {
      Log.error("0xA3210 The type of the condition of the expression of the OCLTypeIfExpression has to be ASTOCLInstanceOfExpression, but was " + node.getCondition().getClass().getName());
      return;
    }

    final ASTOCLInstanceOfExpression condition = (ASTOCLInstanceOfExpression) node.getCondition();
    String varName = getVarNameOfExpression(condition.getLeft());

    typeVisitor.setScope(node.getEnclosingScope());
    condition.getOCLExtType().accept(typeVisitor.getRealThis());
    if (((ITypesCalculator) typeVisitor).getLastResult().isPresentLast()) {
      final FieldSymbol field = DefsTypeBasic.field(varName, ((ITypesCalculator) typeVisitor).getLastResult().getLast());
      DefsTypeBasic.add2scope(node.getEnclosingScope(), field);
    }
    else {
      Log.error("0xA3211 The type of the OCLExtType of the condition of the OCLTypeIfExpression could not be calculated");
      return;
    }

    super.endVisit(node);
  }

  @Override
  public void endVisit(ASTOCLIterateExpression node) {
    if (!handleOCLInExpressions(node.getSpannedScope(), node.getOCLInExpressionList(), "OCLIterateExpression")) {
      return;
    }

    typeVisitor.setScope(node.getInit().getEnclosingScope());
    node.getInit().accept(typeVisitor.getRealThis());

    super.endVisit(node);
  }

  @Override
  public void endVisit(ASTOCLComprehensionExpressionStyle node) {
    for (ASTOCLComprehensionItem oclComprehensionItem : node.getOCLComprehensionItemList()) {
      if (oclComprehensionItem.isPresentGenerator()) {
        if (!handleOCLInExpressions(node.getSpannedScope(), Collections.singletonList(oclComprehensionItem.getGenerator()), "OCLComprehensionExpressionStyle")) {
          return;
        }
      }
      else if (oclComprehensionItem.isPresentDeclaration()) {
        typeVisitor.setScope(oclComprehensionItem.getDeclaration().getEnclosingScope());
        oclComprehensionItem.getDeclaration().accept(typeVisitor.getRealThis());
      } // we don't need to handle the filter here, because it cant define anything
    }

    super.endVisit(node);
  }

  @Override
  public void endVisit(ASTOCLForallExpression node) {
    if (!handleOCLInExpressions(node.getSpannedScope(), node.getOCLInExpressionList(), "OCLForallExpression")) {
      return;
    }

    typeVisitor.setScope(node.getExpression().getEnclosingScope());
    node.getExpression().accept(typeVisitor.getRealThis());

    if (!typeVisitor.getLastResult().isPresentLast()) {
      Log.error("0xA3230 The type of the Expression of the OCLForallExpression could not be calculated");
      return;
    }

    super.endVisit(node);
  }

  @Override
  public void endVisit(ASTOCLExistsExpression node) {
    if (!handleOCLInExpressions(node.getSpannedScope(), node.getOCLInExpressionList(), "OCLExistsExpression")) {
      return;
    }

    typeVisitor.setScope(node.getExpression().getEnclosingScope());
    node.getExpression().accept(typeVisitor.getRealThis());

    if (!typeVisitor.getLastResult().isPresentLast()) {
      Log.error("0xA3230 The type of the Expression of the OCLExistsExpression could not be calculated");
      return;
    }

    super.endVisit(node);
  }

  @Override
  public void endVisit(ASTOCLVariableDeclaration node) {
    if (node.isPresentOCLExtType()) {
      typeVisitor.setScope(node.getOCLExtType().getEnclosingScope());
      node.getOCLExtType().accept(typeVisitor.getRealThis());
    }
    else {
      typeVisitor.setScope(node.getValue().getEnclosingScope());
      node.getValue().accept(typeVisitor.getRealThis());
    }

    if (!typeVisitor.getLastResult().isPresentLast()) {
      Log.error("0xA3240 The type of the OCLDeclaration of the OCLDeclaration could not be calculated");
      return;
    }

    final SymTypeExpression type = typeVisitor.getLastResult().getLast();
    final List<String> varNameList = node.getNameList();

    varNameList.stream().map(name -> DefsTypeBasic.field(name, type)).forEach(f -> DefsTypeBasic.add2scope(node.getEnclosingScope(), f));

    super.endVisit(node);
  }

  public FieldSymbol handleParamDeclaration(ASTOCLParamDeclaration param) {
    final String paramName = param.getParam();
    typeVisitor.setScope(param.getOCLExtType().getEnclosingScope());
    param.getOCLExtType().accept(typeVisitor.getRealThis());
    if (!typeVisitor.getLastResult().isPresentLast()) {
      Log.error("0xA3250 The type of the OCLDeclaration of the OCLMethodDeclaration could not be calculated");
      return null;
    }
    return DefsTypeBasic.field(paramName, typeVisitor.getLastResult().getLast());
  }

  public List<FieldSymbol> registerFields(List<ASTOCLParamDeclaration> params, IOCLExpressionsScope enclosingScope) {
    List<FieldSymbol> fields = new ArrayList<>();
    for (ASTOCLParamDeclaration param : params) {
      final FieldSymbol fieldSymbol = handleParamDeclaration(param);
      if (fieldSymbol == null) {
        return fields;
      }
      fields.add(fieldSymbol);
    }

    fields.forEach(f -> DefsTypeBasic.add2scope(enclosingScope, f));
    return fields;
  }

  @Override
  public void endVisit(ASTOCLMethodDeclaration node) {
    String methodName = node.getName();
    final List<FieldSymbol> fields = registerFields(node.getParamList(), node.getEnclosingScope());

    typeVisitor.setScope(node.getValue().getEnclosingScope());
    node.getValue().accept(typeVisitor.getRealThis());
    if (!typeVisitor.getLastResult().isPresentLast()) {
      Log.error("0xA3251 The type of the value of the OCLMethodDeclaration could not be calculated");
      return;
    }

    SymTypeExpression returnType = typeVisitor.getLastResult().getLast();
    DefsTypeBasic.add2scope(node.getEnclosingScope(), DefsTypeBasic.method(methodName, returnType, fields));

    super.endVisit(node);
  }

  private String getVarNameOfExpression(ASTExpression expr) {
    // TODO SVa: find correct way to get name of variable
    // TODO SVa: should this be possible with a complex expression? -> write expression in scope?
    if (expr instanceof ASTLiteral) {
      Log.error("0xA3240 The type of the Expression cannot be a Literal");
      return null;
    }

    if (expr instanceof ASTNameExpression) {
      return ((ASTNameExpression) expr).getName();
    }
    else {
      Log.error("0xA3241 The type of the Expression has to be a NameExpression");
      return null;
      /*final OCLExpressionsPrettyPrinter prettyPrinter = new OCLExpressionsPrettyPrinter(new IndentPrinter());
      expr.accept(prettyPrinter);
      String varName = prettyPrinter.getPrinter().getContent();
      prettyPrinter.getPrinter().clearBuffer();
      return varName;*/
    }
  }

  private boolean handleOCLInExpressions(IOCLExpressionsScope spannedScope, List<ASTOCLInExpression> exprList, String astType) {
    for (ASTOCLInExpression expr : exprList) {
      final List<String> varNameList = expr.getVarNameList();

      if (expr.isPresentOCLExtType()) {
        typeVisitor.setScope(expr.getEnclosingScope());
        expr.getOCLExtType().accept(typeVisitor.getRealThis());
      }
      else {
        this.typeVisitor.setScope(expr.getEnclosingScope());
        expr.accept(typeVisitor.getRealThis());
      }

      if (typeVisitor.getLastResult().isPresentLast()) {
        final SymTypeExpression last = typeVisitor.getLastResult().getLast();
        varNameList.stream().map(name -> DefsTypeBasic.field(name, last)).forEach(f -> DefsTypeBasic.add2scope(spannedScope, f));
      }
      else {
        Log.error("0xA32A0 The type of the Expression of the OCLInExpression of the " + astType + " could not be calculated");
        return false;
      }
    }
    return true;
  }
}
