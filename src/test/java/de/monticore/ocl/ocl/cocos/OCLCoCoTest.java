// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl.cocos;

import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.OCLCoCoChecker;
import de.monticore.ocl.ocl._cocos.OCLCoCos;
import de.monticore.ocl.util.SymbolTableUtil;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class OCLCoCoTest extends AbstractTest {

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  @ParameterizedTest
  @MethodSource("getValidCocoModels")
  public void acceptsValidModels(String filename) {
    // inference is kept at a specific complexity
    assumeFalse(filename.endsWith("comprehension8.ocl"));
    // todo https://git.rwth-aachen.de/monticore/monticore/-/issues/3462
    assumeFalse(filename.contains("cases2"));
    // todo https://git.rwth-aachen.de/monticore/monticore/-/issues/4179
    // todo https://git.rwth-aachen.de/monticore/monticore/-/issues/4509
    // requires further tests
    // given
    final Optional<ASTOCLCompilationUnit> ast = parse(filename, false);
    assertTrue(ast.isPresent());

    SymbolTableUtil.prepareMill();
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");

    // when
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());

    OCLCoCoChecker checker = OCLCoCos.createChecker();
    checker.checkAll(ast.get());
    assertNoFindings();
  }

  @ParameterizedTest
  @MethodSource("getInvalidCocoModels")
  public void acceptsInvalidModels(String filename) {

    // todo https://git.rwth-aachen.de/monticore/monticore/-/issues/4509
    Assumptions.assumeFalse(true);

    final Optional<ASTOCLCompilationUnit> optAST = parse(filename, false);
    assertTrue(optAST.isPresent());
    final ASTOCLCompilationUnit ast = optAST.get();

    SymbolTableUtil.prepareMill();

    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/DefaultTypes.sym");

    SymbolTableUtil.runSymTabGenitor(ast);
    SymbolTableUtil.runSymTabCompleter(ast);

    OCLCoCoChecker checker = OCLCoCos.createChecker();
    checker.checkAll(ast);

    if (filename.equals("invalidConstructorNameStartsWithCapitalLetter.ocl")) {
      assertEquals(2, Log.getFindings().size());
      assertTrue(Log.getFindings().get(0).getMsg().startsWith("0xOCL01"));
      assertTrue(Log.getFindings().get(1).getMsg().startsWith("0xOCL0D"));
      Log.getFindings().clear();
    }

    if (filename.equals("invalidContextHasOnlyOneType.ocl")) {
      assertEquals(1, Log.getFindings().size());
      assertTrue(Log.getFindings().get(0).getMsg().startsWith("0xOCL23"));
      Log.getFindings().clear();
    }

    if (filename.equals("invalidContextVariableNamesAreUnique.ocl")) {
      assertEquals(1, Log.getFindings().size());
      assertTrue(Log.getFindings().get(0).getMsg().startsWith("0xOCL22"));
      Log.getFindings().clear();
    }

    if (filename.equals("invalidInvariantStartsWithCapitalLetter.ocl")) {
      assertEquals(1, Log.getFindings().size());
      assertTrue(Log.getFindings().get(0).getMsg().startsWith("0xOCL03"));
      Log.getFindings().clear();
    }

    if (filename.equals("invalidMethodStartsWithLowerCaseLetter.ocl")) {
      assertEquals(1, Log.getFindings().size());
      assertTrue(Log.getFindings().get(0).getMsg().startsWith("0xOCL05"));
      Log.getFindings().clear();
    }

    if (filename.equals("invalidParameterNamesUnique.ocl")) {
      assertEquals(1, Log.getFindings().size());
      assertTrue(Log.getFindings().get(0).getMsg().startsWith("0xOCL22"));
      Log.getFindings().clear();
    }

    if (filename.equals("invalidConditionsAreBooleanType.ocl")) {
      assertTrue(1 <= Log.getFindings().size());
      assertTrue(Log.getFindings().get(0).getMsg().startsWith("0xOCL06"));
      Log.getFindings().clear();
    }

    if (filename.equals("invalidUnnamedInvariantHasParameters.ocl")) {
      assertEquals(1, Log.getFindings().size());
      assertTrue(Log.getFindings().get(0).getMsg().startsWith("0xOCL25"));
      Log.getFindings().clear();
    }

    if (filename.equals("invalidVariableDeclaration.ocl")) {
      assertEquals(1, Log.getFindings().size());
      assertTrue(Log.getFindings().get(0).getMsg().startsWith("0xOCL33"));
      Log.getFindings().clear();
    }

    assertNoFindings();
  }

  @ParameterizedTest
  @CsvSource({
      "src/test/resources/testinput/parsable/symtab/coco/not_javagen/list_oclplibrary.ocl",
      "src/test/resources/testinput/parsable/symtab/coco/not_javagen/listAndSet.ocl",
      "src/test/resources/testinput/parsable/symtab/coco/not_javagen/set.ocl",
      "src/test/resources/testinput/parsable/symtab/coco/not_javagen/staticQueries.ocl"
  })
  public void shouldAcceptOclpLibrary(final String oclFile) {
    // given
    final Optional<ASTOCLCompilationUnit> ast = parse(oclFile, false);
    assertTrue(ast.isPresent());
    SymbolTableUtil.prepareMill();

    // when / then
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());

    OCLCoCoChecker checker = OCLCoCos.createChecker();
    checker.checkAll(ast.get());

    assertNoFindings();
  }
}
