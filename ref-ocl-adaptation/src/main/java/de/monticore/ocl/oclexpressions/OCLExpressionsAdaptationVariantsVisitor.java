package de.monticore.ocl.oclexpressions;

import de.monticore.ast.ASTNode;
import de.monticore.ocl.oclexpressions._ast.*;

import java.util.ArrayList;
import java.util.List;

public class OCLExpressionsAdaptationVariantsVisitor
        extends OCLExpressionsAdaptationVariantsVisitorTOP {

  // =========================================================
  // Traversal definition
  // =========================================================


  @Override
  public void traverse(ASTOCLVariableDeclaration node) {
    List<ASTNode> children = new ArrayList<>();
    if (node.isPresentMCType()) {
      children.add(node.getMCType());
    }
    children.add(node.getExpression());
    traverseForConsistentVariants(node, children);
  }

  @Override
  public void traverse(ASTTypeIfExpression expr) {
    traverseForConsistentVariants(expr,
            expr.getMCType(),
            expr.getThenExpression(),
            expr.getElseExpression());
  }

  @Override
  public void traverse(ASTIfThenElseExpression expr) {
    traverseForConsistentVariants(expr,
            expr.getCondition(),
            expr.getElseExpression(),
            expr.getElseExpression());
  }

  @Override
  public void traverse(ASTImpliesExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTEquivalentExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTForallExpression expr) {
    List<ASTNode> children = new ArrayList<>(expr.getInDeclarationList());
    children.add(expr.getExpression());
    traverseForConsistentVariants(expr, children);
  }

  @Override
  public void traverse(ASTExistsExpression expr) {
    List<ASTNode> children = new ArrayList<>(expr.getInDeclarationList());
    children.add(expr.getExpression());
    traverseForConsistentVariants(expr, children);
  }

  @Override
  public void traverse(ASTLetinExpression expr) {
    List<ASTNode> children = new ArrayList<>(expr.getOCLVariableDeclarationList());
    children.add(expr.getExpression());
    traverseForConsistentVariants(expr, children);
  }

  @Override
  public void traverse(ASTIterateExpression expr) {
    traverseForConsistentVariants(expr, expr.getIteration(), expr.getInit(), expr.getValue());
    // TODO Think about if we should adapt the variable here as well?
    //  we could open a whole new topic here:
    //  1. override endVisit(ASTVariable) to adapt the variable name with infix replacement if
    //     applicable.
    //  2. add a binding for the VariableSymbol to the variant (in the OCLIncarnationBindings !) -> yes, that should be possible -> internal vs. imported symbols
    //  3. binding is then used in AST adaptation visitors to change NameExpressions referencing this variable
  }

  @Override
  public void traverse(ASTInDeclaration node) {
    List<ASTNode> children = new ArrayList<>();
    if (node.isPresentMCType()) {
      children.add(node.getMCType());
    }
    children.addAll(node.getInDeclarationVariableList());
    if (node.isPresentExpression()) {
      children.add(node.getExpression());
    }
    traverseForConsistentVariants(node, children);
  }

  // =========================================================
  // endVisit methods
  // =========================================================


  @Override
  public void endVisit(ASTTypeIfThenExpression expr) {
    passChildVariantsUpwards(expr, expr.getExpression());
  }

  @Override
  public void endVisit(ASTAnyExpression expr) {
    passChildVariantsUpwards(expr, expr.getExpression());
  }

  @Override
  public void endVisit(ASTInDeclarationVariable node) {
    getVariants4Ast().addVariant(node, getAdaptationContext().createVariant());
  }

  @Override
  public void endVisit(ASTOCLAtPreQualification expr) {
    passChildVariantsUpwards(expr, expr.getExpression());
  }

  @Override
  public void endVisit(ASTOCLTransitiveQualification expr) {
    passChildVariantsUpwards(expr, expr.getExpression());
  }
}
