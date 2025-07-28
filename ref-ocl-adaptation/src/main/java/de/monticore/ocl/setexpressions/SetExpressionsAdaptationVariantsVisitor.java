package de.monticore.ocl.setexpressions;

import de.monticore.ast.ASTNode;
import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.symboltable.IScope;

import java.util.ArrayList;
import java.util.List;

public class SetExpressionsAdaptationVariantsVisitor
        extends SetExpressionsAdaptationVariantsVisitorTOP {

  @Override
  public void traverse(ASTSetInExpression node) {
    traverseForConsistentVariants(node, node.getElem(), node.getSet());
  }

  @Override
  public void traverse(ASTSetNotInExpression node) {
    traverseForConsistentVariants(node, node.getElem(), node.getSet());
  }

  @Override
  public void traverse(ASTUnionExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTIntersectionExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTSetMinusExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTSetVariableDeclaration node) {
    List<ASTNode> children = new ArrayList<>();
    if (node.isPresentMCType()) {
      children.add(node.getMCType());
    }
    if (node.isPresentExpression()) {
      children.add(node.getExpression());
    }
    traverseForConsistentVariants(node, children);
  }

  @Override
  public void traverse(ASTSetComprehension expr) {
    List<ASTNode> children = new ArrayList<>();
    if (expr.isPresentLeft()) {
      children.add(expr.getLeft());
    }
    children.addAll(expr.getSetComprehensionItemList());
    traverseForConsistentVariants(expr, children);
  }

  @Override
  public void traverse(ASTSetComprehensionItem node) {
    List<ASTNode> children = new ArrayList<>();
    if (node.isPresentExpression()) {
      children.add(node.getExpression());
    }
    if (node.isPresentSetVariableDeclaration()) {
      children.add(node.getSetVariableDeclaration());
    }
    if (node.isPresentGeneratorDeclaration()) {
      children.add(node.getGeneratorDeclaration());
    }
    traverseForConsistentVariants(node, children);
  }

  @Override
  public void traverse(ASTGeneratorDeclaration node) {
    List<ASTNode> children = new ArrayList<>();
    if (node.isPresentMCType()) {
      children.add(node.getMCType());
    }
    children.add(node.getExpression());
    traverseForConsistentVariants(node, children);
  }

  @Override
  public void traverse(ASTSetEnumeration expr) {
    traverseForConsistentVariants(expr, expr.getSetCollectionItemList());
  }

  @Override
  public void traverse(ASTSetValueRange node) {
    traverseForConsistentVariants(node, node.getLowerBound(), node.getUpperBound());
  }

  // ===========================================================
  // endVisit methods
  // ===========================================================

  @Override
  public void endVisit(ASTSetUnionExpression expr) {
    passChildVariantsUpwards(expr, expr.getSet());
  }

  @Override
  public void endVisit(ASTSetIntersectionExpression expr) {
    passChildVariantsUpwards(expr, expr.getSet());
  }

  @Override
  public void endVisit(ASTSetAndExpression expr) {
    passChildVariantsUpwards(expr, expr.getSet());
  }

  @Override
  public void endVisit(ASTSetOrExpression expr) {
    passChildVariantsUpwards(expr, expr.getSet());
  }

  @Override
  public void endVisit(ASTSetValueItem node) {
    passChildVariantsUpwards(node, node.getExpression());
  }
}
