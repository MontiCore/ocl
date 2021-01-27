/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl.prettyprint;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.ocl._visitor.OCLHandler;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.prettyprint.CommentPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.umlstereotype._ast.ASTStereotype;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.Names;

import java.util.Iterator;

public class OCLPrettyPrinter implements OCLHandler {

  protected IndentPrinter printer;
  protected OCLTraverser traverser;

  public OCLPrettyPrinter(IndentPrinter printer) {
    this.printer = printer;
  }

  @Override
  public void handle(ASTOCLCompilationUnit unit) {
    CommentPrettyPrinter.printPreComments(unit, getPrinter());
    if (unit.getPackageList() != null && !unit.getPackageList().isEmpty()) {
      printer
          .println("package " + Names.getQualifiedName(unit.getPackageList()) + ";\n");
    }
    if (unit.getMCImportStatementList() != null && !unit.getMCImportStatementList().isEmpty()) {
      unit.getMCImportStatementList().forEach(i -> i.accept(getTraverser()));
      getPrinter().println();
    }
    unit.getOCLArtifact().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(unit, getPrinter());
  }

  @Override
  public void handle(ASTOCLArtifact node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    printer.print("ocl ");

    printer.println(node.getName() + " {");

    node.getOCLConstraintList().forEach(c -> {
      c.accept(getTraverser());
      printer.println();
    });

    printer.print("}");

    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLOperationConstraint node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    for(ASTStereotype stereotype : node.getStereotypeList()){
      stereotype.accept(getTraverser());
    }

    printer.print("context ");
    node.getOCLOperationSignature().accept(getTraverser());
    printer.println();

    if (!node.isEmptyOCLVariableDeclarations()) {
      printer.print("let: ");

      Iterator<ASTOCLVariableDeclaration> it = node.getOCLVariableDeclarationList().iterator();
      while (it.hasNext()) {
        it.next().accept(getTraverser());
        if (it.hasNext()) {
          printer.println("; ");
        }
      }
      printer.println();
    }

    if (!node.getPreConditionList().isEmpty()) {
      printer.print("pre: ");
      for (ASTExpression e : node.getPreConditionList()){
        e.accept(getTraverser());
        printer.print("; ");
      }
      printer.println();
    }

    if (!node.getPostConditionList().isEmpty()) {
      printer.print("post: ");
      for (ASTExpression e : node.getPostConditionList()){
        e.accept(getTraverser());
        printer.print("; ");
      }
      printer.println();
    }

    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLInvariant node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    for(ASTStereotype stereotype : node.getStereotypeList()){
      stereotype.accept(getTraverser());
    }

    if (node.isContext()) {
      printer.println("context");
      node.getOCLContextDefinitionList().forEach(c -> {
        c.accept(getTraverser());
        printer.println();
      });
      printer.print(" ");
    }
    else if (node.isImport()) {
      printer.println("import");
      node.getOCLContextDefinitionList().forEach(c -> {
        c.accept(getTraverser());
        printer.println();
      });
      printer.print(" ");
    }



    printer.print("inv");

    if (node.isPresentName()) {
      printer.print(" " + node.getName());
    }

    if (!node.getOCLParamDeclarationList().isEmpty()) {
      printer.print("(");
      for (int i = 0; i < node.getOCLParamDeclarationList().size(); i++) {
        if (i != 0) {
          getPrinter().print(", ");
        }
        node.getOCLParamDeclaration(i).accept(getTraverser());
      }
      printer.print(")");
    }

    printer.println(":");

    node.getExpression().accept(getTraverser());
    printer.println();

    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLContextDefinition node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    if (node.isPresentMCType()) {
      node.getMCType().accept(getTraverser());
    }
    else if (node.isPresentGeneratorDeclaration()) {
      node.getGeneratorDeclaration().accept(getTraverser());
    }

    CommentPrettyPrinter.printPreComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLMethodSignature node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    if (node.isPresentMCReturnType()) {
      node.getMCReturnType().accept(getTraverser());
      printer.print(" ");
    }

    printer.print(node.getMethodName());

    printer.print("(");
    for (int i = 0; i < node.getOCLParamDeclarationList().size(); i++) {
      if (i != 0) {
        getPrinter().print(", ");
      }
      node.getOCLParamDeclaration(i).accept(getTraverser());
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

    printer.print("new " + node.getName());
    printer.print("(");
    for (int i = 0; i < node.getOCLParamDeclarationList().size(); i++) {
      if (i != 0) {
        getPrinter().print(", ");
      }
      node.getOCLParamDeclaration(i).accept(getTraverser());
    }
    printer.print(")");

    if (!node.getThrowablesList().isEmpty()) {
      printer.print(" ");
      printer.print("throws " + Joiners.COMMA.join(node.getThrowablesList()));
    }

    CommentPrettyPrinter.printPreComments(node, getPrinter());
  }

  /* ============================================================ */
  /* ======================= GENERATED CODE ===================== */
  /* ============================================================ */

  public IndentPrinter getPrinter() {
    return printer;
  }

  public void setPrinter(IndentPrinter printer) {
    this.printer = printer;
  }

  @Override public OCLTraverser getTraverser() {
    return traverser;
  }

  @Override public void setTraverser(OCLTraverser traverser) {
    this.traverser = traverser;
  }
}
