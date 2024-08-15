/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import com.google.common.base.Preconditions;
import de.monticore.ast.ASTNode;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.ocl._ast.ASTOCLArtifact;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLContextDefinition;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._ast.ASTOCLParamDeclaration;
import de.monticore.ocl.ocl._visitor.OCLHandler;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl._visitor.OCLVisitor2;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.IDerive;
import de.monticore.types.check.ISynthesize;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class OCLPrinter extends AbstractPrinter implements OCLHandler, OCLVisitor2 {

  protected OCLTraverser traverser;

  /** @deprecated use other Constructor (requires TypeCheck3) */
  @Deprecated
  public OCLPrinter(
      IndentPrinter printer, VariableNaming naming, IDerive deriver, ISynthesize syntheziser) {
    this(printer, naming);
    this.deriver = deriver;
    this.syntheziser = syntheziser;
  }

  public OCLPrinter(IndentPrinter printer, VariableNaming naming) {
    Preconditions.checkNotNull(printer);
    Preconditions.checkNotNull(naming);
    this.printer = printer;
    this.naming = naming;
  }

  @Override
  public OCLTraverser getTraverser() {
    return this.traverser;
  }

  @Override
  public void setTraverser(OCLTraverser traverser) {
    Preconditions.checkNotNull(traverser);
    this.traverser = traverser;
  }

  public IndentPrinter getPrinter() {
    return this.printer;
  }

  @Override
  public void visit(ASTOCLCompilationUnit node) {
    Preconditions.checkNotNull(node);
    if (node.isPresentPackage() && !node.getPackage().isEmpty()) {
      this.getPrinter().print("package ");
      this.getPrinter().print(node.getPackage());
      this.getPrinter().println(";");
      this.getPrinter().println();
      this.getPrinter().println("import de.se_rwth.commons.logging.Log;");
      this.getPrinter().println();
    }

    for (ASTMCImportStatement is : node.getMCImportStatementList()) {
      this.getPrinter().println(this.printImportStatement(is));
    }
  }

  @Override
  public void visit(ASTOCLArtifact node) {
    Preconditions.checkNotNull(node);
    this.getPrinter().print("public ");
    this.getPrinter().print("class ");
    this.getPrinter().print(node.getName());
    this.getPrinter().println(" {");
    this.getPrinter().indent();
    this.getPrinter().println();
  }

  @Override
  public void endVisit(ASTOCLArtifact node) {
    Preconditions.checkNotNull(node);
    this.getPrinter().unindent();
    this.getPrinter().println("}");
  }

  protected String printImportStatement(ASTMCImportStatement ast) {
    Preconditions.checkNotNull(ast);
    if (ast.getQName().isEmpty()) {
      Log.debug("Empty import statement.", "OCL2Java");
      return "";
    }
    StringBuilder sb = new StringBuilder();
    sb.append("import").append(" ").append(ast.getQName());
    if (ast.isStar()) {
      sb.append(".*");
    }
    sb.append(";");
    return sb.toString();
  }

  @Override
  public void handle(ASTOCLContextDefinition node) {
    if (node.isPresentMCType()) {
      this.getPrinter().print(boxType(TypeCheck3.symTypeFromAST(node.getMCType())));
    } else if (node.isPresentOCLParamDeclaration()) {
      node.getOCLParamDeclaration().accept(this.getTraverser());
    } else {
      Log.error(UNEXPECTED_STATE_AST_NODE, node.get_SourcePositionStart());
    }
  }

  @Override
  public void handle(ASTOCLParamDeclaration node) {
    this.getPrinter().print(boxType(TypeCheck3.symTypeFromAST(node.getMCType())));
    this.getPrinter().print(" ");
    this.printer.print(node.getName());
    if (node.isPresentExpression()) {
      this.getPrinter().print(" = ");
      node.getExpression().accept(this.getTraverser());
    }
  }

  @Override
  public void handle(ASTOCLInvariant node) {
    this.getPrinter().print("@SuppressWarnings(\"unchecked\")\n");
    this.getPrinter().print("public static Boolean check");

    if (node.isPresentName()) {
      this.getPrinter().print(node.getName());
    } else {
      this.getPrinter().print(this.getNaming().getName(node));
    }

    this.getPrinter().print("(");

    List<ASTNode> parameters = new LinkedList<>();
    parameters.addAll(node.getOCLContextDefinitionList());
    parameters.addAll(node.getOCLParamDeclarationList());
    this.printList(parameters, ", ");

    this.getPrinter().println(") {");
    this.getPrinter().indent();

    this.getPrinter().print("Boolean ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println(" = true;");
    this.getPrinter().println("try {");
    this.getPrinter().indent();
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().print(" = ");
    node.getExpression().accept(this.getTraverser());
    this.getPrinter().println(";");
    this.getPrinter().unindent();
    this.getPrinter().print("} catch (Exception ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println("Exception) {");
    this.getPrinter().indent();
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println(" = false;");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println("Exception.printStackTrace();");
    this.getPrinter().print("Log.error(\"Error while executing ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().print("() !\", ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println("Exception);");
    this.getPrinter().println("}");
    this.getPrinter().unindent();
    this.getPrinter().print("return ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println(";");
    this.getPrinter().println("}");
    this.getPrinter().unindent();
    this.getPrinter().println();
  }

  /**
   * Prints a collection
   *
   * @param items to be printed
   * @param seperator string for seperating items
   */
  protected void printList(Collection<? extends ASTNode> items, String seperator) {
    // print by iterate through all items
    Iterator<? extends ASTNode> iter = items.iterator();
    String sep = "";
    while (iter.hasNext()) {
      this.getPrinter().print(sep);
      iter.next().accept(this.getTraverser());
      sep = seperator;
    }
  }
}
