// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import static org.assertj.core.api.Assertions.assertThat;

import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.util.SymbolTableUtil;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class OCLSymbolTableTest extends AbstractTest {

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  @ParameterizedTest
  @MethodSource("getModelsWithValidSymTab")
  public void shouldCreateSymTabForValidModels(String filename) throws IOException {
    // given
    final Optional<ASTOCLCompilationUnit> ast =
        parse(prefixValidModelsPath("/testinput/validGrammarModels/" + filename), false);
    assertThat(ast).isPresent();

    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/DefaultTypes.sym");

    // when / then
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());
    // todo enable after fix regarding 0xF737F
    // assertNoFindings();
  }
}
