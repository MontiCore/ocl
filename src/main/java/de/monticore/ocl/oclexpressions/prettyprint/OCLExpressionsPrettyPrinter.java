// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.oclexpressions.prettyprint;

import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.prettyprint.CommentPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symbols.basicsymbols._ast.ASTVariable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OCLExpressionsPrettyPrinter implements OCLExpressionsHandler {

  protected OCLExpressionsTraverser traverser;

  protected IndentPrinter printer;

  public OCLExpressionsPrettyPrinter(IndentPrinter printer) {
    this.printer = printer;
  }

  @Override
  public void handle(ASTInDeclaration node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if (node.isPresentMCType()) {
      node.getMCType().accept(getTraverser());
      getPrinter().print(" ");
    }

    List<ASTInDeclarationVariable> variables = node.getInDeclarationVariableList();
    List<String> variableNames = new ArrayList<>();
    for (ASTVariable var : variables) {
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
      node.getExpression().accept(getTraverser());
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTImpliesExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeft().accept(getTraverser());
    getPrinter().print(" implies ");
    node.getRight().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTForallExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("forall ");
    printInDeclarationList(node.getInDeclarationList());
    getPrinter().println(": ");
    getPrinter().indent();
    node.getExpression().accept(getTraverser());
    if (!getPrinter().isStartOfLine()) {
      getPrinter().println();
    }
    getPrinter().unindent();
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTExistsExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("exists ");
    printInDeclarationList(node.getInDeclarationList());
    getPrinter().println(": ");
    getPrinter().indent();
    node.getExpression().accept(getTraverser());
    if (!getPrinter().isStartOfLine()) {
      getPrinter().println();
    }
    getPrinter().unindent();
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTAnyExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("any ");
    node.getExpression().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTLetinExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().println("let");

    getPrinter().indent();
    for (int i = 0; i < node.getOCLVariableDeclarationList().size(); i++) {
      node.getOCLVariableDeclaration(i).accept(getTraverser());
      if (i < node.getOCLVariableDeclarationList().size() - 1) {
        getPrinter().println(";");
      } else {
        getPrinter().println();
      }
    }
    getPrinter().unindent();

    getPrinter().println("in");

    getPrinter().indent();
    node.getExpression().accept(getTraverser());
    getPrinter().println();
    getPrinter().unindent();

    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLVariableDeclaration node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if (node.isPresentMCType()) {
      node.getMCType().accept(getTraverser());
      getPrinter().print(" ");
    }
    for (int i = 0; i < node.getDimList().size(); i++) {
      getPrinter().print("[]");
      getPrinter().print(" ");
    }
    getPrinter().print(node.getName());
    if (node.isPresentExpression()) {
      getPrinter().print(" = ");
      node.getExpression().accept(getTraverser());
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTIterateExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("iterate { ");
    getPrinter().println();
    getPrinter().indent();
    node.getIteration().accept(getTraverser());
    getPrinter().println("; ");
    node.getInit().accept(getTraverser());
    getPrinter().println(": ");
    getPrinter().print(node.getName() + " = ");
    node.getValue().accept(getTraverser());
    getPrinter().println();
    getPrinter().unindent();
    getPrinter().print("}");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTIfThenElseExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("if ");
    node.getCondition().accept(getTraverser());
    getPrinter().print(" then ");
    node.getThenExpression().accept(getTraverser());
    getPrinter().print(" else ");
    node.getElseExpression().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTTypeCastExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("(");
    node.getMCType().accept(getTraverser());
    getPrinter().print(")");
    node.getExpression().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLArrayQualification node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getTraverser());
    for (int i = 0; i < node.getArgumentsList().size(); i++) {
      getPrinter().print("[");
      node.getArguments(i).accept(getTraverser());
      getPrinter().print("]");
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLAtPreQualification node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getTraverser());
    getPrinter().print("@pre");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLTransitiveQualification node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getTraverser());
    getPrinter().print("**");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTEquivalentExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeft().accept(getTraverser());
    getPrinter().print("<=>");
    node.getRight().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTTypeIfExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("typeif ");
    getPrinter().print(node.getName());
    getPrinter().print(" instanceof ");
    node.getMCType().accept(getTraverser());
    getPrinter().print(" then ");
    node.getThenExpression().accept(getTraverser());
    getPrinter().print(" else ");
    node.getElseExpression().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTInstanceOfExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getTraverser());
    getPrinter().print(" instanceof ");
    node.getMCType().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  protected void printInDeclarationList(List<ASTInDeclaration> list) {
    for (int i = 0; i < list.size(); i++) {
      list.get(i).accept(getTraverser());
      if (i < list.size() - 1) {
        getPrinter().print(", ");
      }
    }
  }

  /* ============================================================ */
  /* ======================= GENERATED CODE ===================== */
  /* ============================================================ */

  public IndentPrinter getPrinter() {
    return this.printer;
  }

  public void setTraverser(OCLExpressionsTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public OCLExpressionsTraverser getTraverser() {
    return traverser;
  }
}
