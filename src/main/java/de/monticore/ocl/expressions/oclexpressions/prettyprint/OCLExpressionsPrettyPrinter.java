// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.expressions.oclexpressions.prettyprint;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.prettyprint.ExpressionsBasisPrettyPrinter;
import de.monticore.ocl.expressions.oclexpressions._visitor.OCLExpressionsVisitor;
import de.monticore.ocl.expressions.oclexpressions._ast.*;
import de.monticore.prettyprint.CommentPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTLocalVariableDeclaration;
import de.monticore.symbols.basicsymbols._ast.ASTVariable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OCLExpressionsPrettyPrinter extends ExpressionsBasisPrettyPrinter
  implements OCLExpressionsVisitor {

  protected OCLExpressionsVisitor realThis;

  public OCLExpressionsPrettyPrinter(IndentPrinter printer) {
    super(printer);
    realThis = this;
  }

  @Override
  public void handle(ASTInDeclaration node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if (node.isPresentMCType())
      node.getMCType().accept(getRealThis());

    List<ASTVariable> variables = node.getVariableList();
    List<String> variableNames = new ArrayList<>();
    for (ASTVariable var : variables){
      variableNames.add(var.getName());
    }
    Iterator<String> iter = variableNames.iterator();
    getPrinter().print(iter.next());
    while (iter.hasNext()) {
      getPrinter().print(", ");
      getPrinter().print(iter.next());
    }

    if (node.isPresentExpression()) {
      getPrinter().print(" in ");
      node.getExpression().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTImpliesExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeft().accept(getRealThis());
    getPrinter().print(" implies ");
    node.getRight().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTForallExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("forall ");
    node.getInDeclarationList().forEach(e -> e.accept(getRealThis()));

    getPrinter().print(":");
    node.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTExistsExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("exists ");
    node.getInDeclarationList().forEach(e -> e.accept(getRealThis()));

    getPrinter().print(":");
    node.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTAnyExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("any ");
    node.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTLetinExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("let ");
    /*for (ASTLocalVariableDeclaration ast : node.getOCLVariableDeclarationList()) {
      ast.accept(getRealThis());
      getPrinter().print("; ");
    }*/
    getPrinter().print("in ");
    node.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTIterateExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("iterate { ");
    node.getIteration().accept(getRealThis());
    getPrinter().print("; ");
    node.getInit().accept(getRealThis());
    getPrinter().print(" : ");
    getPrinter().print(node.getName() + " = ");
    node.getValue().accept(getRealThis());
    getPrinter().print(" }");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTIfThenElseExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("if ");
    node.getCondition().accept(getRealThis());
    node.getThenExpression().accept(getRealThis());
    node.getElseExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTParenthizedExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("(");
    node.getExpression().accept(getRealThis());
    getPrinter().print(")");
    if (node.isPresentQualification()) {
      getPrinter().print(".");
      node.getQualification().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTTypeCastExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("(");
    node.getMCType().accept(getRealThis());
    getPrinter().print(")");
    node.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLArrayQualification node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    for (ASTExpression astExpression : node.getArgumentsList()) {
      getPrinter().print("[");
      astExpression.accept(getRealThis());
      getPrinter().print("]");
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLArgumentQualification node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("(");
    for (int i = 0; i < node.getExpressionList().size(); i++) {
      if (i != 0) {
        getPrinter().print(", ");
      }
      node.getExpression(i).accept(getRealThis());
    }
    getPrinter().print(")");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLAtPreQualification node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("@pre");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLTransitiveQualification node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("**");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLQualifiedPrimary node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    String qualifiedName = String.join(".", node.getMCQualifiedName().getPartsList());
    getPrinter().print(qualifiedName + " ");
    if (node.isPresentPostfix()) {
      node.getPostfix().accept(getRealThis());
    }
    if (node.isPresentOCLQualifiedPrimary()) {
      getPrinter().print(".");
      node.getOCLQualifiedPrimary().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTEquivalentExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeft().accept(getRealThis());
    getPrinter().print("<=>");
    node.getRight().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  public IndentPrinter getPrinter() {
    return this.printer;
  }

  public String prettyprint(ASTExpression node) {
    getPrinter().clearBuffer();
    node.accept(getRealThis());
    return getPrinter().getContent();
  }

  @Override
  public void setRealThis(OCLExpressionsVisitor realThis) {
    this.realThis = realThis;
  }

  @Override
  public OCLExpressionsVisitor getRealThis() {
    return realThis;
  }
}
