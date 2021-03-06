// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.ocl.prettyprint;

import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.prettyprint.IndentPrinter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OCLPrettyPrinterTest extends AbstractTest {

  @ParameterizedTest
  @MethodSource("getParsableModels")
  public void testOCLCompilationUnit(String filename) throws IOException {
    // given
    final Optional<ASTOCLCompilationUnit> ast = parse(prefixValidModelsPath(filename), false);
    final OCLFullPrettyPrinter printer = new OCLFullPrettyPrinter(new IndentPrinter());
    assertThat(ast).isNotNull();

    // when
    String output = printer.prettyprint(ast.get());

    // then
    OCLParser parser = new OCLParser();
    final Optional<ASTOCLCompilationUnit> astPrint = parser.parse_StringOCLCompilationUnit(output);
    assertTrue(astPrint.isPresent());
    assertTrue(ast.get().deepEquals(astPrint.get()));
  }
}
