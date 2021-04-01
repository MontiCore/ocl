// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl.cocos;

import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.ExpressionHasNoSideEffect;
import de.monticore.ocl.ocl._cocos.OCLCoCoChecker;
import de.monticore.ocl.ocl._cocos.ValidTypes;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.ocl.util.SymbolTableUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class OCLCoCoTest extends AbstractTest {

  @ParameterizedTest
  @MethodSource("getModelsWithValidSymTab")
  public void acceptsValidModels(String filename) throws IOException {
    // given
    final Optional<ASTOCLCompilationUnit> ast = parse(prefixValidModelsPath(filename), false);
    assertThat(ast).isPresent();

    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/DefaultTypes.sym");

    // when
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());

    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.addCoCo(new ValidTypes(new DeriveSymTypeOfOCLCombineExpressions()));
    checker.checkAll(ast.get());
  }

  @ParameterizedTest
  @CsvSource({
    "src/test/resources/testinput/oclplibrary/collection.ocl",
    "src/test/resources/testinput/oclplibrary/list.ocl",
    "src/test/resources/testinput/oclplibrary/listAndSet.ocl",
    "src/test/resources/testinput/oclplibrary/set.ocl"
  })
  public void shouldAcceptOclpLibrary(final String oclFile) {
    // given
    final Optional<ASTOCLCompilationUnit> ast = parse(oclFile, false);
    assertThat(ast).isPresent();
    SymbolTableUtil.prepareMill();

    // when / then
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());

    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.addCoCo(new ValidTypes(new DeriveSymTypeOfOCLCombineExpressions()));
    checker.checkAll(ast.get());
  }

  //@Test
  public void testBanking() throws IOException {

    /*
    Optional<ASTOCLCompilationUnit> ast = new OCLParser().parse(Paths.get(RELATIVE_MODEL_PATH + "/docs/Banking.ocl").toString());
    initSymbolTable("/docs/Banking.cd", RELATIVE_MODEL_PATH + "/docs");
    OCLScopesGenitorDelegator genitor = new OCLScopesGenitorDelegator();
    TypeCheck tc = new TypeCheck(new FullSynthesizeSymTypeFromMCSimpleGenericTypes(), new DeriveSymTypeOfOCLCombineExpressions());
    genitor.createFromAST(ast.get());
    OCLCoCoChecker checker = OCLCoCos.createChecker(new DeriveSymTypeOfOCLCombineExpressions());
    checker.checkAll(ast.get());

     */
  }
}
