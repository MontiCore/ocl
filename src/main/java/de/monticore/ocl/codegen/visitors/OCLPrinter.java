/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import com.google.common.base.Preconditions;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.ocl._visitor.OCLHandler;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl._visitor.OCLVisitor2;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.se_rwth.commons.logging.Log;

public class OCLPrinter extends AbstractPrinter implements OCLHandler, OCLVisitor2 {

  protected OCLTraverser traverser;

  public OCLPrinter(StringBuilder stringBuilder, VariableNaming naming) {
    Preconditions.checkNotNull(stringBuilder);
    Preconditions.checkNotNull(naming);
    this.stringBuilder = stringBuilder;
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

  @Override
  public void visit(ASTOCLCompilationUnit node) {
    Preconditions.checkNotNull(node);
    if (node.isPresentPackage() && !node.getPackage().isEmpty()) {
      this.getStringBuilder()
        .append("package").append(" ")
        .append(node.getPackage())
        .append(";")
        .append(System.lineSeparator())
        .append(System.lineSeparator());
    }

    for (ASTMCImportStatement is : node.getMCImportStatementList()) {
      this.getStringBuilder().append(this.printImportStatement(is));
      this.getStringBuilder().append(System.lineSeparator());
    }

    if (!node.getMCImportStatementList().isEmpty()) {
      this.getStringBuilder().append(System.lineSeparator());
    }
  }

  @Override
  public void visit(ASTOCLArtifact node) {
    Preconditions.checkNotNull(node);
    this.getStringBuilder()
      .append("public ")
      .append("class ")
      .append(node.getName()).append(" ")
      .append("{ ")
      .append(System.lineSeparator());
  }

  @Override
  public void endVisit(ASTOCLArtifact node) {
    Preconditions.checkNotNull(node);
    this.getStringBuilder().append("}");
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
    this.getStringBuilder().append("@SuppressWarnings(\"unchecked\")\n");
    this.getStringBuilder().append("public static Boolean check");

    if (node.isPresentName()) {
      this.getStringBuilder().append(node.getName());
    } else {
      this.getStringBuilder().append(this.getNaming().getName(node));
    }

    this.getStringBuilder().append("(");

    for (ASTOCLContextDefinition contextDef : node.getOCLContextDefinitionList()) {
      contextDef.accept(this.getTraverser());
    }

    for (ASTOCLParamDeclaration paramDec : node.getOCLParamDeclarationList()) {
      paramDec.accept(this.getTraverser());
    }
    this.getStringBuilder().append(")").append(" { ").append(System.lineSeparator());
    this.getStringBuilder().append("Map<String, Object> witnessElements = new HashMap<>();\n");
    /*TODO
    if(node.isPresentOCLClassContext()) {
      addContextVarsToWitness(sb, node.getOCLClassContext());
    }
    if(node.isPresentOCLParameters()) {
      addOCLParametersToWitness(sb, node.getOCLParameters());
    }*/
    this.getStringBuilder().append("Boolean ").append(this.getNaming().getName(node)).append(" = true;\n");
    this.getStringBuilder().append("try {\n");

    node.getExpression().accept(this.getTraverser());

    this.getStringBuilder().append("} catch (Exception ").append(this.getNaming().getName(node)).append("Exception) {\n");
    this.getStringBuilder().append(this.getNaming().getName(node)).append(" = false;\n");
    this.getStringBuilder().append(this.getNaming().getName(node)).append("Exception.printStackTrace();\n");
    this.getStringBuilder().append("Log.error(\"Error while executing ").append(this.getNaming().getName(node)).append("() !\");\n");
    this.getStringBuilder().append("}\n");
    this.getStringBuilder().append("return ").append(this.getNaming().getName(node)).append(";\n");
    this.getStringBuilder().append("}\n");
  }
}
