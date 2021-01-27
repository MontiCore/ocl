// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl;

import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._parser.OCLParser;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractTest {
  protected static final String RELATIVE_MODEL_PATH = "src/test/resources";

  public static String[] getValidModels() {
    File f = new File(RELATIVE_MODEL_PATH + "/testinput/validGrammarModels");
    String[] filenames = f.list();
    assertThat(filenames).isNotNull();
    return filenames;
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
