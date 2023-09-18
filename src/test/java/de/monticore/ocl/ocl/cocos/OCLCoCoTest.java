// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl.cocos;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.*;
import de.monticore.ocl.util.SymbolTableUtil;
import de.se_rwth.commons.logging.Log;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

public class OCLCoCoTest extends AbstractTest {

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  @ParameterizedTest
  @MethodSource("getValidCocoModels")
  public void acceptsValidModels(String filename) {
    // todo ignoring test container1.ocl which fails due to
    // https://git.rwth-aachen.de/monticore/monticore/-/issues/3141
    assumeFalse(filename.endsWith("container1.ocl"));
    // todo find the issue with these, s.
    // https://git.rwth-aachen.de/monticore/monticore/-/issues/3331
    assumeFalse(filename.endsWith("validConstructorName.ocl"));
    assumeFalse(filename.endsWith("validMethSigName.ocl"));
    assumeFalse(filename.endsWith("validParameterDeclarationName.ocl"));
    assumeFalse(filename.endsWith("validParameterType.ocl"));
    assumeFalse(filename.endsWith("validVariableDeclaration.ocl"));
    assumeFalse(filename.endsWith("validVariableName.ocl"));

    // given
    final Optional<ASTOCLCompilationUnit> ast =
            parse(filename, false);
    assertTrue(ast.isPresent());

    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/DefaultTypes.sym");

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

    final Optional<ASTOCLCompilationUnit> optAST =
            parse(filename, false);
    assertTrue(optAST.isPresent());
    final ASTOCLCompilationUnit ast = optAST.get();

    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
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

    assertFalse(
            Log.getFindings().isEmpty());
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
