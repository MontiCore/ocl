// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.ocl.prettyprint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl._prettyprint.OCLFullPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class OCLPrettyPrinterTest extends AbstractTest {

  @ParameterizedTest
  @MethodSource("getParsableModels")
  public void testOCLCompilationUnit(String filename) throws IOException {
    // given
    final Optional<ASTOCLCompilationUnit> ast =
        parse(prefixValidModelsPath("/testinput/validGrammarModels/" + filename), false);
    assertThat(ast).isNotNull();

    // when
    String output = new OCLFullPrettyPrinter(new IndentPrinter(), true).prettyprint(ast.get());

    // then
    OCLParser parser = new OCLParser();
    final Optional<ASTOCLCompilationUnit> astPrint = parser.parse_StringOCLCompilationUnit(output);
    assertTrue(astPrint.isPresent());
    assertTrue(ast.get().deepEquals(astPrint.get()));
  }
}
