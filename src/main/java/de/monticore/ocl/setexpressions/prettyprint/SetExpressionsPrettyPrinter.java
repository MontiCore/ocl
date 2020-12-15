package de.monticore.ocl.setexpressions.prettyprint;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.prettyprint.ExpressionsBasisPrettyPrinter;
import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsVisitor;
import de.monticore.prettyprint.CommentPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;

public class SetExpressionsPrettyPrinter extends ExpressionsBasisPrettyPrinter
        implements SetExpressionsVisitor {

  protected SetExpressionsVisitor realThis;

  public SetExpressionsPrettyPrinter(IndentPrinter printer) {
    super(printer);
    realThis = this;
  }

  @Override
  public void handle(ASTSetInExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getElem().accept(getRealThis());
    getPrinter().print(" in ");
    node.getSet().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTUnionExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeft().accept(getRealThis());
    getPrinter().print(" union ");
    node.getRight().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTIntersectionExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeft().accept(getRealThis());
    getPrinter().print(" intersect ");
    node.getRight().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetUnionExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("union ");
    node.getSet().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetIntersectionExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("intersect ");
    node.getSet().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetAndExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("setand ");
    node.getSet().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetOrExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("setor ");
    node.getSet().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetVariableDeclaration node){
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if(node.isPresentMCType()){
      node.getMCType().accept(getRealThis());
      getPrinter().print(" ");
    }
    for (int i = 0; i < node.getDimList().size(); i++){
      getPrinter().print("[]");
      getPrinter().print(" ");
    }
    getPrinter().print(node.getName());
    if (node.isPresentExpression()){
      getPrinter().print(" = ");
      node.getExpression().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetComprehension node){
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if(node.isPresentMCType()){
      node.getMCType().accept(getRealThis());
      getPrinter().print(" ");
    }
    getPrinter().print("{");
    node.getLeft().accept(getRealThis());
    getPrinter().print(" | ");
    for (ASTSetComprehensionItem setComprehensionItem : node.getSetComprehensionItemList()){
      setComprehensionItem.accept(getRealThis());
      if(!node.getSetComprehensionItemList().get(
              node.getSetComprehensionItemList().size() - 1).equals(setComprehensionItem)){
        getPrinter().print(", ");
      }
    }
    getPrinter().print("}");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetComprehensionItem node){
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if(node.isPresentExpression()){
      node.getExpression().accept(getRealThis());
    } else if (node.isPresentSetVariableDeclaration()) {
      node.getSetVariableDeclaration().accept(getRealThis());
    } else if(node.isPresentGeneratorDeclaration()){
      node.getGeneratorDeclaration().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTGeneratorDeclaration node){
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if(node.isPresentMCType()){
      node.getMCType().accept(getRealThis());
      getPrinter().print(" ");
    }
    getPrinter().print(node.getName());
    getPrinter().print(" from ");
    node.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetEnumeration node){
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if(node.isPresentMCType()){
      node.getMCType().accept(getRealThis());
      getPrinter().print(" ");
    }
    getPrinter().print("{");
    for (ASTSetCollectionItem setCollectionItem : node.getSetCollectionItemList()){
      setCollectionItem.accept(getRealThis());
      if(!node.getSetCollectionItemList().get(
              node.getSetCollectionItemList().size() - 1).equals(setCollectionItem)){
        getPrinter().print(", ");
      }
    }
    getPrinter().print("}");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetValueItem node){
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    for (ASTExpression expression : node.getExpressionList()){
      expression.accept(getRealThis());
      if(!node.getExpressionList().get(
              node.getExpressionList().size() - 1).equals(expression)){
        getPrinter().print(", ");
      }
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTSetValueRange node){
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLowerBound().accept(getRealThis());
    getPrinter().print(" .. ");
    node.getUpperBound().accept(getRealThis());
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
  public void setRealThis(SetExpressionsVisitor realThis) {
    this.realThis = realThis;
  }

  @Override
  public SetExpressionsVisitor getRealThis(){
    return realThis;
  }
}
