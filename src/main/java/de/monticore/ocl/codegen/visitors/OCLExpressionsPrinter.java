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
import de.monticore.ocl.oclexpressions._ast.ASTIterateExpression;
import de.monticore.ocl.oclexpressions._ast.ASTLetinExpression;
import de.monticore.ocl.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.ocl.oclexpressions._ast.ASTTypeIfExpression;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.IDerive;
import de.monticore.types.check.ISynthesize;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

public class OCLExpressionsPrinter extends AbstractPrinter
    implements OCLExpressionsHandler, OCLExpressionsVisitor2 {

  protected OCLExpressionsTraverser traverser;

  /**
   * @deprecated use other Constructor (requires TypeCheck3)
   */
  @Deprecated
  public OCLExpressionsPrinter(
      IndentPrinter printer, VariableNaming naming, IDerive deriver, ISynthesize syntheziser) {
    this(printer, naming);
    this.deriver = deriver;
    this.syntheziser = syntheziser;
  }

  public OCLExpressionsPrinter(IndentPrinter printer, VariableNaming naming) {
    Preconditions.checkNotNull(printer);
    Preconditions.checkNotNull(naming);
    OCLSymTypeRelations.init();
    this.printer = printer;
    this.naming = naming;
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
  public void handle(ASTTypeIfExpression node) {
    SymTypeExpression type = TypeCheck3.typeOf(node);
    printExpressionBeginLambda(type);
    // TC1 -> TC3 hack
    type = OCLSymTypeRelations.normalize(OCLSymTypeRelations.box(type));
    // returnType newName;
    if (type.isObscureType()) {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
      return;
    } else {
      this.getPrinter().print(type.getTypeInfo().getFullName());
      this.getPrinter().print(" ");
    }
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().println(";");
    // if (name instanceof type) {
    this.getPrinter().print("if(");
    this.getPrinter().print(node.getName());
    this.getPrinter().print(" instanceof ");
    node.getMCType().accept(getTraverser());
    this.getPrinter().println(") {");
    this.getPrinter().indent();
    // make name known as type in the thenExpression only(!)
    // type tmpName = (type)name;
    SymTypeExpression mcType = TypeCheck3.symTypeFromAST(node.getMCType());
    getPrinter().print(boxType(mcType));
    this.getPrinter().print(" ");
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print("_ShadowingVar");
    this.getPrinter().print(" = ");
    this.getPrinter().print("(");
    getPrinter().print(boxType(mcType));
    this.getPrinter().print(")");
    this.getPrinter().print(node.getName());
    this.getPrinter().println(";");
    // open shadowing scope
    this.getPrinter().print("class ");
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print("_ShadowedCalculation");
    this.getPrinter().println("{");
    this.getPrinter().indent();
    this.getPrinter().print(type.getTypeInfo().getFullName());
    this.getPrinter().print(" ");
    this.getPrinter().print("calculate");
    this.getPrinter().print("()");
    this.getPrinter().println("{");
    this.getPrinter().indent();
    // type name = tmpName; // shadows
    getPrinter().print(boxType(mcType));
    this.getPrinter().print(" ");
    this.getPrinter().print(node.getName());
    this.getPrinter().print(" = ");
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print("_ShadowingVar");
    this.getPrinter().println(";");
    // return thenExpression; // from scope
    this.getPrinter().print("return ");
    node.getThenExpression().accept(getTraverser());
    this.getPrinter().println(";");
    // close shadowing scope
    this.getPrinter().unindent();
    this.getPrinter().println("}");
    this.getPrinter().unindent();
    this.getPrinter().println("}");
    // set newName to scoped calculation result;
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" = ");
    this.getPrinter().print("new ");
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print("_ShadowedCalculation");
    this.getPrinter().print("()");
    this.getPrinter().print(".calculate()");
    this.getPrinter().println(";");
    // } else {
    this.getPrinter().unindent();
    this.getPrinter().println("} else {");
    this.getPrinter().indent();
    // in elseExpression, name is NOT known as type
    // newName = elseExpression;
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" = ");
    node.getElseExpression().accept(getTraverser());
    this.getPrinter().println(";");
    // }
    this.getPrinter().unindent();
    this.getPrinter().println("}");
    // return newName;
    this.getPrinter().print("return ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println(";");
    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTIfThenElseExpression node) {
    SymTypeExpression type = TypeCheck3.typeOf(node);
    printExpressionBeginLambda(type);
    // TC1 -> TC3 hack

    type = OCLSymTypeRelations.normalize(OCLSymTypeRelations.box(type));
    // expressionType newName;
    if (type.isObscureType()) {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
    } else {
      this.getPrinter().print(type.getTypeInfo().getFullName());
      this.getPrinter().print(" ");
    }
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().println(";");
    // if(condition) {
    this.getPrinter().print("if(");
    node.getCondition().accept(this.getTraverser());
    this.getPrinter().println(") {");
    this.getPrinter().indent();
    // newName = thenExpression;
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" = ");
    node.getThenExpression().accept(getTraverser());
    this.getPrinter().println(";");
    // } else {
    this.getPrinter().unindent();
    this.getPrinter().println("} else {");
    this.getPrinter().indent();
    // newName = elseExpression;
    this.getPrinter().print(getNaming().getName(node));
    this.getPrinter().print(" = ");
    node.getElseExpression().accept(getTraverser());
    this.getPrinter().println(";");
    // }
    this.getPrinter().unindent();
    this.getPrinter().println("}");
    // return newName;
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
    printExpressionBeginLambda(TypeCheck3.typeOf(node));
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
    printExpressionBeginLambda(TypeCheck3.typeOf(node));
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
    printExpressionBeginLambda(TypeCheck3.typeOf(node));
    node.getOCLVariableDeclarationList().forEach(dec -> dec.accept(getTraverser()));
    this.getPrinter().print("return ");
    node.getExpression().accept(getTraverser());
    this.getPrinter().println(";");
    printExpressionEndLambda();
  }

  @Override
  public void handle(ASTIterateExpression node) {
    printExpressionBeginLambda(TypeCheck3.typeOf(node.getInit().getExpression()));
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
      SymTypeExpression type = TypeCheck3.symTypeFromAST(node.getMCType());
      if (!type.isObscureType()) {
        innerType = type;
      }
    }
    else if (node.isPresentExpression()) {
      SymTypeExpression type = TypeCheck3.typeOf(node.getExpression());
      if (!type.isObscureType()
          && type.isGenericType()
          && ((SymTypeOfGenerics) type).sizeArguments() == 1) {
        innerType = ((SymTypeOfGenerics) type).getArgument(0);
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
    } else {
      Log.error(
          UNEXPECTED_STATE_AST_NODE, node.get_SourcePositionStart(), node.get_SourcePositionEnd());
    }
  }

  @Override
  public void handle(ASTOCLVariableDeclaration node) {
    // variable is not final for iterate expressions
    if (node.isPresentMCType()) {
      getPrinter().print(boxType(TypeCheck3.symTypeFromAST(node.getMCType())));
    }
    else if (node.isPresentExpression()) {
      getPrinter().print(boxType(TypeCheck3.typeOf(node.getExpression())));
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
  public void handle(ASTAnyExpression node) {
    this.getPrinter().print("(");
    node.getExpression().accept(getTraverser());
    this.getPrinter().print(").iterator().next()");
  }

  /**
   * if node has a primitive type, this prints the Java expression such that it has a non-primitive
   * type. e.g. {@code 5} to {@code ((Integer) 5)}
   *
   * @param node the expression to be printed
   */
  protected void printAsBoxedType(ASTExpression node) {
    SymTypeExpression type = TypeCheck3.typeOf(node);
    if (type.isObscureType()) {
      Log.error(NO_TYPE_DERIVED_ERROR, node.get_SourcePositionStart());
      return;
    }
    if (type.isPrimitive()) {
      getPrinter().print("((");
      this.getPrinter().print((type.printFullName()));
      getPrinter().print(") ");
      node.accept(getTraverser());
      getPrinter().print(")");
    } else {
      node.accept(getTraverser());
    }
  }
}
