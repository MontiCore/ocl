/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.expressions.prettyprint;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.expressions.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.ocl._visitor.OCLVisitor;
import de.monticore.prettyprint.CommentPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.Names;

public class OCLPrettyPrinter implements OCLVisitor {

  protected OCLVisitor realThis;
  protected IndentPrinter printer;

  public OCLPrettyPrinter(IndentPrinter printer) {
    this.printer = printer;
    realThis = this;
  }

  @Override
  public void setRealThis(OCLVisitor realThis) {
    this.realThis = realThis;
  }

  @Override
  public OCLVisitor getRealThis() {
    return realThis;
  }

  public IndentPrinter getPrinter() {
    return printer;
  }

  public String prettyprint(ASTOCLCompilationUnit compilationUnit) {
    getPrinter().clearBuffer();
    compilationUnit.accept(getRealThis());
    return getPrinter().getContent();
  }

  @Override
  public void handle(ASTOCLCompilationUnit unit) {
    CommentPrettyPrinter.printPreComments(unit, getPrinter());
    if (unit.getPackageList() != null && !unit.getPackageList().isEmpty()) {
      printer
          .println("package " + Names.getQualifiedName(unit.getPackageList()) + ";\n");
    }
    if (unit.getMCImportStatementList() != null && !unit.getMCImportStatementList().isEmpty()) {
      unit.getMCImportStatementList().forEach(i -> i.accept(getRealThis()));
      getPrinter().println();
    }
    unit.getOCLArtifact().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(unit, getPrinter());
  }

  @Override
  public void handle(ASTOCLArtifact node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    printer.print("ocl ");

    printer.println(node.getName() + " {");

    node.getOCLConstraintList().forEach(c -> {
      c.accept(getRealThis());
      printer.println();
    });

    printer.print(" }");

    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLOperationConstraint node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    // TODO print stereotypes
    printer.print("context ");
    node.getOCLOperationSignature().accept(getRealThis());
    printer.println();

    if (!node.isEmptyOCLVariableDeclarations()) {
      printer.print("let: ");
      for (ASTOCLVariableDeclaration vd : node.getOCLVariableDeclarationList()) {
        vd.accept(getRealThis());
        printer.println("; ");
      }
      printer.println();
    }

    if (!node.getPreConditionsList().isEmpty()) {
      printer.print("pre: ");
      for (ASTExpression e : node.getPreConditionsList()){
        e.accept(getRealThis());
        printer.print("; ");
      }
      printer.println();
    }

    if (!node.getPostConditionsList().isEmpty()) {
      printer.print("post: ");
      printer.print(": ");
      for (ASTExpression e : node.getPostConditionsList()){
        e.accept(getRealThis());
        printer.print("; ");
      }
      printer.println();
    }

    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLInvariant node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    if (node.isContext()) {
      printer.println("context");
      node.getOCLContextDefinitionList().forEach(c -> {
        c.accept(getRealThis());
        printer.println();
      });
      printer.print(" ");
    }
    else if (node.isImport()) {
      printer.println("import");
      node.getOCLContextDefinitionList().forEach(c -> {
        c.accept(getRealThis());
        printer.println();
      });
      printer.print(" ");
    }



    printer.print("inv");

    if (node.isPresentName()) {
      printer.print(" " + node.getName());
    }

    if (!node.getParamsList().isEmpty()) {
      printer.print("(");
      for (int i = 0; i < node.getParamsList().size(); i++) {
        if (i != 0) {
          getPrinter().print(", ");
        }
        node.getParams(i).accept(getRealThis());
      }
      printer.print(")");
    }

    printer.println(":");

    node.getExpressionList().forEach(s -> {
      s.accept(getRealThis());
      printer.println();
    });

    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLContextDefinition node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    if (node.isPresentMCType()) {
      node.getMCType().accept(getRealThis());
    }
    else if (node.isPresentExpression()) {
      node.getExpression().accept(getRealThis());
    }

    CommentPrettyPrinter.printPreComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLMethodSignature node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    if (node.isPresentMCReturnType()) {
      node.getMCReturnType().accept(getRealThis());
      printer.print(" ");
    }

    printer.print(node.getMethodName());

    printer.print("(");
    for (int i = 0; i < node.getParamsList().size(); i++) {
      if (i != 0) {
        getPrinter().print(", ");
      }
      node.getParams(i).accept(getRealThis());
    }
    printer.print(")");

    if (!node.getThrowablesList().isEmpty()) {
      printer.print(" ");
      printer.print("throws " + Joiners.COMMA.join(node.getThrowablesList()));
    }

    CommentPrettyPrinter.printPreComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLConstructorSignature node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    printer.print("new " + node.getReferenceType());
    printer.print("(");
    for (int i = 0; i < node.getParamsList().size(); i++) {
      if (i != 0) {
        getPrinter().print(", ");
      }
      node.getParams(i).accept(getRealThis());
    }
    printer.print(")");

    if (!node.getThrowablesList().isEmpty()) {
      printer.print(" ");
      printer.print("throws " + Joiners.COMMA.join(node.getThrowablesList()));
    }

    CommentPrettyPrinter.printPreComments(node, getPrinter());
  }
}
