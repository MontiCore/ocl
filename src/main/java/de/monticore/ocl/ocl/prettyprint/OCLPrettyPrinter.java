/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl.prettyprint;

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
    unit.getOCLFile().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(unit, getPrinter());
  }

  @Override
  public void handle(ASTOCLFile node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    printer.print(node.getPrefix() + " ");

    if (node.isPresentFileName()) {
      printer.println(node.getFileName() + " {");
    }

    node.getOCLConstraintList().forEach(c -> {
      c.accept(getRealThis());
      printer.println();
    });

    if (node.isPresentFileName()) {
      printer.print(" }");
    }

    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLOperationConstraint node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    // TODO print stereotypes
    printer.print("context ");
    node.getOCLOperationSignature().accept(getRealThis());
    printer.println();

    if (node.isPresentExpression()) {
      node.getExpression().accept(getRealThis());
      printer.println();
    }

    if (node.isPresentOCLPreStatement()) {
      node.getOCLPreStatement().accept(getRealThis());
      printer.println();
    }

    if (node.isPresentOCLPostStatement()) {
      node.getOCLPostStatement().accept(getRealThis());
      printer.println();
    }

    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLInvariant node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    if (node.isPresentOCLClassContext()) {
      node.getOCLClassContext().accept(getRealThis());
      printer.print(" ");
    }

    printer.print("inv");

    if (node.isPresentName()) {
      printer.print(" " + node.getName());
    }

    if (node.isPresentOCLParameters()) {
      node.getOCLParameters().accept(getRealThis());
    }

    printer.println(":");

    node.getStatementsList().forEach(s -> {
      s.accept(getRealThis());
      printer.println();
    });

    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLClassContext node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    if (node.isContext()) {
      printer.println("context");
    }
    else if (node.isImport()) {
      printer.println("import");
    }

    node.getOCLContextDefinitionList().forEach(c -> {
      c.accept(getRealThis());
      printer.println();
    });

    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLContextDefinition node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    if (node.isPresentOCLExtType()) {
      node.getOCLExtType().accept(getRealThis());
    }
    else if (node.isPresentExpression()) {
      node.getExpression().accept(getRealThis());
    }

    CommentPrettyPrinter.printPreComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLPreStatement node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    printer.print("pre");
    if (node.isPresentName()) {
      printer.print(" " + node.getName());
    }
    printer.println(":");

    node.getStatementsList().forEach(s -> {
      s.accept(getRealThis());
      printer.println(";");
    });

    CommentPrettyPrinter.printPreComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLPostStatement node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    printer.print("post");
    if (node.isPresentName()) {
      printer.print(" " + node.getName());
    }
    printer.println(":");

    node.getStatementsList().forEach(s -> {
      s.accept(getRealThis());
      printer.println(";");
    });

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

    node.getOCLParameters().accept(getRealThis());

    if (node.isPresentOCLThrowsClause()) {
      printer.print(" ");
      node.getOCLThrowsClause().accept(getRealThis());
    }

    CommentPrettyPrinter.printPreComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLConstructorSignature node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    printer.print("new " + node.getReferenceType());
    node.getOCLParameters().accept(getRealThis());

    if (node.isPresentOCLThrowsClause()) {
      printer.print(" ");
      node.getOCLThrowsClause().accept(getRealThis());
    }

    CommentPrettyPrinter.printPreComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLParameters node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    printer.print("(");
    for (int i = 0; i < node.getParamsList().size(); i++) {
      if (i != 0) {
        getPrinter().print(", ");
      }
      node.getParams(i).accept(getRealThis());
    }
    printer.print(")");

    CommentPrettyPrinter.printPreComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLThrowsClause node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    printer.print("throws " + Joiners.COMMA.join(node.getThrowablesList()));
    CommentPrettyPrinter.printPreComments(node, getPrinter());
  }

  @Override
  public void handle(ASTStereotypeExpr node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    node.getStereotype().accept(getRealThis());
    printer.print(" ");
    node.getExpression().accept(getRealThis());

    CommentPrettyPrinter.printPreComments(node, getPrinter());
  }
}
