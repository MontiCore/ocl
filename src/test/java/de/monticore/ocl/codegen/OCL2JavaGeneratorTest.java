/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class OCL2JavaGeneratorTest {

  @BeforeEach
  public void init() {
    LogStub.init();
    Log.enableFailQuick(false);
  }

  protected final String RELATIVE_MODEL_PATH = "src/test/resources";

  protected final String TEST_MODEL_PATH = "codegen/input/invariants";

  @ParameterizedTest
  @ValueSource(strings = {"Test01.ocl", "Test02.ocl", "Test03.ocl", "Test04.ocl", "Test05.ocl",
    "Test06.ocl", "Test07.ocl", "Test08.ocl", "Test09.ocl"})
  public void shouldGenerate(String s) throws IOException {
    // Given
    OCLParser parser = new OCLParser();

    Supplier<ASTOCLCompilationUnit> supplier = () -> {
      Log.printFindings();
      return null;
    };
    ASTOCLCompilationUnit ast = parser.parse(Paths.get(RELATIVE_MODEL_PATH, TEST_MODEL_PATH, s).toString())
      .orElse(supplier.get());
    Assertions.assertNotNull(ast);
  }

  protected static boolean compare(File expectedFile, File generatedFile) throws IOException {
    List<String> expected = FileUtils.readLines(expectedFile, Charset.defaultCharset());
    List<String> generated = FileUtils.readLines(generatedFile, Charset.defaultCharset());
    expected = removeWhitespace(expected);
    generated = removeWhitespace(generated);

    return expected.equals(generated);
  }

  protected static List<String> removeWhitespace(List<String> text) {
    Preconditions.checkNotNull(text);
    List<String> newText = new LinkedList<>();
    for (String line : text) {
      if (!line.equals("")) {
        String newLine = line.replaceAll("\\s+", "");
        newText.add(newLine);
      }
    }

    return newText;
  }
}
