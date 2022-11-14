/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import com.google.common.base.Preconditions;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.oclexpressions._ast.ASTAnyExpression;
import de.monticore.ocl.oclexpressions._ast.ASTEquivalentExpression;
import de.monticore.ocl.oclexpressions._ast.ASTExistsExpression;
import de.monticore.ocl.oclexpressions._ast.ASTForallExpression;
import de.monticore.ocl.oclexpressions._ast.ASTIfThenElseExpression;
import de.monticore.ocl.oclexpressions._ast.ASTImpliesExpression;
import de.monticore.ocl.oclexpressions._ast.ASTInDeclaration;
import de.monticore.ocl.oclexpressions._ast.ASTInDeclarationVariable;
import de.monticore.ocl.oclexpressions._ast.ASTInstanceOfExpression;
import de.monticore.ocl.oclexpressions._ast.ASTIterateExpression;
import de.monticore.ocl.oclexpressions._ast.ASTLetinExpression;
import de.monticore.ocl.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.ocl.oclexpressions._ast.ASTTypeCastExpression;
import de.monticore.ocl.oclexpressions._ast.ASTTypeIfExpression;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.IDerive;
import de.monticore.types.check.ISynthesize;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types.check.TypeCheckResult;
import de.se_rwth.commons.logging.Log;

import static de.monticore.types.check.SymTypePrimitive.box;

public class OCLExpressionsPrinter extends AbstractPrinter implements OCLExpressionsHandler,
    OCLExpressionsVisitor2 {

  protected OCLExpressionsTraverser traverser;

  public OCLExpressionsPrinter(IndentPrinter printer, VariableNaming naming,
      IDerive deriver, ISynthesize syntheziser) {
    Preconditions.checkNotNull(printer);
    Preconditions.checkNotNull(naming);
    Preconditions.checkNotNull(deriver);
    Preconditions.checkNotNull(syntheziser);
    this.printer = printer;
    this.naming = naming;
    this.deriver = deriver;
    this.syntheziser = syntheziser;
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

  public IndentPrinter getPrinter() {
    return this.printer;
  }

  @Override
  public void handle(ASTIfThenElseExpression node) {
    printExpressionBeginLambda(getDeriver().deriveType(node));
    TypeCheckResult type = this.getDeriver().deriveType(node);
    if (!type.isPresentResult()) {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
    }
    else {
      this.getPrinter().print(type.getResult().getTypeInfo().getFullName());
      this.getPrinter().print(" ");
    }
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().println(";");
    this.getPrinter().print("if(");
    node.getCondition().accept(this.getTraverser());
    this.getPrinter().println(") {");
    this.getPrinter().indent();
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" = ");
    node.getThenExpression().accept(getTraverser());
    this.getPrinter().println(";");
    this.getPrinter().unindent();
    this.getPrinter().println("} else {");
    this.getPrinter().indent();
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" = ");
    node.getElseExpression().accept(getTraverser());
    this.getPrinter().println(";");
    this.getPrinter().unindent();
    this.getPrinter().println("}");
    this.getPrinter().print("return ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println(";");
    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTImpliesExpression node) {
    this.getPrinter().print("!(");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(" || ");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(")");
  }

  @Override
  public void handle(ASTForallExpression node) {
    printExpressionBeginLambda(getDeriver().deriveType(node));
    this.getPrinter().print("Boolean ");
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().println(" = true;");
    node.getInDeclarationList().forEach(dec -> dec.accept(getTraverser()));
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" &= ");
    node.getExpression().accept(getTraverser());
    this.getPrinter().println(";");
    OCLExpressionsTraverser endTraverser = OCLMill.traverser();
    endTraverser.setOCLExpressionsHandler(new EndingBracketPrinter(this.getPrinter()));
    node.getInDeclarationList().forEach(dec -> dec.accept(endTraverser));
    this.getPrinter().print("return ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println(";");
    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTExistsExpression node) {
    printExpressionBeginLambda(getDeriver().deriveType(node));
    this.getPrinter().print("Boolean ");
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().println(" = false;");
    node.getInDeclarationList().forEach(dec -> dec.accept(getTraverser()));
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" |= ");
    node.getExpression().accept(getTraverser());
    this.getPrinter().println(";");
    OCLExpressionsTraverser endTraverser = OCLMill.traverser();
    endTraverser.setOCLExpressionsHandler(new EndingBracketPrinter(this.getPrinter()));
    node.getInDeclarationList().forEach(dec -> dec.accept(endTraverser));
    this.getPrinter().print("return ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println(";");
    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTLetinExpression node) {
    printExpressionBeginLambda(getDeriver().deriveType(node));
    node.getOCLVariableDeclarationList().forEach(dec -> dec.accept(getTraverser()));
    this.getPrinter().print("return ");
    node.getExpression().accept(getTraverser());
    this.getPrinter().println(";");
    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTIterateExpression node) {
    printExpressionBeginLambda(getDeriver().deriveType(node.getInit().getExpression()));
    node.getInit().accept(getTraverser());

    node.getIteration().accept(getTraverser());
    getPrinter().print(node.getName());
    getPrinter().print(" = ");
    node.getValue().accept(getTraverser());
    getPrinter().println(";");
    getPrinter().unindent();
    getPrinter().println("}");

    getPrinter().print("return ");
    getPrinter().print(node.getName());
    getPrinter().println(";");

    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTTypeCastExpression node) {
    this.getPrinter().print("((");
    getPrinter().print(boxType(getSynthesizer().synthesizeType(node.getMCType())));
    this.getPrinter().print(") ");
    node.getExpression().accept(getTraverser());
    this.getPrinter().print(")");
  }

  @Override
  public void handle(ASTEquivalentExpression node) {
    printAsBoxedType(node.getLeft());
    this.getPrinter().print(".equals(");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(")");
  }

  @Override
  public void handle(ASTInDeclaration node) {
    SymTypeExpression innerType = null;
    if (node.isPresentMCType()) {
      TypeCheckResult type = this.getSynthesizer().synthesizeType(node.getMCType());
      if (type.isPresentResult()) {
        innerType = type.getResult();
      }
    }
    else if (node.isPresentExpression()) {
      TypeCheckResult type = this.getDeriver().deriveType(node.getExpression());
      if (type.isPresentResult()
          && type.getResult().isGenericType()
          && ((SymTypeOfGenerics) type.getResult()).sizeArguments() == 1) {
        innerType = ((SymTypeOfGenerics) type.getResult()).getArgument(0);
      }
    }
    if (innerType == null) {
      Log.error(INNER_TYPE_NOT_DERIVED_ERROR, node.get_SourcePositionStart());
    }
    if (node.isPresentExpression()) {
      for (ASTInDeclarationVariable var : node.getInDeclarationVariableList()) {
        this.getPrinter().print("for(");
        this.getPrinter().print(innerType.getTypeInfo().getFullName());
        this.getPrinter().print(" ");
        this.getPrinter().print(var.getName());
        this.getPrinter().print(" : ");
        node.getExpression().accept(getTraverser());
        this.getPrinter().println(") {");
        this.getPrinter().indent();
      }
    }
    else {
      Log.error(UNEXPECTED_STATE_AST_NODE, node.get_SourcePositionStart(),
          node.get_SourcePositionEnd());
    }
  }

  @Override
  public void handle(ASTOCLVariableDeclaration node) {
    //variable is not final for iterate expressions
    if (node.isPresentMCType()) {
      getPrinter().print(boxType(getSynthesizer().synthesizeType(node.getMCType())));
    }
    else if (node.isPresentExpression()) {
      getPrinter().print(boxType(getDeriver().deriveType(node.getExpression())));
    }
    this.getPrinter().print(" ");
    this.getPrinter().print(node.getName());
    if (node.isPresentExpression()) {
      this.getPrinter().print(" = ");
      node.getExpression().accept(this.getTraverser());
    }
    this.getPrinter().println(";");
  }

  @Override
  public void handle(ASTTypeIfExpression node) {
    this.getPrinter().print("((");
    this.getPrinter().print(node.getName());
    this.getPrinter().print(" instanceof ");
    getPrinter().print(boxType(getSynthesizer().synthesizeType(node.getMCType())));
    this.getPrinter().print(") ? ");
    // todo make "Name" known as type MCType, does this work in Java?
    // not even instanceof-pattern-matching (Java 14) is enough (creates a new symbol)
    // might work using new Java scope?
    // -> tested: it does not work using Lambda...
    node.getThenExpression().accept(this.getTraverser());
    this.getPrinter().print(" : ");
    node.getElseExpression().accept(this.getTraverser());
    this.getPrinter().print(")");
  }

  @Override
  public void handle(ASTInstanceOfExpression node) {
    this.getPrinter().print("(");
    node.getExpression().accept(this.getTraverser());
    this.getPrinter().print(" instanceof ");
    getPrinter().print(boxType(getSynthesizer().synthesizeType(node.getMCType())));
    this.getPrinter().print(")");
  }

  @Override
  public void handle(ASTAnyExpression node) {
    this.getPrinter().print("(");
    node.getExpression().accept(getTraverser());
    this.getPrinter().print(").iterator().next()");
  }

  /**
   * if node has a primitive type,
   * this prints the Java expression
   * such that it has a non-primitive type.
   * e.g. {@code 5} to {@code ((Integer) 5)}
   *
   * @param node the expression to be printed
   */
  protected void printAsBoxedType(ASTExpression node) {
    TypeCheckResult type = this.getDeriver().deriveType(node);
    if (!type.isPresentResult()) {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
      return;
    }
    if (type.getResult().isPrimitive()) {
      getPrinter().print("((");
      this.getPrinter().print(box(type.getResult().printFullName()));
      getPrinter().print(") ");
      node.accept(getTraverser());
      getPrinter().print(")");
    }
    else {
      node.accept(getTraverser());
    }
  }

}
