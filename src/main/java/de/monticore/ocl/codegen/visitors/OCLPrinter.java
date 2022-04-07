/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import com.google.common.base.Preconditions;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.ocl._visitor.OCLHandler;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl._visitor.OCLVisitor2;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.se_rwth.commons.logging.Log;

public class OCLPrinter extends AbstractPrinter implements OCLHandler, OCLVisitor2 {

  protected OCLTraverser traverser;

  protected IndentPrinter printer;

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
  public void handle(ASTOCLInvariant node) {
    this.getPrinter().print("@SuppressWarnings(\"unchecked\")\n");
    this.getPrinter().print("public static Boolean check");

    if (node.isPresentName()) {
      this.getPrinter().print(node.getName());
    }
    else {
      this.getPrinter().print(this.getNaming().getName(node));
    }

    this.getPrinter().print("(");

    for (ASTOCLContextDefinition contextDef : node.getOCLContextDefinitionList()) {
      contextDef.accept(this.getTraverser());
    }

    for (ASTOCLParamDeclaration paramDec : node.getOCLParamDeclarationList()) {
      paramDec.accept(this.getTraverser());
    }
    this.getPrinter().println(") {");
    this.getPrinter().indent();
    this.getPrinter().println("Map<String, Object> witnessElements = new HashMap<>();");
    /*TODO
    if(node.isPresentOCLClassContext()) {
      addContextVarsToWitness(sb, node.getOCLClassContext());
    }
    if(node.isPresentOCLParameters()) {
      addOCLParametersToWitness(sb, node.getOCLParameters());
    }*/
    this.getPrinter().print("Boolean ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println(" = true;");
    this.getPrinter().println("try {");
    this.getPrinter().indent();
    node.getExpression().accept(this.getTraverser());
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
    this.getPrinter().println("() !\");");
    this.getPrinter().println("}");
    this.getPrinter().unindent();
    this.getPrinter().print("return ");
    this.getPrinter().print(this.getNaming().getName(node));
    this.getPrinter().println(";");
    this.getPrinter().println("}");
    this.getPrinter().unindent();
  }
}
