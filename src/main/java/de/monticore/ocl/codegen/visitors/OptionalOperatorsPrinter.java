/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.ocl.optionaloperators._ast.*;
import de.monticore.ocl.types.check.OCLDeriver;
import de.monticore.ocl.types.check.OCLSynthesizer;
import de.monticore.prettyprint.IndentPrinter;
import de.se_rwth.commons.logging.Log;
import org.assertj.core.util.Preconditions;

public class OptionalOperatorsPrinter extends AbstractPrinter implements OCLExpressionsHandler,
  OCLExpressionsVisitor2 {
  
  protected OCLExpressionsTraverser traverser;
  
  public OptionalOperatorsPrinter(IndentPrinter printer, VariableNaming naming,
                                  OCLDeriver oclDeriver, OCLSynthesizer oclSynthesizer) {
    Preconditions.checkNotNull(printer);
    Preconditions.checkNotNull(naming);
    Preconditions.checkNotNull(oclDeriver);
    Preconditions.checkNotNull(oclSynthesizer);
    this.printer = printer;
    this.naming = naming;
    this.oclDeriver = oclDeriver;
    this.oclSynthesizer = oclSynthesizer;
  }
  
  protected OCLDeriver getOclDeriver() {
    return this.oclDeriver;
  }
  
  protected IndentPrinter getPrinter() {
    return this.printer;
  }
  
  public OCLExpressionsTraverser getTraverser() {
    return this.traverser;
  }
  
  public void setTraverser(OCLExpressionsTraverser traverser) {
    Preconditions.checkNotNull(traverser);
    this.traverser = traverser;
  }
  
  public void handle(ASTOptionalLessEqualExpression node) {
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".isPresent() ?");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".get() <= ");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(".get() : false;");
  }
  
  public void handle(ASTOptionalGreaterEqualExpression node) {
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".isPresent() ?");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".get() >= ");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(".get() : false;");
  }
  
  public void handle(ASTOptionalLessThanExpression node) {
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".isPresent() ?");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".get() < ");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(".get() : false;");
  }
  
  public void handle(ASTOptionalGreaterThanExpression node) {
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".isPresent() ?");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".get() > ");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(".get() : false;");
  }
  
  public void handle(ASTOptionalEqualsExpression node) {
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".isPresent() ?");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".get() == ");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(".get() : false;");
  }
  
  public void handle(ASTOptionalNotEqualsExpression node) {
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".isPresent() ?");
    node.getLeft().accept(getTraverser());
    this.getPrinter().print(".get() != ");
    node.getRight().accept(getTraverser());
    this.getPrinter().print(".get() : false;");
  }
  
  public void handle(ASTOptionalSimilarExpression node) {
    Log.error("implementation not available, to be discussed");
  }
  
  public void handle(ASTOptionalNotSimilarExpression node) {
    Log.error("implementation not available, to be discussed");
  }
}
