/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl.prettyprint;

import de.monticore.ast.ASTNode;
import de.monticore.expressions.prettyprint.BitExpressionsPrettyPrinter;
import de.monticore.expressions.prettyprint.CommonExpressionsPrettyPrinter;
import de.monticore.expressions.prettyprint.ExpressionsBasisPrettyPrinter;
import de.monticore.literals.prettyprint.MCCommonLiteralsPrettyPrinter;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLArtifact;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.oclexpressions.prettyprint.OCLExpressionsPrettyPrinter;
import de.monticore.ocl.optionaloperators.prettyprint.OptionalOperatorsPrettyPrinter;
import de.monticore.ocl.setexpressions.prettyprint.SetExpressionsPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.prettyprint.MCBasicsPrettyPrinter;
import de.monticore.prettyprint.UMLStereotypePrettyPrinter;
import de.monticore.types.prettyprint.MCBasicTypesPrettyPrinter;
import de.monticore.types.prettyprint.MCCollectionTypesPrettyPrinter;
import de.monticore.types.prettyprint.MCSimpleGenericTypesPrettyPrinter;

public class OCLFullPrettyPrinter {
  protected IndentPrinter printer;
  protected OCLTraverser traverser;

  public OCLFullPrettyPrinter() {
    this(new IndentPrinter());
  }

  public OCLFullPrettyPrinter(IndentPrinter printer) {
    this.printer = printer;
    traverser = OCLMill.traverser();

    OCLPrettyPrinter oclPP = new OCLPrettyPrinter(printer);
    UMLStereotypePrettyPrinter stereoPP = new UMLStereotypePrettyPrinter(printer);
    MCSimpleGenericTypesPrettyPrinter genericPP = new MCSimpleGenericTypesPrettyPrinter(printer);
    SetExpressionsPrettyPrinter setPP = new SetExpressionsPrettyPrinter(printer);
    OCLExpressionsPrettyPrinter oclexPP = new OCLExpressionsPrettyPrinter(printer);
    OptionalOperatorsPrettyPrinter optPP = new OptionalOperatorsPrettyPrinter(printer);
    BitExpressionsPrettyPrinter bitPP = new BitExpressionsPrettyPrinter(printer);

    ExpressionsBasisPrettyPrinter expPP = new ExpressionsBasisPrettyPrinter(printer);
    MCBasicsPrettyPrinter mcbPP = new MCBasicsPrettyPrinter(printer);
    MCCollectionTypesPrettyPrinter ctPP = new MCCollectionTypesPrettyPrinter(printer);
    MCBasicTypesPrettyPrinter btPP = new MCBasicTypesPrettyPrinter(printer);
    MCCommonLiteralsPrettyPrinter clPP = new MCCommonLiteralsPrettyPrinter(printer);
    CommonExpressionsPrettyPrinter cePP = new CommonExpressionsPrettyPrinter(printer);
    BitExpressionsPrettyPrinter bePP = new BitExpressionsPrettyPrinter(printer);

    traverser.setOCLHandler(oclPP);
    traverser.add4UMLStereotype(stereoPP);
    traverser.setUMLStereotypeHandler(stereoPP);
    traverser.add4MCSimpleGenericTypes(genericPP);
    traverser.setMCSimpleGenericTypesHandler(genericPP);
    traverser.setSetExpressionsHandler(setPP);
    traverser.setOCLExpressionsHandler(oclexPP);
    traverser.setOptionalOperatorsHandler(optPP);
    traverser.add4BitExpressions(bitPP);
    traverser.setBitExpressionsHandler(bitPP);
    traverser.add4ExpressionsBasis(expPP);
    traverser.setExpressionsBasisHandler(expPP);
    traverser.add4MCBasics(mcbPP);
    traverser.add4MCCollectionTypes(ctPP);
    traverser.setMCCollectionTypesHandler(ctPP);
    traverser.add4MCBasicTypes(btPP);
    traverser.setMCBasicTypesHandler(btPP);
    traverser.add4MCCommonLiterals(clPP);
    traverser.setMCCommonLiteralsHandler(clPP);
    traverser.add4CommonExpressions(cePP);
    traverser.setCommonExpressionsHandler(cePP);
    traverser.add4BitExpressions(bePP);
    traverser.setBitExpressionsHandler(bePP);
  }

  public String prettyprint(ASTOCLCompilationUnit node) {
    getPrinter().clearBuffer();
    node.accept(getTraverser());
    return getPrinter().getContent();
  }

  public String prettyprint(ASTOCLArtifact node) {
    getPrinter().clearBuffer();
    node.accept(getTraverser());
    return getPrinter().getContent();
  }

  public String prettyprint(ASTNode node) {
    getPrinter().clearBuffer();
    node.accept(getTraverser());
    return getPrinter().getContent();
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

  public OCLTraverser getTraverser() {
    return traverser;
  }

  public void setTraverser(OCLTraverser traverser) {
    this.traverser = traverser;
  }
}
