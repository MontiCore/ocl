/* (c) https://github.com/MontiCore/monticore */
package de.monticore.numberunit;

import de.monticore.literals.literals._ast.ASTLiteralsNode;
import de.monticore.literals.prettyprint.LiteralsPrettyPrinterConcreteVisitor;
import de.monticore.prettyprint.IndentPrinter;

public class PrintHelper {
  public static String print(ASTLiteralsNode ast) {
    IndentPrinter ip = new IndentPrinter();
    LiteralsPrettyPrinterConcreteVisitor visitor = new LiteralsPrettyPrinterConcreteVisitor(ip);
    return visitor.prettyprint(ast);
  }
}
