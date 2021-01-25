package de.monticore.ocl.setexpressions.prettyprint;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsHandler;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsTraverser;
import de.monticore.prettyprint.CommentPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;

public class SetExpressionsPrettyPrinter
  implements SetExpressionsHandler {

  protected SetExpressionsTraverser traverser;

  protected IndentPrinter printer;

  public SetExpressionsPrettyPrinter(IndentPrinter printer) {
    this.printer = printer;
  }

  @Override
  public void handle(ASTSetInExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getElem().accept(getTraverser());
    getPrinter().print(" isin ");
    node.getSet().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTUnionExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeft().accept(getTraverser());
    getPrinter().print(" union ");
    node.getRight().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTIntersectionExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeft().accept(getTraverser());
    getPrinter().print(" intersect ");
    node.getRight().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetUnionExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("union ");
    node.getSet().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetIntersectionExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("intersect ");
    node.getSet().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetAndExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("setand ");
    node.getSet().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetOrExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("setor ");
    node.getSet().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetVariableDeclaration node) {
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
  public void handle(ASTSetComprehension node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if (node.isPresentMCType()) {
      node.getMCType().accept(getTraverser());
      getPrinter().print(" ");
    }
    getPrinter().print("{");
    node.getLeft().accept(getTraverser());
    getPrinter().print(" | ");
    for (ASTSetComprehensionItem setComprehensionItem : node.getSetComprehensionItemList()) {
      setComprehensionItem.accept(getTraverser());
      if (!node.getSetComprehensionItemList().get(
        node.getSetComprehensionItemList().size() - 1).equals(setComprehensionItem)) {
        getPrinter().print(", ");
      }
    }
    getPrinter().print("}");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetComprehensionItem node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if (node.isPresentExpression()) {
      node.getExpression().accept(getTraverser());
    }
    else if (node.isPresentSetVariableDeclaration()) {
      node.getSetVariableDeclaration().accept(getTraverser());
    }
    else if (node.isPresentGeneratorDeclaration()) {
      node.getGeneratorDeclaration().accept(getTraverser());
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTGeneratorDeclaration node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if (node.isPresentMCType()) {
      node.getMCType().accept(getTraverser());
      getPrinter().print(" ");
    }
    getPrinter().print(node.getName());
    getPrinter().print(" in ");
    node.getExpression().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetEnumeration node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if (node.isPresentMCType()) {
      node.getMCType().accept(getTraverser());
      getPrinter().print(" ");
    }
    getPrinter().print("{");
    for (ASTSetCollectionItem setCollectionItem : node.getSetCollectionItemList()) {
      setCollectionItem.accept(getTraverser());
      if (!node.getSetCollectionItemList().get(
        node.getSetCollectionItemList().size() - 1).equals(setCollectionItem)) {
        getPrinter().print(", ");
      }
    }
    getPrinter().print("}");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetValueItem node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    for (ASTExpression expression : node.getExpressionList()) {
      expression.accept(getTraverser());
      if (!node.getExpressionList().get(
        node.getExpressionList().size() - 1).equals(expression)) {
        getPrinter().print(", ");
      }
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetValueRange node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLowerBound().accept(getTraverser());
    getPrinter().print(" .. ");
    node.getUpperBound().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /* ============================================================ */
  /* ======================= GENERATED CODE ===================== */
  /* ============================================================ */

  public IndentPrinter getPrinter() {
    return printer;
  }

  public void setPrinter(IndentPrinter printer) {
    this.printer = printer;
  }

  public void setTraverser(SetExpressionsTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public SetExpressionsTraverser getTraverser() {
    return traverser;
  }
}
