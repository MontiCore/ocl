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
    "example/cocos/valid/validConstructorName.ocl",
    "example/cocos/valid/validConstructorName0.ocl",
    "example/cocos/valid/validFileName.ocl",
    "example/cocos/valid/validInvariantName.ocl",
    "example/cocos/valid/validMethSigName.ocl",
    "example/cocos/valid/validParameterDeclarationName.ocl",
    "example/cocos/valid/validParameterType.ocl",
    "example/cocos/valid/validPostStatementName.ocl",
    "example/cocos/valid/validPrePost.ocl",
    "example/cocos/valid/validPreStatementName.ocl",
    "example/cocos/valid/validVariableName.ocl"})
  public void shouldParseValidInput(String fileName) {
    this.parse(Paths.get(RELATIVE_MODEL_PATH, fileName).toString(), false);
  }

  @ParameterizedTest
  @MethodSource("getValidModels")
  public void shouldParseValidGrammarModels(String filename) {
    this.parse(Paths.get(prefixValidModelsPath(filename)).toString(), false);
  }
}