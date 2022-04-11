/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import com.google.common.base.Preconditions;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.ocl.types.check.OCLTypeCalculator;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.types.check.TypeCheckResult;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Stack;

public class OCLExpressionsPrinter extends AbstractPrinter implements OCLExpressionsHandler,
    OCLExpressionsVisitor2 {

  protected OCLExpressionsTraverser traverser;

  protected OCLTypeCalculator typeCalculator;

  protected IndentPrinter printer;

  protected Stack<Integer> javaBlockCount;

  public OCLExpressionsPrinter(IndentPrinter printer, VariableNaming naming,
      OCLTypeCalculator typeCalculator) {
    Preconditions.checkNotNull(printer);
    Preconditions.checkNotNull(naming);
    Preconditions.checkNotNull(typeCalculator);
    this.printer = printer;
    this.naming = naming;
    this.typeCalculator = typeCalculator;
    this.javaBlockCount = new Stack<>();
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

  protected Stack<Integer> getJavaBlockCount() {
    return this.javaBlockCount;
  }

  protected void pushNewJavaBlockCountFrame() {
    getJavaBlockCount().push(0);
  }

  protected void incrementJavaBlockCount() {
    getJavaBlockCount().push(getJavaBlockCount().pop() + 1);
  }

  protected Integer popCurrentJavaBlockcount() {
    return getJavaBlockCount().pop();
  }

  @Override
  public void handle(ASTInstanceOfExpression node) {
    node.getExpression().accept(getTraverser());
    this.getPrinter().print("Boolean ");
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" = ");
    this.getPrinter().print(getNaming().getName(node.getExpression()));
    this.getPrinter().print(" instanceof ");
    node.getMCType().accept(getTraverser());
    this.getPrinter().println(";");
  }

  @Override
  public void handle(ASTIfThenElseExpression node) {
    node.getCondition().accept(getTraverser());
    TypeCheckResult type = this.getTypeCalculator().deriveType(node);
    if (!type.isPresentCurrentResult()) {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
    }
    else {
      this.getPrinter().print(type.getCurrentResult().print());
      this.getPrinter().print(" ");
    }
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().println(";");

    this.getPrinter().print("if(");
    this.getPrinter().print(getNaming().getName(node.getCondition()));
    this.getPrinter().println(") {");
    this.getPrinter().indent();
    node.getThenExpression().accept(getTraverser());
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" = ");
    this.getPrinter().print(getNaming().getName(node.getThenExpression()));
    this.getPrinter().println(";");
    this.getPrinter().unindent();
    this.getPrinter().println("} else {");
    this.getPrinter().indent();
    node.getElseExpression().accept(getTraverser());
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" = ");
    this.getPrinter().print(getNaming().getName(node.getElseExpression()));
    this.getPrinter().println(";");
    this.getPrinter().unindent();
    this.getPrinter().println("}");
  }

  @Override
  public void handle(ASTImpliesExpression node) {
    node.getLeft().accept(getTraverser());
    node.getRight().accept(getTraverser());
    this.getPrinter().print("Boolean ");
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" = !");
    this.getPrinter().print(getNaming().getName(node.getLeft()));
    this.getPrinter().print(" || ");
    this.getPrinter().print(getNaming().getName(node.getRight()));
    this.getPrinter().println(";");
  }

  @Override
  public void handle(ASTForallExpression node) {
    this.getPrinter().print("Boolean ");
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().println(" = true;");

    this.pushNewJavaBlockCountFrame();
    node.getInDeclarationList().forEach(dec -> dec.accept(getTraverser()));

    node.getExpression().accept(getTraverser());
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" &= ");
    this.getPrinter().print(getNaming().getName(node.getExpression()));
    this.getPrinter().println(";");

    Integer blockCount = this.popCurrentJavaBlockcount();
    for (int i = 0; i < blockCount; i++) {
      this.getPrinter().unindent();
      this.getPrinter().println("}");
    }
  }

  @Override
  public void handle(ASTExistsExpression node) {
    this.getPrinter().print("Boolean ");
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().println(" = false;");

    this.pushNewJavaBlockCountFrame();
    node.getInDeclarationList().forEach(dec -> dec.accept(getTraverser()));
    node.getExpression().accept(getTraverser());

    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" |= ");
    this.getPrinter().print(getNaming().getName(node.getExpression()));
    this.getPrinter().println(";");

    Integer blockCount = this.popCurrentJavaBlockcount();
    for (int i = 0; i < blockCount; i++) {
      this.getPrinter().unindent();
      this.getPrinter().println("}");
    }
  }

  @Override
  public void handle(ASTLetinExpression node) {
    node.getOCLVariableDeclarationList().forEach(dec -> dec.accept(getTraverser()));
    node.getExpression().accept(getTraverser());

    TypeCheckResult type = this.getTypeCalculator().deriveType(node);
    if (!type.isPresentCurrentResult()) {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
    }
    else {
      getPrinter().print(type.getCurrentResult().print());
      getPrinter().print(" ");
    }
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" = ");
    this.getPrinter().print(getNaming().getName(node.getExpression()));
    this.getPrinter().println(";");
  }

  @Override
  public void handle(ASTIterateExpression node) {
    //todo
  }

  @Override
  public void handle(ASTTypeCastExpression node) {
    node.getExpression().accept(getTraverser());
    node.getMCType().accept(getTraverser());
    this.getPrinter().print(" ");
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" = (");
    node.getMCType().accept(getTraverser());
    this.getPrinter().print(") ");
    this.getPrinter().print(getNaming().getName(node.getExpression()));
    this.getPrinter().println(";");
  }

  @Override
  public void handle(ASTEquivalentExpression node) {
    node.getLeft().accept(getTraverser());
    node.getRight().accept(getTraverser());
    this.getPrinter().print("Boolean");
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" = ");
    this.getPrinter().print(getNaming().getName(node.getLeft()));
    this.getPrinter().print(".equals(");
    this.getPrinter().print(getNaming().getName(node.getRight()));
    this.getPrinter().println(");");
  }

  @Override
  public void handle(ASTInDeclaration node) {
    TypeCheckResult type = this.getTypeCalculator().deriveType(node.getExpression());
    if (!type.isPresentCurrentResult()) {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
    }
    if (node.isPresentExpression()) {
      node.getExpression().accept(getTraverser());
      for (ASTInDeclarationVariable var : node.getInDeclarationVariableList()) {
        this.getPrinter().print("for(");
        this.getPrinter().print(type.getCurrentResult().print());
        this.getPrinter().print(" ");
        this.getPrinter().print(var.getName());
        this.getPrinter().print(" : ");
        this.getPrinter().print(getNaming().getName(node.getExpression()));
        this.getPrinter().println(") {");
        this.getPrinter().indent();
        this.incrementJavaBlockCount();
      }
    }
    else if (node.isPresentMCType()) {
      //ToDo
    }

  }

  @Override
  public void handle(ASTOCLVariableDeclaration node) {
    if(node.isPresentExpression()){
      node.getExpression().accept(getTraverser());
    }
    if (node.isPresentMCType()) {
      node.getMCType().accept(getTraverser());
    }
    else if (node.isPresentExpression()){
      TypeCheckResult type = this.getTypeCalculator().deriveType(node.getExpression());
      if (type.isPresentCurrentResult()) {
        this.getPrinter().print(type.getCurrentResult().print());
      }
      else {
        Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
      }
    }else

    this.getPrinter().print(" ");
    this.getPrinter().print(node.getName());
    if(node.isPresentExpression()) {
      this.getPrinter().print(" = ");
      this.getPrinter().print(getNaming().getName(node.getExpression()));
    }
    this.getPrinter().println(";");
  }

  @Override
  public void handle(ASTTypeIfExpression node) {
    //todo
  }

  @Override
  public void handle(ASTAnyExpression node) {
    node.getExpression().accept(getTraverser());

    TypeCheckResult type = this.getTypeCalculator().deriveType(node.getExpression());
    if (type.isPresentCurrentResult() && type.getCurrentResult().isObjectType()) {
        List<TypeVarSymbol> typeVarSymbols = type.getCurrentResult().getTypeInfo().getTypeParameterList();
        if(typeVarSymbols.size() != 1){
          Log.error("0xFF058 any-expression requires a container (e.g. List<>)", node.get_SourcePositionStart());
        }
        this.getPrinter().print(typeVarSymbols.get(0).getName());
        this.getPrinter().print(" ");
      } else {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
     }
    // todo WIP
  }

}
