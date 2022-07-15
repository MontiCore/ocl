/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;/* (c) https://github.com/MontiCore/monticore */

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.optionaloperators._ast.ASTOptionalEqualsExpression;
import de.monticore.ocl.types.check.OCLDeriver;
import de.monticore.ocl.types.check.OCLSynthesizer;
import de.monticore.prettyprint.IndentPrinter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class OptionalOperatorsPrinterTest extends AbstractTest {
  
  @Test
  public void test() throws IOException {
    OCLParser parser = new OCLParser();
    OptionalOperatorsPrinter printer = new OptionalOperatorsPrinter(new IndentPrinter(), new VariableNaming(), new OCLDeriver(), new OCLSynthesizer());
    final Optional<ASTExpression> optAST = parser.parse_StringExpression("x ?== y");
    assertTrue(optAST.isPresent());
    final ASTExpression ast = optAST.get();
    printer.getPrinter().clearBuffer();
    printer.handle((ASTOptionalEqualsExpression) ast);
    String result = printer.getPrinter().getContent();
    assertEquals(result, "x.isPresent() ? x.get==y.get : false;");
  }
}
