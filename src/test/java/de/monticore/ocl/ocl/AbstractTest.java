// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl;

import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl._symboltable.IOCLGlobalScope;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractTest {

  @BeforeAll
  public static void init() {
    LogStub.init();
    Log.enableFailQuick(false);
  }

  @BeforeEach
  public void setUp() {
    Log.getFindings().clear();
  }

  protected static final String RELATIVE_MODEL_PATH = "src/test/resources";

  protected IOCLGlobalScope globalScope;

  public static String[] getParsableModels() {
    File f = new File(RELATIVE_MODEL_PATH + "/testinput/validGrammarModels");
    String[] filenames = f.list();
    assertThat(filenames).isNotNull();
    filenames = Arrays.stream(filenames)
      .sorted()
      .collect(Collectors.toList())
      .toArray(filenames);

    return filenames;
  }

  public static String[] getModelsWithValidSymTab() {
    List<String> files = Arrays.stream(getParsableModels())
      .filter(f -> f.matches("^[a-n].*"))
      .collect(Collectors.toList());
    String[] result = new String[files.size()];
    files.toArray(result);
    return result;
  }

  public static String prefixValidModelsPath(String fileName) {
    return RELATIVE_MODEL_PATH + "/testinput/validGrammarModels/" + fileName;
  }

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
}
