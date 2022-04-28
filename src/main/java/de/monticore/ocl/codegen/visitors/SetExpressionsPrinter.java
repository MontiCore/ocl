// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.codegen.visitors;

import com.google.common.base.Preconditions;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsHandler;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsTraverser;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsVisitor2;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.TypeCheckResult;
import de.se_rwth.commons.logging.Log;

import static de.monticore.types.check.SymTypeConstant.box;

public class SetExpressionsPrinter extends AbstractPrinter
    implements SetExpressionsHandler, SetExpressionsVisitor2 {

  protected SetExpressionsTraverser traverser;

  protected IndentPrinter printer;

  public SetExpressionsPrinter(IndentPrinter printer, VariableNaming naming) {
    Preconditions.checkNotNull(printer);
    Preconditions.checkNotNull(naming);
    this.printer = printer;
    this.naming = naming;
  }

  @Override
  public SetExpressionsTraverser getTraverser() {
    return this.traverser;
  }

  @Override
  public void setTraverser(SetExpressionsTraverser traverser) {
    Preconditions.checkNotNull(traverser);
    this.traverser = traverser;
  }

  public IndentPrinter getPrinter() {
    return this.printer;
  }

  @Override
  public void handle(ASTSetInExpression node) {
    node.getSet().accept(getTraverser());
    getPrinter().print(".contains(");
    node.getElem().accept(getTraverser());
    getPrinter().print(")");
  }

  @Override
  public void handle(ASTSetNotInExpression node) {
    getPrinter().print("!");
    node.getSet().accept(getTraverser());
    getPrinter().print(".contains(");
    node.getElem().accept(getTraverser());
    getPrinter().print(")");
  }

  @Override
  public void handle(ASTUnionExpression node) {

  }

  @Override
  public void handle(ASTIntersectionExpression node) {

  }

  @Override
  public void handle(ASTSetMinusExpression node) {

  }

  @Override
  public void handle(ASTSetUnionExpression node) {

  }

  @Override
  public void handle(ASTSetIntersectionExpression node) {

  }

  @Override
  public void handle(ASTSetAndExpression node) {

  }

  @Override
  public void handle(ASTSetOrExpression node) {

  }

  @Override
  public void handle(ASTSetVariableDeclaration node) {

  }

  @Override
  public void handle(ASTSetComprehension node) {

  }

  @Override
  public void handle(ASTSetComprehensionItem node) {

  }

  @Override
  public void handle(ASTGeneratorDeclaration node) {

  }

  @Override
  public void handle(ASTSetEnumeration node) {

  }

  @Override
  public void handle(ASTSetValueItem node) {

  }

  @Override
  public void handle(ASTSetValueRange node) {

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
