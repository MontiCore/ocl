// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.codegen.javagen;

import de.monticore.codegen.CodeGenOperationPrinter;
import de.monticore.codegen.javagen.AbstractJavaGenVisitor;
import de.monticore.ocl.optionaloperators._ast.*;
import de.monticore.ocl.optionaloperators._visitor.OptionalOperatorsHandler;
import de.monticore.ocl.optionaloperators._visitor.OptionalOperatorsTraverser;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.SymTypeExpression;

import static de.monticore.types3.SymTypeRelations.normalize;
import static de.monticore.types3.TypeCheck3.typeOf;

public class OptionalOperatorsJavaGenVisitor extends AbstractJavaGenVisitor
    implements OptionalOperatorsHandler {

  // Traverser
  protected OptionalOperatorsTraverser traverser;

  public OptionalOperatorsJavaGenVisitor(IndentPrinter printer) {
    super(printer);
  }

  @Override
  public OptionalOperatorsTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(OptionalOperatorsTraverser traverser) {
    this.traverser = traverser;
  }

  // CodeGen

  protected SymTypeExpression unwrapOptionalType(SymTypeExpression a) {
    SymTypeExpression lt = a;
    if(lt.isGenericType()){
      lt = lt.asGenericType().getArgument(0);
    } else {
      throw new IllegalStateException();
    }
    return lt;
  }

  @Override
  public void handle(ASTOptionalExpressionPrefix node) {
    // ToDo
    getPrinter().print("(");
    node.getLeft().accept(getTraverser());
    getPrinter().print(").isPresent() ? (");
    getPrinter().print("(");
    node.getLeft().accept(getTraverser());
    getPrinter().print(").get()");
    getPrinter().print(") : (");
    node.getRight().accept(getTraverser());
    getPrinter().print(")");
  }

  @Override
  public void handle(ASTOptionalLessEqualExpression node) {
    // ToDo
    getPrinter().print("(");
    node.getLeft().accept(getTraverser());
    getPrinter().print(").isPresent() && ");
    CodeGenOperationPrinter.printLessEqual(getPrinter(), normalize(typeOf(node)), unwrapOptionalType(normalize(typeOf(node.getLeft()))), normalize(typeOf(node.getRight())),
            p->{getPrinter().print("(");
              node.getLeft().accept(getTraverser());
              getPrinter().print(").get()");},
            p->node.getRight().accept(getTraverser()));
  }

  @Override
  public void handle(ASTOptionalGreaterEqualExpression node) {
    // ToDo
    getPrinter().print("(");
    node.getLeft().accept(getTraverser());
    getPrinter().print(").isPresent() && ");

    CodeGenOperationPrinter.printGreaterEqual(getPrinter(), normalize(typeOf(node)), unwrapOptionalType(normalize(typeOf(node.getLeft()))), normalize(typeOf(node.getRight())),
            p->{getPrinter().print("(");
              node.getLeft().accept(getTraverser());
              getPrinter().print(").get()");},
            p->node.getRight().accept(getTraverser()));
  }

  @Override
  public void handle(ASTOptionalLessThanExpression node) {
    // ToDo
    getPrinter().print("(");
    node.getLeft().accept(getTraverser());
    getPrinter().print(").isPresent() && ");
    CodeGenOperationPrinter.printLessThan(getPrinter(), normalize(typeOf(node)), unwrapOptionalType(normalize(typeOf(node.getLeft()))), normalize(typeOf(node.getRight())),
            p->{getPrinter().print("(");
              node.getLeft().accept(getTraverser());
              getPrinter().print(").get()");},
            p->node.getRight().accept(getTraverser()));
  }

  @Override
  public void handle(ASTOptionalGreaterThanExpression node) {
    // ToDo
    getPrinter().print("(");
    node.getLeft().accept(getTraverser());
    getPrinter().print(").isPresent() && ");
    CodeGenOperationPrinter.printGreaterThan(getPrinter(), normalize(typeOf(node)), unwrapOptionalType(normalize(typeOf(node.getLeft()))), normalize(typeOf(node.getRight())),
            p->{getPrinter().print("(");
              node.getLeft().accept(getTraverser());
              getPrinter().print(").get()");},
            p->node.getRight().accept(getTraverser()));
  }

  @Override
  public void handle(ASTOptionalEqualsExpression node) {
    // ToDo
    getPrinter().print("(");
    node.getLeft().accept(getTraverser());
    getPrinter().print(").isPresent() && ");
    CodeGenOperationPrinter.printEquals(getPrinter(), normalize(typeOf(node)), unwrapOptionalType(normalize(typeOf(node.getLeft()))), normalize(typeOf(node.getRight())),
            p->{getPrinter().print("(");
              node.getLeft().accept(getTraverser());
              getPrinter().print(").get()");},
            p->node.getRight().accept(getTraverser()));
  }

  @Override
  public void handle(ASTOptionalNotEqualsExpression node) {
    // ToDo
    getPrinter().print("(");
    node.getLeft().accept(getTraverser());
    getPrinter().print(").isPresent() && ");
    CodeGenOperationPrinter.printNotEquals(getPrinter(), normalize(typeOf(node)), unwrapOptionalType(normalize(typeOf(node.getLeft()))), normalize(typeOf(node.getRight())),
            p->{getPrinter().print("(");
              node.getLeft().accept(getTraverser());
              getPrinter().print(").get()");},
            p->node.getRight().accept(getTraverser()));
  }

  @Override
  public void handle(ASTOptionalSimilarExpression node) {
    // ToDo
    getPrinter().print("(");
    node.getLeft().accept(getTraverser());
    getPrinter().print(").isPresent() && ");
    CodeGenOperationPrinter.printEquals(getPrinter(), normalize(typeOf(node)), unwrapOptionalType(normalize(typeOf(node.getLeft()))), normalize(typeOf(node.getRight())),
            p->{getPrinter().print("(");
              node.getLeft().accept(getTraverser());
              getPrinter().print(").get()");},
            p->node.getRight().accept(getTraverser()));
  }

  @Override
  public void handle(ASTOptionalNotSimilarExpression node) {
    // ToDo
    getPrinter().print("(");
    node.getLeft().accept(getTraverser());
    getPrinter().print(").isPresent() && ");
    CodeGenOperationPrinter.printNotEquals(getPrinter(), normalize(typeOf(node)), unwrapOptionalType(normalize(typeOf(node.getLeft()))), normalize(typeOf(node.getRight())),
            p->{getPrinter().print("(");
              node.getLeft().accept(getTraverser());
              getPrinter().print(").get()");},
            p->node.getRight().accept(getTraverser()));
  }
}
