// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractTest {

  @BeforeEach
  protected void initLogger() {
    LogStub.init();
    Log.enableFailQuick(false);
    Log.getFindings().clear();
  }

  protected void initMills() {
    OCLMill.reset();
    OCLMill.init();
    OCLMill.globalScope().clear();
  }

  protected static final String RELATIVE_MODEL_PATH = "src/test/resources";

  public static String[] getModels(String folderPath) {
    File f = new File(RELATIVE_MODEL_PATH + folderPath);
    String[] filenames = f.list();
    assertThat(filenames).isNotNull();
    filenames = Arrays.stream(filenames).sorted().collect(Collectors.toList()).toArray(filenames);

    return filenames;
  }

  public static String[] getParsableModels() {
    return getModels("/testinput/validGrammarModels");
  }

  public static String[] getValidCoCoModels() {
    return getModels("/testinput/cocos/valid");
  }

  public static String[] getInvalidCoCoModels() {
    return getModels("/testinput/cocos/invalid");
  }

  public static String[] getModelsWithValidSymTab() {
    List<String> files =
        Arrays.stream(getParsableModels())
            .filter(f -> f.matches("^[a-n].*"))
            .collect(Collectors.toList());
    String[] result = new String[files.size()];
    files.toArray(result);
    return result;
  }

  public static String prefixValidModelsPath(String fileName) {
    return RELATIVE_MODEL_PATH + fileName;
  }

  public Optional<ASTOCLCompilationUnit> parse(String relativeFilePath, boolean expParserErrors) {
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

  protected void assertNoFindings() {
    assertTrue(
        Log.getFindings().isEmpty(),
        Log.getFindings().stream()
            .map(Finding::buildMsg)
            .collect(Collectors.joining(System.lineSeparator())));
  }
}
