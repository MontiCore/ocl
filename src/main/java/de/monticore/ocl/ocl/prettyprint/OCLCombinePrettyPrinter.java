/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl.prettyprint;

import de.monticore.MCCommonLiteralsPrettyPrinter;
import de.monticore.expressions.prettyprint.CommonExpressionsPrettyPrinter;
import de.monticore.expressions.prettyprint.ExpressionsBasisPrettyPrinter;
import de.monticore.expressions.prettyprint.OCLExpressionsPrettyPrinter;
import de.monticore.ocl.ocl._ast.ASTCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLFile;
import de.monticore.ocl.ocl._visitor.OCLDelegatorVisitor;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.prettyprint.UMLStereotypePrettyPrinter;
import de.monticore.types.prettyprint.MCBasicTypesPrettyPrinter;
import de.monticore.types.prettyprint.MCCollectionTypesPrettyPrinter;

public class OCLCombinePrettyPrinter
    extends OCLDelegatorVisitor {
  private final IndentPrinter printer;
  // TODO SVa: write pretty printer class to use in the other visitors

  public OCLCombinePrettyPrinter(IndentPrinter ip) {
    this.printer = ip;
    setRealThis(this);

    setMCCommonLiteralsVisitor(new MCCommonLiteralsPrettyPrinter(printer));
    setMCBasicTypesVisitor(new MCBasicTypesPrettyPrinter(printer));
    setMCCollectionTypesVisitor(new MCCollectionTypesPrettyPrinter(printer));
    setExpressionsBasisVisitor(new ExpressionsBasisPrettyPrinter(printer));
    setCommonExpressionsVisitor(new CommonExpressionsPrettyPrinter(printer));
    setOCLExpressionsVisitor(new OCLExpressionsPrettyPrinter(printer));
    setAdditionalExpressionsVisitor(new AdditionalExpressionsPrettyPrinter(printer));
    setOCLVisitor(new OCLPrettyPrinter(printer));
    setUMLStereotypeVisitor(new UMLStereotypePrettyPrinter(printer));
  }

  public IndentPrinter getPrinter() {
    return printer;
  }

  public String prettyprint(ASTCompilationUnit node) {
    getPrinter().clearBuffer();
    node.accept(getRealThis());
    return getPrinter().getContent();
  }

  public String prettyprint(ASTOCLFile node) {
    getPrinter().clearBuffer();
    node.accept(getRealThis());
    return getPrinter().getContent();
  }
}
