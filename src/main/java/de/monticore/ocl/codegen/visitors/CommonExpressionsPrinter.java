/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import static de.monticore.types.check.SymTypePrimitive.box;

import com.google.common.base.Preconditions;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsHandler;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsTraverser;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsVisitor2;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.IDerive;
import de.monticore.types.check.ISynthesize;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.TypeCheckResult;
import de.se_rwth.commons.logging.Log;

public class CommonExpressionsPrinter extends AbstractPrinter
    implements CommonExpressionsHandler, CommonExpressionsVisitor2 {

  protected CommonExpressionsTraverser traverser;

  public CommonExpressionsPrinter(
      IndentPrinter printer, VariableNaming naming, IDerive deriver, ISynthesize syntheziser) {
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
  public CommonExpressionsTraverser getTraverser() {
    return this.traverser;
  }

  @Override
  public void setTraverser(CommonExpressionsTraverser traverser) {
    Preconditions.checkNotNull(traverser);
    this.traverser = traverser;
  }

  public IndentPrinter getPrinter() {
    return this.printer;
  }

  @Override
  public void handle(ASTMinusPrefixExpression node) {
    Preconditions.checkNotNull(node);
    this.getPrinter().print("-");
    node.getExpression().accept(getTraverser());
  }

  @Override
  public void handle(ASTBooleanNotExpression node) {
    Preconditions.checkNotNull(node);
    this.getPrinter().print("!");
    node.getExpression().accept(getTraverser());
  }

  @Override
  public void handle(ASTLogicalNotExpression node) {
    Preconditions.checkNotNull(node);
    this.getPrinter().print("!");
    node.getExpression().accept(getTraverser());
  }

  @Override
  public void handle(ASTMultExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "*");
  }

  @Override
  public void handle(ASTDivideExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "/");
  }

  @Override
  public void handle(ASTModuloExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "%");
  }

  @Override
  public void handle(ASTPlusExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "+");
  }

  @Override
  public void handle(ASTMinusExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "-");
  }

  @Override
  public void handle(ASTLessEqualExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "<=");
  }

  @Override
  public void handle(ASTGreaterEqualExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, ">=");
  }

  @Override
  public void handle(ASTLessThanExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "<");
  }

  @Override
  public void handle(ASTGreaterThanExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, ">=");
  }

  @Override
  public void handle(ASTBooleanAndOpExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "&&");
  }

  @Override
  public void handle(ASTBooleanOrOpExpression node) {
    Preconditions.checkNotNull(node);
    this.handleInfixExpression(node, "||");
  }

  @Override
  public void handle(ASTConditionalExpression node) {
    Preconditions.checkNotNull(node);
    this.getPrinter().print("(");
    node.getCondition().accept(this.getTraverser());
    this.getPrinter().print(" ? ");
    node.getTrueExpression().accept(this.getTraverser());
    this.getPrinter().print(" : ");
    node.getFalseExpression().accept(this.getTraverser());
    this.getPrinter().print(")");
  }

  @Override
  public void handle(ASTEqualsExpression node) {
    Preconditions.checkNotNull(node);
    printAsBoxedType(node.getLeft());
    this.getPrinter().print(".equals(");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(")");
  }

  @Override
  public void handle(ASTNotEqualsExpression node) {
    Preconditions.checkNotNull(node);
    this.getPrinter().print("!");
    printAsBoxedType(node.getLeft());
    this.getPrinter().print(".equals(");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(")");
  }

  @Override
  public void handle(ASTArrayAccessExpression node) {
    Log.errorIfNull(node);
    TypeCheckResult exprTypeRes = getDeriver().deriveType(node.getExpression());
    if (!exprTypeRes.isPresentResult()) {
      // error should be logged already
      getPrinter().print("NO_TYPE_DERIVED_ARRAY_ACCESS_EXPRESSION");
    } else {
      SymTypeExpression exprType = exprTypeRes.getResult();
      getPrinter().print("(");
      node.getExpression().accept(getTraverser());
      getPrinter().print(")");
      printArrayAccess(exprType, node, 0);
    }
  }

  protected void printArrayAccess(
      SymTypeExpression exprType, ASTArrayAccessExpression node, int depth) {
    // Expression before the access has already been printed
    if (exprType.isArrayType()) {
      getPrinter().print("[");
      node.getIndexExpression().accept(getTraverser());
      getPrinter().print("]");
    } else if (OCLSymTypeRelations.isList(exprType) || OCLSymTypeRelations.isMap(exprType)) {
      getPrinter().print(".get(");
      node.getIndexExpression().accept(getTraverser());
      getPrinter().print(")");
    } else if (OCLSymTypeRelations.isOptional(exprType)) {
      getPrinter().print(".map(");
      getPrinter().print(getNaming().getName(node) + "_optVar" + depth);
      getPrinter().print(" ->");
      getPrinter().println();
      getPrinter().indent();
      getPrinter().print(getNaming().getName(node) + "_optVar" + depth);
      printArrayAccess(OCLSymTypeRelations.getCollectionElementType(exprType), node, depth + 1);
      getPrinter().println();
      getPrinter().unindent();
      getPrinter().print(")");
    }
    // can only be set or collection
    else if (OCLSymTypeRelations.isOCLCollection(exprType)) {
      getPrinter().print(".stream().map(");
      getPrinter().print(getNaming().getName(node) + "_setVar" + depth);
      getPrinter().print(" ->");
      getPrinter().println();
      getPrinter().indent();
      getPrinter().print(getNaming().getName(node) + "_setVar" + depth);
      printArrayAccess(OCLSymTypeRelations.getCollectionElementType(exprType), node, depth + 1);
      getPrinter().println();
      getPrinter().unindent();
      getPrinter().print(")");
      getPrinter()
          .print(".collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new))");
    } else {
      // error already logged
      getPrinter().print("NO_VALID_TYPE_DERIVED_ARRAY_ACCESS_EXPRESSION");
    }
  }

  @Override
  public void handle(ASTCallExpression node) {
    Preconditions.checkNotNull(node);
    node.getExpression().accept(this.getTraverser());
    node.getArguments().accept(this.getTraverser());
  }

  @Override
  public void handle(ASTFieldAccessExpression node) {
    Preconditions.checkNotNull(node);
    node.getExpression().accept(this.getTraverser());
    this.getPrinter().print(".");
    this.getPrinter().print(node.getName());
  }

  protected void handleInfixExpression(ASTInfixExpression node, String operator) {
    Preconditions.checkNotNull(node);
    Preconditions.checkNotNull(operator);
    Preconditions.checkArgument(!operator.isEmpty());
    this.getPrinter().print("(");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(" ");
    this.getPrinter().print(operator);
    this.getPrinter().print(" ");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(")");
  }

  /**
   * if node has a primitive type, this prints the Java expression such that it has a non-primitive
   * type. e.g. "5" to "((Integer) 5)"
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
      this.getPrinter().print(box(type.getResult().getTypeInfo().getFullName()));
      getPrinter().print(") ");
      node.accept(getTraverser());
      getPrinter().print(")");
    } else {
      node.accept(getTraverser());
    }
  }
}
