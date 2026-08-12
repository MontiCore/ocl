package de.monticore.ocl.codegen.javagen;

import de.monticore.codegen.javagen.SymTypeExpression2JavaConverter;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.ocl._visitor.OCLHandler;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.prettyprint.IndentPrinter;
import de.se_rwth.commons.logging.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OCLJavaGenVisitor implements OCLHandler {
  protected OCLTraverser traverser;
  protected IndentPrinter printer;
  protected String fullPackage;
  protected int anonymousInvCounter = 0;
  protected Map<ASTOCLInvariant, Integer> anonymousInvToNumber = new HashMap<>();
  
  
  public OCLJavaGenVisitor(IndentPrinter printer) {
    this.printer = printer;
  }

  public OCLJavaGenVisitor(IndentPrinter printer, String fullPackage) {
    this.printer = printer;
    this.fullPackage = fullPackage;
  }

  @Override
  public OCLTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(OCLTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void traverse(ASTOCLCompilationUnit node) {
    if (fullPackage != null && !fullPackage.isEmpty()) {
      printer.println("package " + fullPackage + ";");
      printer.println();
    } else if (node.isPresentPackage()) {
      printer.println("package " + node.getPackage() + ";");
      printer.println();
    }

    // Skip model import statements

    // Default imports
    printer.println("import java.util.List;");
    printer.println("import java.util.Set;");
    printer.println("import java.util.HashSet;");
    printer.println("import java.util.Objects;");
    printer.println("import java.util.Optional;");
    printer.println("import java.util.function.Function;");

    addAdditionalImports();
    
    printer.println();

    if (null != node.getOCLArtifact()) {
      node.getOCLArtifact().accept(getTraverser());
    }
  }
  
  public void addAdditionalImports() {
    // Hookpoint for additional imports
  }

  @Override
  public void handle(ASTOCLArtifact node) {
    printer.print("public class ");
    printer.print(node.getName());
    printer.println("{");
    printer.indent();

    OCLHandler.super.handle(node);

    printer.unindent();
    printer.println();
    printer.println("}");
  }

  @Override
  public void handle(ASTOCLParamDeclaration node) {
    printer.print(SymTypeExpression2JavaConverter.printModelTypeAsJavaType(node.getMCType()));
    printer.print(" ");
    printer.print(node.getName());

    if(node.isPresentExpression()){
      // TODO
      Log.warn("Expression for OCLParamDeclaration is not yet supported",
          node.getExpression().get_SourcePositionStart(), node.getExpression().get_SourcePositionEnd());
    }
  }
  
  @Override
  public void handle(ASTOCLInvariant node) {
    // check method
    printMethodSignature(node, "boolean", "check");
    printer.println("{");
    printer.indent();
    
    printer.println("try {");
    printer.indent();
    printer.println();
    
    printer.print("return ");
    node.getExpression().accept(getTraverser());
    printer.println(";");
    printer.unindent();
    
    printer.println("} catch(Exception __e) {");
    printer.indent();
    printer.println("return false;");
    printer.unindent();
    printer.println("}");
    
    printer.unindent();
    printer.println("}");
  }
  
  private void printMethodSignature(ASTOCLInvariant node, String returnType, String prefix) {
    printer.print("public static " + returnType + " " + prefix);
    if (node.isPresentName()) {
      printer.print(node.getName());
    } else {
      printer.print("Inv" + anonymousInvToNumber.computeIfAbsent(node, n -> anonymousInvCounter++));
    }
    
    printer.print("(");
    
    boolean paramPrinted = false;
    for (ASTOCLContextDefinition contextDefinition : node.getOCLContextDefinitionList()) {
      if (paramPrinted) {
        printer.print(", ");
      }
      contextDefinition.accept(getTraverser());
      paramPrinted = true;
      
      // Anonymous parameter
      if (contextDefinition.isPresentMCType()) {
        printer.print(SymTypeExpression2JavaConverter.printModelTypeAsJavaType(contextDefinition.getMCType()));
        printer.print(" _this");
      }
    }
    
    
    for (ASTOCLParamDeclaration paramDeclaration : node.getOCLParamDeclarationList()) {
      if (paramPrinted) {
        printer.print(", ");
      }
      paramDeclaration.accept(getTraverser());
      paramPrinted = true;
    }
    
    printer.println(")");
  }
}
