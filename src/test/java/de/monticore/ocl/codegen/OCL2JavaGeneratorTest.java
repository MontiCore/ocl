/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.common.base.Preconditions;
import de.monticore.io.paths.MCPath;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.util.SymbolTableUtil;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import de.se_rwth.commons.logging.LogStub;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class OCL2JavaGeneratorTest extends AbstractTest {

  protected static final String RELATIVE_MODEL_PATH = "src/test/resources";

  protected static final String RELATIVE_TARGET_PATH = "target/generated-test-sources";

  protected static final String TEST_MODEL_PATH = "testinput/parsable/symtab/invalid_coco";

  protected static final String PACKAGE = "invariants";

  protected static final String TEST_TARGET_PATH = "codegen/invariants";


  @BeforeEach
  protected void init() {
    this.setup();
    this.initLogger();
  }

  protected void setup() {
    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    MCPath modelPath = new MCPath(Paths.get(RELATIVE_MODEL_PATH, TEST_MODEL_PATH).getParent());
    OCLMill.globalScope().setSymbolPath(modelPath);
  }

  @Test
  public void placeHolder() {

  }


  @ParameterizedTest
  @ValueSource(
          strings = {"Test01", "Test02", "Test03", "Test04", "Test06", "Test07", "Test08"})
  public void shouldGenerate(String s) throws IOException {
    Preconditions.checkNotNull(s);
    Preconditions.checkArgument(!s.isEmpty());
    // todo enable after https://git.rwth-aachen.de/monticore/monticore/-/issues/3141 is closed
    Assumptions.assumeFalse(s.equals("Test02"));
    // todo enable after https://git.rwth-aachen.de/monticore/monticore/-/issues/3168 is closed
    Assumptions.assumeFalse(s.equals("Test06"));

    // Given
    File input = Paths.get(RELATIVE_MODEL_PATH, TEST_MODEL_PATH, s + ".ocl").toFile();
    File target = Paths.get(RELATIVE_TARGET_PATH, TEST_TARGET_PATH, s + ".java").toFile();
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
    ASTOCLCompilationUnit ast = loadASTWithSymbols(input);

    // When
    OCL2JavaGenerator.generate(ast, target.toString());

    // Then
    compile(target);
    assertNoFindings();
  }
//    This following Test is for the new model tree structure, it requires at least one valid model. We out comment it
//    because we don't have such a model yet. But, please keep this commented code for future usage. Thanks!
//
//    @ParameterizedTest
//    @MethodSource("getJavaGeneratedModels")
//    public void shouldGenerate(String filename) throws IOException {
//        Preconditions.checkNotNull(filename);
//        Preconditions.checkArgument(!filename.isEmpty());
//        // todo enable after https://git.rwth-aachen.de/monticore/monticore/-/issues/3141 is closed
//        Assumptions.assumeFalse(filename.endsWith("Test02.ocl"));
//        // todo enable after https://git.rwth-aachen.de/monticore/monticore/-/issues/3168 is closed
//        Assumptions.assumeFalse(filename.endsWith("Test06.ocl"));
//
//        // Given
//        File input = Paths.get(filename).toFile();
//        File target = Paths.get(filename.replace(".ocl", ".java")).toFile();
//        SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
//        ASTOCLCompilationUnit ast = loadASTWithSymbols(input);
//
//        // When
//        OCL2JavaGenerator.generate(ast, target.toString());
//
//        // Then
//        compile(target);
//        assertTrue(
//                Log.getFindings().isEmpty(),
//                Log.getFindings().stream()
//                        .map(Finding::buildMsg)
//                        .collect(Collectors.joining(System.lineSeparator())));
//        target.delete();
//    }


  protected ASTOCLCompilationUnit loadASTWithSymbols(File input) throws IOException {
    OCLParser parser = new OCLParser();
    Optional<ASTOCLCompilationUnit> ast = parser.parse(input.toString());
    Preconditions.checkState(ast.isPresent(), Log.getFindings());
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());
    return ast.get();
  }

  /**
   * tries to compile the file fails the test if there were compilation errors
   *
   * @param file to be compiled
   * @throws IOException file exception
   */
  protected void compile(File file) throws IOException {
    List<String> options = Arrays.asList("-d", RELATIVE_TARGET_PATH);
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();
    try (StandardJavaFileManager fileManager =
                 compiler.getStandardFileManager(diagnosticsCollector, null, null); ) {
      Iterable<? extends JavaFileObject> units =
              fileManager.getJavaFileObjectsFromFiles(Collections.singleton(file));
      JavaCompiler.CompilationTask task =
              compiler.getTask(null, fileManager, diagnosticsCollector, options, null, units);
      if (!task.call()) {
        fail(
                diagnostics2String(
                        Collections.unmodifiableList(diagnosticsCollector.getDiagnostics())));
      }
    }
  }

  /**
   * returns a string describing the compilation errors used for more readable test results given
   * failing tests.
   *
   * @param diagnostics The compilation diagnostic results
   * @return the compilation errors as String
   */
  protected String diagnostics2String(final List<Diagnostic<?>> diagnostics) {
    return diagnostics.stream()
            .map(
                    d ->
                            new StringBuilder()
                                    .append(d.getLineNumber())
                                    .append(":")
                                    .append(d.getColumnNumber())
                                    .append(": ")
                                    .append(d.getMessage(null))
                                    .toString())
            .collect(Collectors.joining(System.lineSeparator()));
  }

  public static String[] getModelsByFolder(String folderpath) {

    String modelDir = RELATIVE_MODEL_PATH + folderpath;
    File dirFile = new File(modelDir);
    String[] extensions = new String[]{"ocl"};
    List<File> models = (List<File>) FileUtils.listFiles(dirFile, extensions, true);

    String[] filenames = new String[models.size()];
    for (int i=0; i< models.size(); i++) {
      filenames[i] = models.get(i).getPath();
    }
    assertThat(filenames).isNotNull();
    filenames = Arrays.stream(filenames).sorted().collect(Collectors.toList()).toArray(filenames);

    return filenames;
  }


  public static String[] getJavaGeneratedModels() {
    return getModelsByFolder("/testinput/parsable/symtab/coco/javagen");
  }


}