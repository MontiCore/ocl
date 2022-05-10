/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import com.google.common.base.Preconditions;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.oclexpressions._ast.ASTAnyExpression;
import de.monticore.ocl.oclexpressions._ast.ASTEquivalentExpression;
import de.monticore.ocl.oclexpressions._ast.ASTExistsExpression;
import de.monticore.ocl.oclexpressions._ast.ASTForallExpression;
import de.monticore.ocl.oclexpressions._ast.ASTIfThenElseExpression;
import de.monticore.ocl.oclexpressions._ast.ASTImpliesExpression;
import de.monticore.ocl.oclexpressions._ast.ASTInDeclaration;
import de.monticore.ocl.oclexpressions._ast.ASTInDeclarationVariable;
import de.monticore.ocl.oclexpressions._ast.ASTInstanceOfExpression;
import de.monticore.ocl.oclexpressions._ast.ASTIterateExpression;
import de.monticore.ocl.oclexpressions._ast.ASTLetinExpression;
import de.monticore.ocl.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.ocl.oclexpressions._ast.ASTTypeCastExpression;
import de.monticore.ocl.oclexpressions._ast.ASTTypeIfExpression;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.ocl.types.check.OCLTypeCalculator;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types.check.TypeCheckResult;
import de.se_rwth.commons.logging.Log;

import java.util.List;

import static de.monticore.types.check.SymTypeConstant.box;

public class OCLExpressionsPrinter extends AbstractPrinter implements OCLExpressionsHandler,
    OCLExpressionsVisitor2 {

  protected OCLExpressionsTraverser traverser;

  protected OCLTypeCalculator typeCalculator;

  protected IndentPrinter printer;

  public OCLExpressionsPrinter(IndentPrinter printer, VariableNaming naming,
      OCLTypeCalculator typeCalculator) {
    Preconditions.checkNotNull(printer);
    Preconditions.checkNotNull(naming);
    Preconditions.checkNotNull(typeCalculator);
    this.printer = printer;
    this.naming = naming;
    this.typeCalculator = typeCalculator;
  }

  @Override
  public OCLExpressionsTraverser getTraverser() {
    return this.traverser;
  }

  @Override
  public void setTraverser(OCLExpressionsTraverser traverser) {
    Preconditions.checkNotNull(traverser);
    this.traverser = traverser;
  }

  protected OCLTypeCalculator getTypeCalculator() {
    return typeCalculator;
  }

  public IndentPrinter getPrinter() {
    return this.printer;
  }

  @Override
  public void handle(ASTIfThenElseExpression node) {
    printExpressionBeginLambda(node);
    TypeCheckResult type = this.getTypeCalculator().deriveType(node);
    if (!type.isPresentCurrentResult()) {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
    }
    else {
      this.getPrinter().print(type.getCurrentResult().getTypeInfo().getFullName());
      this.getPrinter().print(" ");
    }
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().println(";");
    this.getPrinter().print("if(");
    node.getCondition().accept(this.getTraverser());
    this.getPrinter().println(") {");
    this.getPrinter().indent();
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" = ");
    node.getThenExpression().accept(getTraverser());
    this.getPrinter().println(";");
    this.getPrinter().unindent();
    this.getPrinter().println("} else {");
    this.getPrinter().indent();
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" = ");
    node.getElseExpression().accept(getTraverser());
    this.getPrinter().println(";");
    this.getPrinter().unindent();
    this.getPrinter().println("}");
    this.getPrinter().print("return ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println(";");
    printExpressionEndLambda(node);
  }

  @Override
  public void handle(ASTImpliesExpression node) {
    this.getPrinter().print("!(");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(" || ");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(")");
  }

  @Override
  public void handle(ASTForallExpression node) {
    printExpressionBeginLambda(node);
    this.getPrinter().print("Boolean ");
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().println(" = true;");
    node.getInDeclarationList().forEach(dec -> dec.accept(getTraverser()));
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" &= ");
    node.getExpression().accept(getTraverser());
    this.getPrinter().println(";");
    OCLExpressionsTraverser endTraverser = OCLMill.traverser();
    endTraverser.setOCLExpressionsHandler(new EndingBracketPrinter(this.getPrinter()));
    node.getInDeclarationList().forEach(dec -> dec.accept(endTraverser));
    this.getPrinter().print("return ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println(";");
    printExpressionEndLambda(node);
  }

  @Override
  public void handle(ASTExistsExpression node) {
    printExpressionBeginLambda(node);
    this.getPrinter().print("Boolean ");
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().println(" = false;");
    node.getInDeclarationList().forEach(dec -> dec.accept(getTraverser()));
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" |= ");
    node.getExpression().accept(getTraverser());
    this.getPrinter().println(";");
    OCLExpressionsTraverser endTraverser = OCLMill.traverser();
    endTraverser.setOCLExpressionsHandler(new EndingBracketPrinter(this.getPrinter()));
    node.getInDeclarationList().forEach(dec -> dec.accept(endTraverser));
    this.getPrinter().print("return ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println(";");
    printExpressionEndLambda(node);
  }

  @Override
  public void handle(ASTLetinExpression node) {
    printExpressionBeginLambda(node);
    node.getOCLVariableDeclarationList().forEach(dec -> dec.accept(getTraverser()));
    this.getPrinter().print("return ");
    node.getExpression().accept(getTraverser());
    this.getPrinter().println(";");
    printExpressionEndLambda(node);
  }

  @Override
  public void handle(ASTIterateExpression node) {
    //todo
  }

  @Override
  public void handle(ASTTypeCastExpression node) {
    this.getPrinter().print("((");
    node.getMCType().accept(getTraverser());
    this.getPrinter().print(") ");
    node.getExpression().accept(getTraverser());
    this.getPrinter().print(")");
  }

  @Override
  public void handle(ASTEquivalentExpression node) {
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".equals(");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(")");
  }

  @Override
  public void handle(ASTInDeclaration node) {
    SymTypeExpression innerType = null;
    if (node.isPresentMCType()) {
      TypeCheckResult type = this.getTypeCalculator().synthesizeType(node.getMCType());
      if (type.isPresentCurrentResult()) {
        innerType = type.getCurrentResult();
      }
    }
    else if (node.isPresentExpression()) {
      TypeCheckResult type = this.getTypeCalculator().deriveType(node.getExpression());
      if (type.isPresentCurrentResult()
          && type.getCurrentResult().isGenericType()
          && ((SymTypeOfGenerics) type.getCurrentResult()).sizeArguments() == 1) {
        innerType = ((SymTypeOfGenerics) type.getCurrentResult()).getArgument(0);
      }
    }
    if (innerType == null) {
      Log.error(INNER_TYPE_NOT_DERIVED_ERROR, node.get_SourcePositionStart());
    }
    if (node.isPresentExpression()) {
      for (ASTInDeclarationVariable var : node.getInDeclarationVariableList()) {
        this.getPrinter().print("for(");
        this.getPrinter().print(innerType.getTypeInfo().getFullName());
        this.getPrinter().print(" ");
        this.getPrinter().print(var.getName());
        this.getPrinter().print(" : ");
        node.getExpression().accept(getTraverser());
        this.getPrinter().println(") {");
        this.getPrinter().indent();
      }
    }
    else {
      //ToDo
    }
  }

  @Override
  public void handle(ASTOCLVariableDeclaration node) {
    this.getPrinter().print("final ");
    if (node.isPresentMCType()) {
      node.getMCType().accept(getTraverser());
    }
    else if (node.isPresentExpression()) {
      TypeCheckResult type = this.getTypeCalculator().deriveType(node.getExpression());
      if (type.isPresentCurrentResult()) {
        this.getPrinter().print(type.getCurrentResult().getTypeInfo().getFullName());
      }
      else {
        Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
      }
    }
    this.getPrinter().print(" ");
    this.getPrinter().print(node.getName());
    if (node.isPresentExpression()) {
      this.getPrinter().print(" = ");
      node.getExpression().accept(this.getTraverser());
    }
    this.getPrinter().println(";");
  }

  @Override
  public void handle(ASTTypeIfExpression node) {
    //todo
  }

  @Override
  public void handle(ASTInstanceOfExpression node) {
    this.getPrinter().print("(");
    node.getExpression().accept(this.getTraverser());
    this.getPrinter().print(" instanceof ");
    node.getMCType().accept(this.getTraverser());
    this.getPrinter().print(")");
  }

  @Override
  public void handle(ASTAnyExpression node) {
    TypeCheckResult type = this.getTypeCalculator().deriveType(node.getExpression());
    if (type.isPresentCurrentResult() && type.getCurrentResult().isObjectType()) {
      List<TypeVarSymbol> typeVarSymbols = type.getCurrentResult().getTypeInfo()
          .getTypeParameterList();
      if (typeVarSymbols.size() != 1) {
        Log.error("0xFF058 any-expression requires a container (e.g. List<>)",
            node.get_SourcePositionStart());
      }
      this.getPrinter().print("(");
      node.getExpression().accept(getTraverser());
      this.getPrinter().print(").get(0)");
    }
    else {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
    }
  }

  protected void printExpressionBeginLambda(ASTExpression node) {
    TypeCheckResult type = this.getTypeCalculator().deriveType(node);
    if (!type.isPresentCurrentResult()) {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
      return;
    }
    this.getPrinter().print("((Supplier<");
    this.getPrinter().print(box(type.getCurrentResult().getTypeInfo().getFullName()));
    this.getPrinter().println(">)()->{");
  }

  protected void printExpressionEndLambda(ASTExpression node) {
    this.getPrinter().print("})).get()");
  }

}
