/*
 *  * (c) https://github.com/MontiCore/monticore
 *  *
 *  * The license generally applicable for this project
 *  * can be found under https://github.com/MontiCore/monticore.
 */

package de.monticore.ocl.expressions.prettyprint;

import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLFile;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.prettyprint.IndentPrinter;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OCLPrettyPrinterTest {

  @BeforeClass
  public static void init() {
    LogStub.init();
    Log.enableFailQuick(false);
  }

  @Before
  public void setUp() {
    Log.getFindings().clear();
  }

  @Test
  public void testOCLCompilationUnit() throws IOException {
    final OCLParser parser = new OCLParser();
    final Optional<ASTOCLCompilationUnit> ast = parser.parse_StringOCLCompilationUnit("package blub; import foo; ocl TestOCL {}");
    assertTrue(ast.isPresent());
    assertFalse(parser.hasErrors());

    final OCLCombinePrettyPrinter printer = new OCLCombinePrettyPrinter(new IndentPrinter());
    String output = printer.prettyprint(ast.get());
    final Optional<ASTOCLCompilationUnit> astPrint = parser.parse_StringOCLCompilationUnit(output);
    assertFalse(parser.hasErrors());
    assertTrue(astPrint.isPresent());
    System.out.println("output: " + output);
    assertTrue(ast.get().deepEquals(astPrint.get()));
  }

  @Test
  public void testOCLFile() throws IOException {
    final OCLParser parser = new OCLParser();
    final Optional<ASTOCLFile> ast = parser.parse_StringOCLFile("ocl TestOCL {}");
    assertTrue(ast.isPresent());
    assertFalse(parser.hasErrors());

    final OCLCombinePrettyPrinter printer = new OCLCombinePrettyPrinter(new IndentPrinter());
    String output = printer.prettyprint(ast.get());
    final Optional<ASTOCLFile> astPrint = parser.parse_StringOCLFile(output);
    assertFalse(parser.hasErrors());
    assertTrue(astPrint.isPresent());
    System.out.println("output: " + output);
    assertTrue(ast.get().deepEquals(astPrint.get()));
  }
}
