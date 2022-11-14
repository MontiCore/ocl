// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.codegen.visitors;

import com.google.common.base.Preconditions;
import de.monticore.ocl.oclexpressions._ast.ASTInDeclaration;
import de.monticore.ocl.oclexpressions._ast.ASTInDeclarationVariable;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.prettyprint.IndentPrinter;

/**
 * As the AST structure does not always resemble the Structure of the printed code,
 * we sometimes need to have another printing run, only for missing brackets
 */
public class EndingBracketPrinter implements OCLExpressionsHandler {

  protected OCLExpressionsTraverser traverser;

  protected IndentPrinter printer;

  public EndingBracketPrinter(IndentPrinter printer) {
    Preconditions.checkNotNull(printer);
    this.printer = printer;
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
  public void handle(ASTInDeclaration node) {
    for (ASTInDeclarationVariable var : node.getInDeclarationVariableList()) {
      this.getPrinter().unindent();
      this.getPrinter().println("}");
    }
  }

}
