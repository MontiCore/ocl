// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.codegen.visitors;

import com.google.common.base.Preconditions;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsHandler;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsTraverser;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsVisitor2;
import de.monticore.ocl.types.check.OCLDeriver;
import de.monticore.ocl.types.check.OCLSynthesizer;
import de.monticore.ocl.types.check.OCLTypeCheck;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.SymTypePrimitive;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types.check.TypeCheckResult;
import de.monticore.types.prettyprint.MCBasicTypesFullPrettyPrinter;
import de.se_rwth.commons.logging.Log;

public class SetExpressionsPrinter extends AbstractPrinter
    implements SetExpressionsHandler, SetExpressionsVisitor2 {

  protected static final String EXPRESSION_NOT_BOOLEAN_ERROR = "0xC4721 Expected boolean expression";

  protected static final String MISSING_IMPLEMENTATION_ERROR = "0xC4722 Implementation missing";

  protected SetExpressionsTraverser traverser;

  protected IndentPrinter printer;

  public SetExpressionsPrinter(IndentPrinter printer, VariableNaming naming,
      OCLDeriver oclDeriver, OCLSynthesizer oclSynthesizer) {
    Preconditions.checkNotNull(printer);
    Preconditions.checkNotNull(naming);
    Preconditions.checkNotNull(oclDeriver);
    Preconditions.checkNotNull(oclSynthesizer);
    this.printer = printer;
    this.naming = naming;
    this.oclDeriver = oclDeriver;
    this.oclSynthesizer = oclSynthesizer;
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
    printExpressionBeginLambda(getOCLDeriver().deriveType(node));

    getPrinter().print("Set<");
    printDerivedInnerType(node);
    getPrinter().print("> ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println(" = new java.util.HashSet<>();");

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
    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTIntersectionExpression node) {
    printExpressionBeginLambda(getOCLDeriver().deriveType(node));

    getPrinter().print("Set<");
    printDerivedInnerType(node);
    getPrinter().print("> ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println(" = new java.util.HashSet<>();");

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

    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTSetMinusExpression node) {
    printExpressionBeginLambda(getOCLDeriver().deriveType(node));

    getPrinter().print("Set<");
    printDerivedInnerType(node);
    getPrinter().print("> ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println(" = new java.util.HashSet<>();");

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

    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTSetUnionExpression node) {
    printExpressionBeginLambda(getOCLDeriver().deriveType(node));

    getPrinter().print(getOCLDeriver().deriveType(node.getSet()));
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().print(" = ");
    node.getSet().accept(getTraverser());
    getPrinter().println(";");

    getPrinter().print("Set<");
    printDerivedInnerType(node);
    getPrinter().print("> ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println(" = new java.util.HashSet<>();");

    getPrinter().print("for(");
    getPrinter().print(getOCLDeriver().deriveType(node));
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

    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTSetIntersectionExpression node) {
    printExpressionBeginLambda(getOCLDeriver().deriveType(node));

    printDerivedType(node.getSet());
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().print(" = ");
    node.getSet().accept(getTraverser());
    getPrinter().println(";");

    getPrinter().print("java.util.Set<");
    printDerivedInnerType(node);
    getPrinter().print("> ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(" = ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().println(".stream().findAny().orElse(new java.util.HashSet<>());");

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

    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTSetAndExpression node) {
    printExpressionBeginLambda(getOCLDeriver().deriveType(node));

    getPrinter().print("Boolean ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println(" = true;");

    printDerivedType(node.getSet());
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().print(" = ");
    node.getSet().accept(getTraverser());
    getPrinter().println(";");

    getPrinter().print("for (Boolean ");
    printDerivedType(node.getSet());
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().print("_item");
    getPrinter().print(" : ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().println(") {");
    getPrinter().indent();

    getPrinter().print(getNaming().getName(node));
    getPrinter().print(" &= ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().print("_item");
    getPrinter().print(";");

    getPrinter().unindent();
    getPrinter().println("}");

    getPrinter().print("return ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(";");

    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTSetOrExpression node) {
    printExpressionBeginLambda(getOCLDeriver().deriveType(node));

    getPrinter().print("Boolean ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println(" = false;");

    ASTExpression node1 = node.getSet();
    printDerivedType(node1);
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node1));
    getPrinter().print(" = ");
    node1.accept(getTraverser());
    getPrinter().println(";");

    getPrinter().print("for (Boolean ");
    printDerivedType(node.getSet());
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().print("_item");
    getPrinter().print(" : ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().println(") {");
    getPrinter().indent();

    getPrinter().print(getNaming().getName(node));
    getPrinter().print(" |= ");
    getPrinter().print(getNaming().getName(node.getSet()));
    getPrinter().print("_item");
    getPrinter().print(";");

    getPrinter().unindent();
    getPrinter().println("}");

    getPrinter().print("return ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(";");

    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTSetComprehension node) {
    printExpressionBeginLambda(getOCLDeriver().deriveType(node));
    printDerivedType(node);
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(" = ");
    if (node.getMCType()
        .printType(new MCBasicTypesFullPrettyPrinter(new IndentPrinter()))
        .contains("Set")) {
      getPrinter().println("new java.util.HashSet<>();");
    }
    else {
      getPrinter().println("new java.util.LinkedList<>();");
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
    else if (node.getLeft().isPresentExpression()) {
      node.getLeft().getExpression().accept(getTraverser());
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

    getPrinter().print("return ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println(";");

    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTSetComprehensionItem node) {
    if (node.isPresentExpression()) {
      TypeCheckResult type = getOCLDeriver().deriveType(node.getExpression());
      if (type.isPresentResult()
          && OCLTypeCheck.isBoolean(type.getResult())) {
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
    else if (node.isPresentSetVariableDeclaration()) {
      ASTSetVariableDeclaration setVarDecl = node.getSetVariableDeclaration();
      if (setVarDecl.isPresentMCType()) {
        getPrinter().print(boxType(getOCLSynthesizer().synthesizeType(setVarDecl.getMCType())));
      }
      else if (setVarDecl.isPresentExpression()) {
        getPrinter().print(boxType(getOCLDeriver().deriveType(setVarDecl.getExpression())));
      }
      else {
        Log.error(UNEXPECTED_STATE_AST_NODE, setVarDecl.get_SourcePositionStart(),
            setVarDecl.get_SourcePositionEnd());
      }
      getPrinter().print(" ");
      getPrinter().print(setVarDecl.getName());
      getPrinter().print(" = ");
      setVarDecl.getExpression().accept(getTraverser());
      getPrinter().println(";");
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
      getPrinter().print(boxType(getOCLSynthesizer().synthesizeType(node.getMCType())));
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
    printExpressionBeginLambda(getOCLDeriver().deriveType(node));

    printDerivedType(node);
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print(" = ");
    if (node.getMCType()
        .printType(new MCBasicTypesFullPrettyPrinter(new IndentPrinter()))
        .contains("Set")) {
      getPrinter().println("new java.util.HashSet<>();");
    }
    else {
      getPrinter().println("new java.util.LinkedList<>();");
    }

    for (ASTSetCollectionItem item : node.getSetCollectionItemList()) {
      getPrinter().print(getNaming().getName(node));
      //for ASTSetValueItem we could use "add", but we avoid reflections
      getPrinter().print(".addAll(");
      item.accept(getTraverser());
      getPrinter().println(");");
    }

    getPrinter().print("return ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println(";");

    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTSetValueItem node) {
    getPrinter().print("java.util.Collections.singleton(");
    node.getExpression().accept(getTraverser());
    getPrinter().print(")");
  }

  @Override
  public void handle(ASTSetValueRange node) {
    // Lambda returning List
    this.getPrinter().print("((java.util.function.Supplier<");
    getPrinter().print("java.util.List<");
    printDerivedType(node.getLowerBound());
    getPrinter().print(">");
    this.getPrinter().println(">)()->{");
    this.getPrinter().indent();

    getPrinter().print("java.util.List<");
    printDerivedType(node.getLowerBound());
    getPrinter().print("> ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println(" = new java.util.LinkedList<>();");

    // bounds
    printDerivedType(node.getLowerBound());
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print("LowerBound = ");
    node.getLowerBound().accept(getTraverser());
    getPrinter().println(";");

    printDerivedType(node.getLowerBound());
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print("UpperBound = ");
    node.getUpperBound().accept(getTraverser());
    getPrinter().println(";");

    // lower bound > upper bound -> backwards (Step = -1)
    getPrinter().print("int ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println("Step = 1;");

    getPrinter().print("if (");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print("LowerBound > ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println("UpperBound) {");
    getPrinter().indent();

    getPrinter().print(getNaming().getName(node));
    getPrinter().println("Step = -1;");

    getPrinter().unindent();
    getPrinter().println("}");

    //iterate and add to result
    getPrinter().print("for (");
    printDerivedType(node.getLowerBound());
    getPrinter().print(" ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print("_iter = ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print("LowerBound; ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print("_iter * ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print("Step <= ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print("UpperBound * ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print("Step; ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print("_iter = (");
    // java.Lang.Character -> avoid type errors
    // this works as only primitives are supported
    getPrinter().print(SymTypePrimitive.unbox(
        getOCLDeriver().deriveType(node.getLowerBound()).getResult().printFullName()));
    getPrinter().print(")(");
    getPrinter().print(getNaming().getName(node));
    getPrinter().print("_iter + ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println("Step)) {");
    getPrinter().indent();

    getPrinter().print(getNaming().getName(node));
    getPrinter().print(".add(");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println("_iter);");

    getPrinter().unindent();
    getPrinter().println("}");

    getPrinter().print("return ");
    getPrinter().print(getNaming().getName(node));
    getPrinter().println(";");

    this.getPrinter().unindent();
    this.getPrinter().print("}).get()");
  }

  protected void printDerivedType(ASTExpression node) {
    TypeCheckResult type = getOCLDeriver().deriveType(node);
    if (!type.isPresentResult()) {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
      return;
    }
    getPrinter().print(boxType(type));
  }

  /**
   * given an expression with type {@code Generic<MyType>} prints {@code MyType}
   *
   * @param node the expression
   */
  protected void printDerivedInnerType(ASTExpression node) {
    SymTypeExpression innerType = getInnerType(node);
    if (innerType != null) {
      if (innerType.isGenericType()) {
        getPrinter().print(SymTypeOfGenerics.box((SymTypeOfGenerics) innerType));
      }
      else {
        getPrinter().print(SymTypePrimitive.box(innerType.printFullName()));
      }
    }
    else {
      Log.error(INNER_TYPE_NOT_DERIVED_ERROR, node.get_SourcePositionStart());
    }
  }

  /**
   * given an expression with type {@code Generic<MyType>} returns {@code MyType}
   *
   * @param node the expression with one inner type
   * @return the inner type
   */
  protected SymTypeExpression getInnerType(ASTExpression node) {
    SymTypeExpression innerType = null;
    TypeCheckResult type = this.getOCLDeriver().deriveType(node);
    if (type.isPresentResult()
        && type.getResult().isGenericType()
        && ((SymTypeOfGenerics) type.getResult()).sizeArguments() == 1) {
      innerType = ((SymTypeOfGenerics) type.getResult()).getArgument(0);
    }
    if (innerType == null) {
      Log.error(INNER_TYPE_NOT_DERIVED_ERROR, node.get_SourcePositionStart());
    }
    return innerType;
  }

}
