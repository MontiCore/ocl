// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl.cocos;

import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.ExpressionHasNoSideEffect;
import de.monticore.ocl.ocl._cocos.ExpressionValidCoCo;
import de.monticore.ocl.ocl._cocos.OCLCoCoChecker;
import de.monticore.ocl.types.check.OCLDeriver;
import de.monticore.ocl.util.SymbolTableUtil;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class OCLCoCoTest extends AbstractTest {

  @BeforeAll
  public static void init() {
    LogStub.init();
    Log.enableFailQuick(false);
  }

  @BeforeEach
  public void setup() {
    Log.getFindings().clear();
  }

  @ParameterizedTest
  @MethodSource("getModelsWithValidSymTab")
  public void acceptsValidModels(String filename) {
    // todo ignoring test container1.ocl which fails due to
    // https://git.rwth-aachen.de/monticore/monticore/-/issues/3141
    assumeFalse(filename.equals("container1.ocl"));

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
    checker.addCoCo(new ExpressionValidCoCo(new OCLDeriver()));
    checker.checkAll(ast.get());
    assertThat(Log.getFindings().isEmpty());
  }

  @ParameterizedTest
  @CsvSource({
      "src/test/resources/testinput/oclplibrary/list.ocl",
      "src/test/resources/testinput/oclplibrary/listAndSet.ocl",
      "src/test/resources/testinput/oclplibrary/set.ocl",
      "src/test/resources/testinput/oclplibrary/staticQueries.ocl"
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
    checker.addCoCo(new ExpressionValidCoCo(new OCLDeriver()));
    checker.checkAll(ast.get());
    assertThat(Log.getFindings().isEmpty());
  }
}
