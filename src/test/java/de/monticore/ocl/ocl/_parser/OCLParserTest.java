package de.monticore.ocl.ocl._parser;

import de.monticore.ocl.ocl.AbstractTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Paths;

class OCLParserTest extends AbstractTest {

  protected static final String RELATIVE_MODEL_PATH = "src/test/resources";

  @ParameterizedTest
  @ValueSource(strings = {
    "docs/Bookshop.ocl",
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
    "testinput/cocos/valid/validVariableName.ocl"})
  public void shouldParseValidInput(String fileName) {
    this.parse(Paths.get(RELATIVE_MODEL_PATH, fileName).toString(), false);
  }

  @ParameterizedTest
  @MethodSource("getParsableModels")
  public void shouldParseValidGrammarModels(String filename) {
    this.parse(Paths.get(prefixValidModelsPath(filename)).toString(), false);
  }
}