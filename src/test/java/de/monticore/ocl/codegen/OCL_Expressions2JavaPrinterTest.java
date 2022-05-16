/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen;

import com.google.common.base.Preconditions;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.prettyprint.ExpressionsBasisPrettyPrinter;
import de.monticore.literals.prettyprint.MCCommonLiteralsPrettyPrinter;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.codegen.visitors.CommonExpressionsPrinter;
import de.monticore.ocl.codegen.visitors.OCLExpressionsPrinter;
import de.monticore.ocl.codegen.visitors.SetExpressionsPrinter;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLArtifact;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.types.check.OCLDeriver;
import de.monticore.ocl.types.check.OCLSynthesizer;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.prettyprint.MCBasicsPrettyPrinter;
import de.monticore.types.prettyprint.MCBasicTypesPrettyPrinter;
import de.monticore.types.prettyprint.MCCollectionTypesPrettyPrinter;
import de.monticore.types.prettyprint.MCSimpleGenericTypesPrettyPrinter;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * test for most Expressions found in OCL
 * this is NOT a test for OCLExpressions2Java-Printing
 */
public class OCL_Expressions2JavaPrinterTest extends AbstractTest {

  protected final static String RELATIVE_TARGET_PATH = "target"
      + File.separator
      + "generated-test-sources";

  @BeforeEach
  public void setup() {
    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/DefaultTypes.sym");
  }

  @ParameterizedTest
  @MethodSource("getModelsWithValidSymTab")
  public void shouldCalculateTrue(String filename) throws IOException {

    // Given
    final ASTOCLCompilationUnit ast = loadASTWithSymbols(prefixValidModelsPath(filename));

    // When
    final String classSource = print2JavaClass(ast.getOCLArtifact());
    final String classSourcePathStr =
        RELATIVE_TARGET_PATH + File.separator + ast.getOCLArtifact().getName() + ".java";
    Files.write(Paths.get(classSourcePathStr), classSource.getBytes());
    compile(classSourcePathStr);
    int exitCode = execute(ast.getOCLArtifact().getName());

    // Then
    assertEquals(0, exitCode);
    assertTrue(Log.getFindings().isEmpty());
  }

  protected String print2JavaClass(ASTOCLArtifact node) {
    IndentPrinter printer = new IndentPrinter();
    printer.print("public class ");
    printer.print(node.getName());
    printer.println(" {");
    printer.indent();
    printer.println("public static void main(String[] args) {");
    printer.indent();
    printer.println("Boolean result = true;");

    // without context, only context free invariants may be checked
    Set<ASTOCLInvariant> invariants = InvariantCollectionVisitor.getInvariants(node).stream()
        .filter(ASTOCLInvariant::isEmptyOCLContextDefinitions)
        .filter(ASTOCLInvariant::isEmptyOCLParamDeclarations)
        .collect(Collectors.toSet());
    assumeFalse(invariants.isEmpty());
    for (ASTOCLInvariant invariant : invariants) {
      printer.print("result &= ");
      printer.print(print2Java(invariant.getExpression()));
      printer.println(";");
    }

    printer.println("System.exit(result ? 0 : 1);");
    printer.unindent();
    printer.println("}");
    printer.unindent();
    printer.println("}");
    return printer.getContent();
  }

  protected void compile(String fileName) throws IOException {
    File javaSrcFile = new File(fileName);
    List<String> options = Arrays.asList(
        "-d",
        RELATIVE_TARGET_PATH
    );
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<JavaFileObject>();
    try (
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticsCollector,
            null, null);
    ) {
      Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(
          Arrays.asList(javaSrcFile));
      JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticsCollector,
          options, null,
          units);
      if (!task.call()) {
        fail(diagnostics2String(
            Collections.unmodifiableList(diagnosticsCollector.getDiagnostics())));
      }
    }
  }

  protected int execute(String fileName) {
    int exitCode = -1;
    try {
      String java = new StringBuilder()
          .append(System.getProperty("java.home"))
          .append(File.separator)
          .append("bin")
          .append(File.separator)
          .append("java")
          .toString();
      ProcessBuilder processBuilder = new ProcessBuilder();
      processBuilder.command(
          java,
          fileName
      );
      processBuilder.directory(new File(RELATIVE_TARGET_PATH));
      Process proc = processBuilder.start();
      exitCode = proc.waitFor();
    }
    catch (IOException | InterruptedException e) {
      fail(e);
    }
    return exitCode;
  }

  protected String print2Java(ASTExpression node) {
    OCLTraverser traverser = OCLMill.traverser();
    IndentPrinter printer = new IndentPrinter();
    VariableNaming naming = new VariableNaming();
    OCLDeriver deriver = new OCLDeriver();
    OCLSynthesizer synthesizer = new OCLSynthesizer();

    // Expressions setup
    CommonExpressionsPrinter comExprPrinter = new CommonExpressionsPrinter(printer, naming,
        deriver, synthesizer);
    traverser.setCommonExpressionsHandler(comExprPrinter);
    traverser.add4CommonExpressions(comExprPrinter);
    ExpressionsBasisPrettyPrinter exprBasPrinter = new ExpressionsBasisPrettyPrinter(printer);
    traverser.setExpressionsBasisHandler(exprBasPrinter);
    traverser.add4ExpressionsBasis(exprBasPrinter);
    OCLExpressionsPrinter oclExprPrinter = new OCLExpressionsPrinter(printer, naming,
        deriver, synthesizer);
    traverser.setOCLExpressionsHandler(oclExprPrinter);
    traverser.add4OCLExpressions(oclExprPrinter);
    SetExpressionsPrinter setExprPrinter = new SetExpressionsPrinter(printer, naming,
        deriver, synthesizer);
    traverser.setSetExpressionsHandler(setExprPrinter);
    traverser.add4SetExpressions(setExprPrinter);

    // Types
    MCSimpleGenericTypesPrettyPrinter simpleGenericTypes = new MCSimpleGenericTypesPrettyPrinter(
        printer);
    traverser.setMCSimpleGenericTypesHandler(simpleGenericTypes);
    traverser.add4MCSimpleGenericTypes(simpleGenericTypes);
    MCCollectionTypesPrettyPrinter collectionTypes = new MCCollectionTypesPrettyPrinter(printer);
    traverser.setMCCollectionTypesHandler(collectionTypes);
    traverser.add4MCCollectionTypes(collectionTypes);
    MCBasicTypesPrettyPrinter basicTypes = new MCBasicTypesPrettyPrinter(printer);
    traverser.setMCBasicTypesHandler(basicTypes);
    traverser.add4MCBasicTypes(basicTypes);
    MCBasicsPrettyPrinter basics = new MCBasicsPrettyPrinter(printer);
    traverser.add4MCBasics(basics);

    MCCommonLiteralsPrettyPrinter comLitPrinter = new MCCommonLiteralsPrettyPrinter(printer);
    traverser.setMCCommonLiteralsHandler(comLitPrinter);
    traverser.add4MCCommonLiterals(comLitPrinter);

    node.accept(traverser);
    return printer.getContent();
  }

  protected ASTOCLCompilationUnit loadASTWithSymbols(String fileName) throws IOException {
    Optional<ASTOCLCompilationUnit> ast = parse(fileName, false);
    Preconditions.checkState(ast.isPresent(), Log.getFindings());
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());

    return ast.get();
  }

  /**
   * returns a string describing the compilation errors
   * used for miore readable test0results given failing tests.
   * @param diagnostics The comlilation diagnostic results
   * @return the compilation errors as String
   */
  protected String diagnostics2String(final List<Diagnostic<?>> diagnostics) {
    return diagnostics.stream().map(d ->
        new StringBuilder()
            .append(d.getLineNumber())
            .append(":")
            .append(d.getColumnNumber())
            .append(": ")
            .append(d.getMessage(null))
            .toString()
    ).collect(Collectors.joining(System.lineSeparator()));
  }

}
