/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen;

import com.google.common.base.Preconditions;
import de.monticore.io.paths.MCPath;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.util.SymbolTableUtil;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Optional;

public class OCL2JavaGeneratorTest {

  protected final static String RELATIVE_MODEL_PATH = "src/test/resources";
  protected final static String RELATIVE_TARGET_PATH = "target/generated-test-sources";
  protected final static String TEST_MODEL_PATH = "codegen/input";
  protected final static String PACKAGE = "invariants";
  protected final static String EXPECTED_RESULT_PATH = "codegen/desired/invariants";
  protected final static String TEST_TARGET_PATH = "codegen/invariants";

  @BeforeEach
  protected void init() {
    LogStub.init();
    Log.enableFailQuick(false);
    this.setup();
  }

  protected void setup() {
    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    MCPath modelPath = new MCPath(Paths.get(RELATIVE_MODEL_PATH, TEST_MODEL_PATH).getParent());
    OCLMill.globalScope().setSymbolPath(modelPath);
  }

  @ParameterizedTest
  @ValueSource(strings = {"Test01", "Test02", "Test03", "Test04", "Test05", "Test06", "Test07", "Test08", "Test09"})
  public void shouldGenerate(String s) throws IOException {
    Preconditions.checkNotNull(s);
    Preconditions.checkArgument(!s.isEmpty());

    // Given
    File input = Paths.get(RELATIVE_MODEL_PATH, TEST_MODEL_PATH, PACKAGE, s + ".ocl").toFile();
    File expected = Paths.get(RELATIVE_MODEL_PATH, EXPECTED_RESULT_PATH, s + ".java").toFile();
    File target = Paths.get(RELATIVE_TARGET_PATH, TEST_TARGET_PATH, s + ".java").toFile();
    ASTOCLCompilationUnit ast = loadASTWithSymbols(input);

    // When
    OCL2JavaGenerator.generate(ast, target.toString());

    // Then
    Assertions.assertEquals(
      StringUtils.deleteWhitespace(FileUtils.readLines(expected, Charset.defaultCharset()).toString()),
      StringUtils.deleteWhitespace(FileUtils.readLines(target, Charset.defaultCharset()).toString()),
      "The content of the generated file \'" + target + "\'"
        + " and the content of the expected file \'" + expected + "\'"
        + " do not match.");
  }


  protected ASTOCLCompilationUnit loadASTWithSymbols(File input) throws IOException {
    OCLParser parser = new OCLParser();
    Optional<ASTOCLCompilationUnit> ast = parser.parse(input.toString());
    Preconditions.checkState(ast.isPresent(), Log.getFindings());
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());
    return ast.get();
  }
}
