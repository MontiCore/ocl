package de.monticore.ocl.ocl._parser;

import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OCLParserTest {

  protected static final String RELATIVE_MODEL_PATH = "src/test/resources";

  public Optional<ASTOCLCompilationUnit> parse(String relativeFilePath,
    boolean expParserErrors) {
    OCLParser parser = new OCLParser();
    Optional<ASTOCLCompilationUnit> optAst;
    try {
      optAst = parser.parse(relativeFilePath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (expParserErrors) {
      assertThat(parser.hasErrors()).isTrue();
      assertThat(optAst).isNotPresent();
    } else {
      assertThat(parser.hasErrors()).isFalse();
      assertThat(optAst).isPresent();
    }
    return optAst;
  }

  //Todo: test all models
  @ParameterizedTest
  @ValueSource(strings = {
    "example/cocos/valid/validConstructorName.ocl",
    "example/cocos/valid/validConstructorName0.ocl",
    "example/cocos/valid/validFileName.ocl",
    "example/cocos/valid/validInvariantName.ocl",
    //"example/cocos/valid/validMethodDeclarationName.ocl",
    "example/cocos/valid/validMethSigName.ocl",
    "example/cocos/valid/validParameterDeclarationName.ocl",
    "example/cocos/valid/validParameterType.ocl",
    "example/cocos/valid/validPostStatementName.ocl",
    "example/cocos/valid/validPrePost.ocl",
    "example/cocos/valid/validPreStatementName.ocl",
    //"example/cocos/valid/validTypes.ocl",
    "example/cocos/valid/validVariableName.ocl"})
  public void shouldParseValidInput(String fileName) {
    this.parse(Paths.get(RELATIVE_MODEL_PATH, fileName).toString(), false);
  }



}