// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.codegen.visitors;

import com.google.common.base.Preconditions;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsHandler;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsTraverser;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsVisitor2;
import de.monticore.ocl.types.check.OCLTypeCheck;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types.check.TypeCheckResult;
import de.monticore.types.prettyprint.MCBasicTypesFullPrettyPrinter;
import de.se_rwth.commons.logging.Log;

import static de.monticore.types.check.SymTypeConstant.box;

public class SetExpressionsPrinter extends AbstractPrinter
    implements SetExpressionsHandler, SetExpressionsVisitor2 {

  protected static final String EXPRESSION_NOT_BOOLEAN_ERROR = "0xC4721 Expected boolean expression";

  protected static final String MISSING_IMPLEMENTATION_ERROR = "0xC4722 Implementation missing";

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
    printExpressionBeginLambda(node);
    getPrinter().print("Set<");
    printDerivedInnerType(node);
    getPrinter().print("> ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println(" = new HashSet<>();");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(".addAll(");
    node.getLeft().accept(getTraverser());
    getPrinter().println(");");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(".addAll(");
    node.getRight().accept(getTraverser());
    getPrinter().println(");");
    getPrinter().print("return ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(";");
    printExpressionEndLambda(node);
  }

  @Override
  public void handle(ASTIntersectionExpression node) {
    printExpressionBeginLambda(node);
    getPrinter().print("Set<");
    printDerivedInnerType(node);
    getPrinter().print("> ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println(" = new HashSet<>();");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(".addAll(");
    node.getLeft().accept(getTraverser());
    getPrinter().println(");");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(".retainAll(");
    node.getRight().accept(getTraverser());
    getPrinter().println(");");
    getPrinter().print("return ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(";");
    printExpressionEndLambda(node);
  }

  @Override
  public void handle(ASTSetMinusExpression node) {
    printExpressionBeginLambda(node);
    getPrinter().print("Set<");
    printDerivedInnerType(node);
    getPrinter().print("> ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println(" = new HashSet<>();");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(".addAll(");
    node.getLeft().accept(getTraverser());
    getPrinter().println(");");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(".removeAll(");
    node.getRight().accept(getTraverser());
    getPrinter().println(");");
    getPrinter().print("return ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(";");
    printExpressionEndLambda(node);
  }

  @Override
  public void handle(ASTSetUnionExpression node) {
    printExpressionBeginLambda(node);

    getPrinter().print("Set<");
    printDerivedInnerType(node);
    getPrinter().print("> ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println(" = new HashSet<>();");

    printDerivedType(node);
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().print(" = ");
    node.getSet().accept(getTraverser());
    getPrinter().println(";");

    getPrinter().print("for(");
    printDerivedType(node);
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().print("_item");
    getPrinter().print(" : ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().println(") {");
    getPrinter().indent();

    getPrinter().print(getNaming().getName(node));
    getPrinter().print(".addAll(");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().print("_item");
    getPrinter().println(");");

    getPrinter().unindent();
    getPrinter().println("}");
    getPrinter().print("return ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(";");

    printExpressionEndLambda(node);
  }

  @Override
  public void handle(ASTSetIntersectionExpression node) {
    printExpressionBeginLambda(node);

    printDerivedType(node);
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().print(" = ");
    node.getSet().accept(getTraverser());
    getPrinter().println(";");

    getPrinter().print("Set<");
    printDerivedInnerType(node);
    getPrinter().print("> ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(" = ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().println(".stream().findAny().orElse(new HashSet<>());");

    getPrinter().print("for(");
    printDerivedType(node);
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().print("_item");
    getPrinter().print(" : ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().println(") {");
    getPrinter().indent();

    getPrinter().print(getNaming().getName(node));
    getPrinter().print(".retainAll(");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().print("_item");
    getPrinter().println(");");

    getPrinter().unindent();
    getPrinter().println("}");
    getPrinter().print("return ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(";");

    printExpressionEndLambda(node);
  }

  @Override
  public void handle(ASTSetAndExpression node) {

  }

  @Override
  public void handle(ASTSetOrExpression node) {

  }

  @Override
  public void handle(ASTSetComprehension node) {
    printExpressionBeginLambda(node);
    printDerivedType(node);
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(" = ");
    if (node.getMCType()
        .printType(new MCBasicTypesFullPrettyPrinter(new IndentPrinter()))
        .contains("Set")) {
      getPrinter().println("new HashSet<>();");
    }
    else {
      getPrinter().println("new LinkedList<>();");
    }

    if (node.getLeft().isPresentGeneratorDeclaration()) {
      node.getLeft().accept(getTraverser());
    }
    for (ASTSetComprehensionItem item : node.getSetComprehensionItemList()) {
      item.accept(getTraverser());
    }

    getPrinter().print(getNaming().getName(node));
    getPrinter().print(".add(");
    if (node.getLeft().isPresentGeneratorDeclaration()) {
      getPrinter().print(node.getLeft().getGeneratorDeclaration().getName());
    }
    else {
      node.getLeft().accept(getTraverser());
    }
    getPrinter().println(");");

    for (ASTSetComprehensionItem item : node.getSetComprehensionItemList()) {
      if (!item.isPresentSetVariableDeclaration()) {
        getPrinter().println("}");
        getPrinter().unindent();
      }
    }
    if (node.getLeft().isPresentGeneratorDeclaration()) {
      getPrinter().println("}");
      getPrinter().unindent();
    }

    printExpressionEndLambda(node);
  }

  @Override
  public void handle(ASTSetComprehensionItem node) {
    if (node.isPresentExpression()) {
      TypeCheckResult type = getTypeCalculator().deriveType(node.getExpression());
      if (type.isPresentCurrentResult()
          && OCLTypeCheck.isBoolean(type.getCurrentResult())) {
        getPrinter().print("if (");
        node.getExpression().accept(getTraverser());
        getPrinter().println(") {");
        getPrinter().indent();
      }
      else {
        Log.error(EXPRESSION_NOT_BOOLEAN_ERROR, node.get_SourcePositionStart());
      }
    }
    else if (node.isPresentGeneratorDeclaration()) {
      node.getGeneratorDeclaration().accept(getTraverser());
    }
    else if (node.isPresentSetVariableDeclaration()
        && node.getSetVariableDeclaration().isPresentMCType()) {
      ASTSetVariableDeclaration setVarDecl = node.getSetVariableDeclaration();
      if (setVarDecl.isPresentMCType()) {
        if (setVarDecl.isPresentMCType()) {
          setVarDecl.getMCType().accept(getTraverser());
        }
        else {
          printDerivedInnerType(setVarDecl.getExpression());
        }
        getPrinter().print(" ");
        getPrinter().print(setVarDecl.getName());
        getPrinter().print(" = ");
        setVarDecl.getExpression().accept(getTraverser());
        getPrinter().println(";");
      }
    }
    else {
      //failsafe if something is added to the grammar
      Log.error(MISSING_IMPLEMENTATION_ERROR);
    }
  }

  @Override
  public void handle(ASTGeneratorDeclaration node) {
    getPrinter().print("for (");
    if (node.isPresentMCType()) {
      node.getMCType().accept(getTraverser());
    }
    else {
      printDerivedInnerType(node.getExpression());
    }
    getPrinter().print(" ");
    getPrinter().print(node.getName());
    getPrinter().print(" : ");
    node.getExpression().accept(getTraverser());
    getPrinter().println(") {");
    getPrinter().indent();
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

  protected void printDerivedType(ASTExpression node) {
    TypeCheckResult type = getTypeCalculator().deriveType(node);
    if (!type.isPresentCurrentResult()) {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
      return;
    }
    getPrinter().print(type.getCurrentResult().getTypeInfo().getFullName());
  }

  /**
   * given an expression with type Generic with argument MyType prints MyType
   *
   * @param node the expression
   */
  protected void printDerivedInnerType(ASTExpression node) {
    TypeCheckResult type = this.getTypeCalculator().deriveType(node);
    if (type.isPresentCurrentResult()
        && type.getCurrentResult().isGenericType()
        && ((SymTypeOfGenerics) type.getCurrentResult()).sizeArguments() == 1) {
      SymTypeExpression innerType = ((SymTypeOfGenerics) type.getCurrentResult()).getArgument(0);
      if (innerType != null) {
        getPrinter().print(innerType.getTypeInfo().getFullName());
      }
      else {
        Log.error(INNER_TYPE_NOT_DERIVED_ERROR, node.get_SourcePositionStart());
      }
    }
  }

}
