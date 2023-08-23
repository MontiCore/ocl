// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._parser;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import de.monticore.ocl.ocl.AbstractTest;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class OCLParserTest extends AbstractTest {

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  protected static final String RELATIVE_MODEL_PATH = "src/test/resources";

  @ParameterizedTest
  @ValueSource(
      strings = {
        "testinput/cocos/valid/validConstructorName.ocl",
        "testinput/cocos/valid/validConstructorName0.ocl",
        "testinput/cocos/valid/validFileName.ocl",
        "testinput/cocos/valid/validInvariantName.ocl",
        "testinput/cocos/valid/validMethSigName.ocl",
        "testinput/cocos/valid/validParameterDeclarationName.ocl",
        "testinput/cocos/valid/validParameterType.ocl",
        "testinput/cocos/valid/validPostStatementName.ocl",
        "testinput/cocos/valid/validPrePost.ocl",
        "testinput/cocos/valid/validPreStatementName.ocl",
        "testinput/cocos/valid/validVariableName.ocl"
      })
  public void shouldParseValidInput(String fileName) {
    assumeFalse(fileName.endsWith("validMethSigName.ocl"));
    this.parse(Paths.get(RELATIVE_MODEL_PATH, fileName).toString(), false);
    assertNoFindings();
  }

  @ParameterizedTest
  @MethodSource("getParsableModels")
  public void shouldParseValidGrammarModels(String filename) {
    this.parse(
        Paths.get(prefixValidModelsPath("/testinput/validGrammarModels/" + filename)).toString(),
        false);
    assertNoFindings();
  }
}
