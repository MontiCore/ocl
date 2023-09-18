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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
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

  public static String[] getModelsByFolder(String folderpath) {

    String modelDir = RELATIVE_MODEL_PATH + folderpath;
    File dirFile = new File(modelDir);
    String[] extensions = new String[] {"ocl"};
    List<File> models = (List<File>) FileUtils.listFiles(dirFile, extensions, true);

    String[] filenames = new String[models.size()];
    for (int i = 0; i < models.size(); i++) {
      filenames[i] = models.get(i).getPath();
    }
    assertThat(filenames).isNotNull();
    filenames = Arrays.stream(filenames).sorted().collect(Collectors.toList()).toArray(filenames);

    return filenames;
  }

  public static String[] getModelsFromFile(String models_filename) {
    String filepath = RELATIVE_MODEL_PATH + "/sorting_results/" + models_filename;
    List<String> filenames;
    try (Stream<String> lines = Files.lines(Paths.get(filepath))) {
      filenames = lines.collect(Collectors.toList());
      assertThat(filenames).isNotNull();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    for (String filename : filenames) {
      if (!filename.endsWith(".ocl")) {
        filenames.remove(filename);
      }
    }
    String[] result = new String[filenames.size()];
    result = filenames.toArray(result);
    return result;
  }

  public static String[] getParsableModels() {
    return getModelsByFolder("/testinput/parsable");
  }

  public static String[] getNotParsableModels() {
    return getModelsByFolder("/testinput/not_parsable");
  }

  public static String[] getSymbolTableModels() {
    return getModelsByFolder("/testinput/parsable/symtab");
  }

  public static String[] getNoSymbolTableModels() {
    return getModelsByFolder("/testinput/parsable/no_symtab");
  }

  public static String[] getValidCocoModels() {
    return getModelsByFolder("/testinput/parsable/symtab/coco");
  }

  public static String[] getInvalidCocoModels() {
    return getModelsByFolder("/testinput/parsable/symtab/invalid_coco");
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
