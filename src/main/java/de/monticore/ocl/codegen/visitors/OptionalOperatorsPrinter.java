/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import com.google.common.base.Preconditions;
import de.monticore.expressions.commonexpressions._ast.ASTInfixExpression;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.optionaloperators._ast.*;
import de.monticore.ocl.optionaloperators._visitor.OptionalOperatorsHandler;
import de.monticore.ocl.optionaloperators._visitor.OptionalOperatorsTraverser;
import de.monticore.ocl.optionaloperators._visitor.OptionalOperatorsVisitor2;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.IDerive;
import de.monticore.types.check.ISynthesize;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

public class OptionalOperatorsPrinter extends AbstractPrinter
    implements OptionalOperatorsHandler, OptionalOperatorsVisitor2 {

  protected OptionalOperatorsTraverser traverser;

  /** @deprecated use other Constructor (requires TypeCheck3) */
  @Deprecated
  public OptionalOperatorsPrinter(
      IndentPrinter printer, VariableNaming naming, IDerive deriver, ISynthesize syntheziser) {
    this(printer, naming);
    this.deriver = deriver;
    this.syntheziser = syntheziser;
  }

  public OptionalOperatorsPrinter(IndentPrinter printer, VariableNaming naming) {
    Preconditions.checkNotNull(printer);
    Preconditions.checkNotNull(naming);
    this.printer = printer;
    this.naming = naming;
  }

  protected IndentPrinter getPrinter() {
    return this.printer;
  }

  public OptionalOperatorsTraverser getTraverser() {
    return this.traverser;
  }

  public void setTraverser(OptionalOperatorsTraverser traverser) {
    Preconditions.checkNotNull(traverser);
    this.traverser = traverser;
  }

  public void handle(ASTOptionalExpressionPrefix node) {
    // we cast as the types of the optional operators do not equal
    // the type of most direct Java versions in all cases
    // e.g. assume type of a is Optional<Integer>
    // typecheck returns Integer for (a?:5)
    // in Java, (a.isPresent()?a.get():5) has type int
    this.getPrinter().print("((");
    this.getPrinter().print(boxType(TypeCheck3.typeOf(node)));
    this.getPrinter().print(")(");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".isPresent() ? ");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".get() : ");
    node.getRight().accept(getTraverser());
    this.getPrinter().print("))");
  }

  public void handle(ASTOptionalLessEqualExpression node) {
    handleOptionalFirstParameterComparison(node, "<=");
  }

  public void handle(ASTOptionalGreaterEqualExpression node) {
    handleOptionalFirstParameterComparison(node, ">=");
  }

  public void handle(ASTOptionalLessThanExpression node) {
    handleOptionalFirstParameterComparison(node, "<");
  }

  public void handle(ASTOptionalGreaterThanExpression node) {
    handleOptionalFirstParameterComparison(node, ">");
  }

  public void handle(ASTOptionalEqualsExpression node) {
    this.getPrinter().print("(");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".isPresent() ? ");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".get().equals(");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(") : false)");
  }

  public void handle(ASTOptionalNotEqualsExpression node) {
    this.getPrinter().print("(");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".isPresent() ? (! ");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".get().equals(");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(")) : false)");
  }

  protected void handleOptionalFirstParameterComparison(ASTInfixExpression node, String operator) {
    this.getPrinter().print("(");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".isPresent() ? ");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".get() ");
    this.getPrinter().print(operator);
    this.getPrinter().print(" ");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(" : false)");
  }

  public void handle(ASTOptionalSimilarExpression node) {
    Log.error("0x65656 implementation not available, to be discussed");
  }

  public void handle(ASTOptionalNotSimilarExpression node) {
    Log.error("0x65657 implementation not available, to be discussed");
  }
}
