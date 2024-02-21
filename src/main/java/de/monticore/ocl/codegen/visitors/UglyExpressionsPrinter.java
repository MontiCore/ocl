/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.uglyexpressions._ast.ASTArrayCreator;
import de.monticore.expressions.uglyexpressions._ast.ASTArrayDimensionByExpression;
import de.monticore.expressions.uglyexpressions._ast.ASTClassCreator;
import de.monticore.expressions.uglyexpressions._ast.ASTCreatorExpression;
import de.monticore.expressions.uglyexpressions._ast.ASTInstanceofExpression;
import de.monticore.expressions.uglyexpressions._ast.ASTTypeCastExpression;
import de.monticore.expressions.uglyexpressions._visitor.UglyExpressionsHandler;
import de.monticore.expressions.uglyexpressions._visitor.UglyExpressionsTraverser;
import de.monticore.expressions.uglyexpressions._visitor.UglyExpressionsVisitor2;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.IDerive;
import de.monticore.types.check.ISynthesize;
import de.se_rwth.commons.logging.Log;

public class UglyExpressionsPrinter extends AbstractPrinter
    implements UglyExpressionsHandler, UglyExpressionsVisitor2 {

  protected UglyExpressionsTraverser traverser;

  public UglyExpressionsPrinter(
      IndentPrinter printer, VariableNaming naming, IDerive deriver, ISynthesize synthesizer) {
    this.printer = Log.errorIfNull(printer);
    this.naming = Log.errorIfNull(naming);
    this.deriver = Log.errorIfNull(deriver);
    this.syntheziser = Log.errorIfNull(synthesizer);
  }

  @Override
  public UglyExpressionsTraverser getTraverser() {
    return this.traverser;
  }

  @Override
  public void setTraverser(UglyExpressionsTraverser traverser) {
    Log.errorIfNull(traverser);
    this.traverser = traverser;
  }

  public IndentPrinter getPrinter() {
    return this.printer;
  }

  @Override
  public void handle(ASTInstanceofExpression node) {
    this.getPrinter().print("(");
    node.getExpression().accept(this.getTraverser());
    this.getPrinter().print(" instanceof ");
    getPrinter().print(boxType(getSynthesizer().synthesizeType(node.getMCType())));
    this.getPrinter().print(")");
  }

  @Override
  public void handle(ASTTypeCastExpression node) {
    this.getPrinter().print("((");
    getPrinter().print(boxType(getSynthesizer().synthesizeType(node.getMCType())));
    this.getPrinter().print(") ");
    node.getExpression().accept(getTraverser());
    this.getPrinter().print(")");
  }

  // not part of OCL

  @Override
  public void handle(ASTCreatorExpression node) {
    this.getPrinter().print("(new ");
    node.getCreator().accept(getTraverser());
    this.getPrinter().print(")");
  }

  @Override
  public void handle(ASTClassCreator node) {
    node.getMCType().accept(getTraverser());
    node.getArguments().accept(getTraverser());
  }

  @Override
  public void handle(ASTArrayCreator node) {
    node.getMCType().accept(getTraverser());
    node.getArrayDimensionSpecifier().accept(getTraverser());
  }

  @Override
  public void handle(ASTArrayDimensionByExpression node) {
    for (ASTExpression expr : node.getExpressionList()) {
      getPrinter().print("[");
      expr.accept(getTraverser());
      getPrinter().print("[");
    }
    for (int i = 0; i < node.getDimList().size(); i++) {
      getPrinter().print("[]");
    }
  }
}
