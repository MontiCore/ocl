// (c) https://github.com/MontiCore/monticore

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
import java.util.List;

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
      getPrinter().println("package " + Names.getQualifiedName(unit.getPackageList()) + ";\n");
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
    getPrinter().print("ocl ");

    getPrinter().println(node.getName() + " {");
    getPrinter().indent();

    for (int i = 0; i < node.getOCLConstraintList().size(); i++) {
      if (i != 0) {
        getPrinter().println();
      }
      ASTOCLConstraint c = node.getOCLConstraint(i);
      c.accept(getTraverser());
    }

    getPrinter().unindent();
    getPrinter().print("}");

    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLOperationConstraint node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    for (ASTStereotype stereotype : node.getStereotypeList()) {
      stereotype.accept(getTraverser());
    }

    getPrinter().print("context ");
    node.getOCLOperationSignature().accept(getTraverser());
    getPrinter().println();

    getPrinter().indent();
    if (!node.isEmptyOCLVariableDeclarations()) {
      getPrinter().print("let ");

      Iterator<ASTOCLVariableDeclaration> it = node.getOCLVariableDeclarationList().iterator();
      while (it.hasNext()) {
        it.next().accept(getTraverser());
        if (it.hasNext()) {
          getPrinter().println("; ");
        }
      }
      getPrinter().println();
    }

    if (!node.getPreConditionList().isEmpty()) {
      getPrinter().print("pre: ");
      for (ASTExpression e : node.getPreConditionList()) {
        e.accept(getTraverser());
        getPrinter().print("; ");
      }
      getPrinter().println();
    }

    if (!node.getPostConditionList().isEmpty()) {
      getPrinter().print("post: ");
      for (ASTExpression e : node.getPostConditionList()) {
        e.accept(getTraverser());
        getPrinter().print("; ");
      }
      getPrinter().println();
    }
    getPrinter().unindent();

    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLInvariant node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    for (ASTStereotype stereotype : node.getStereotypeList()) {
      stereotype.accept(getTraverser());
    }

    if (node.isContext()) {
      getPrinter().print("context ");
    }
    else if (node.isImport()) {
      getPrinter().print("import ");
    }

    for (int i = 0; i < node.getOCLContextDefinitionList().size(); i++) {
      ASTOCLContextDefinition c = node.getOCLContextDefinition(i);
      c.accept(getTraverser());
      if (i < node.getOCLContextDefinitionList().size() - 1) {
        getPrinter().print(", ");
      }
    }
    if (node.isContext() || node.isImport()) {
      getPrinter().println();
    }

    getPrinter().print("inv");

    if (node.isPresentName()) {
      getPrinter().print(" " + node.getName());
    }

    if (!node.getOCLParamDeclarationList().isEmpty()) {
      printOCLParamDeclarationList(node.getOCLParamDeclarationList());
    }

    getPrinter().println(":");

    getPrinter().indent();
    node.getExpression().accept(getTraverser());
    getPrinter().println(";");
    getPrinter().unindent();

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
    else if (node.isPresentOCLParamDeclaration()) {
      node.getOCLParamDeclaration().accept(getTraverser());
    }

    CommentPrettyPrinter.printPreComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLMethodSignature node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    if (node.isPresentMCReturnType()) {
      node.getMCReturnType().accept(getTraverser());
      getPrinter().print(" ");
    }

    getPrinter().print(node.getMethodName());
    printOCLParamDeclarationList(node.getOCLParamDeclarationList());

    if (!node.getThrowablesList().isEmpty()) {
      getPrinter().print(" ");
      getPrinter().print("throws " + Joiners.COMMA.join(node.getThrowablesList()));
    }

    CommentPrettyPrinter.printPreComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLConstructorSignature node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());

    getPrinter().print("new " + node.getName());
    printOCLParamDeclarationList(node.getOCLParamDeclarationList());

    if (!node.getThrowablesList().isEmpty()) {
      getPrinter().print(" ");
      getPrinter().print("throws " + Joiners.COMMA.join(node.getThrowablesList()));
    }

    CommentPrettyPrinter.printPreComments(node, getPrinter());
  }

  @Override public void handle(ASTOCLParamDeclaration node) {
    node.getMCType().accept(getTraverser());
    getPrinter().print(" " + node.getName());
    if (node.isPresentExpression()) {
      getPrinter().print(" = ");
      node.getExpression().accept(getTraverser());
    }
  }

  protected void printOCLParamDeclarationList(List<ASTOCLParamDeclaration> listToPrint) {
    getPrinter().print("(");
    for (int i = 0; i < listToPrint.size(); i++) {
      if (i != 0) {
        getPrinter().print(", ");
      }
      listToPrint.get(i).accept(getTraverser());
    }
    getPrinter().print(")");
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
