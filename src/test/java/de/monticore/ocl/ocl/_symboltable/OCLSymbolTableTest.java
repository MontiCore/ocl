// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.util.SymbolTableUtil;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class OCLSymbolTableTest extends AbstractTest {

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  // for parsable models with a symbol table.
  @ParameterizedTest
  @MethodSource("getSymbolTableModels")
  public void shouldCreateSymTabForValidModels(String filename) throws IOException {

    final Optional<ASTOCLCompilationUnit> ast = parse(filename, false);
    assertThat(ast).isPresent();

    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/DefaultTypes.sym");

    // when / then
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());

    assertNoFindings();
  }

  // for parsable models with no symbol table.
  @ParameterizedTest
  @MethodSource("getNoSymbolTableModels")
  public void shouldNotCreateSymTabForValidModels(String filename) throws IOException {

    final Optional<ASTOCLCompilationUnit> ast = parse(filename, false);
    assertThat(ast).isPresent();

    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/DefaultTypes.sym");

    // when / then
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());

    assertFalse(
        Log.getFindings().isEmpty(),
        Log.getFindings().stream()
            .map(Finding::buildMsg)
            .collect(Collectors.joining(System.lineSeparator())));
  }
}
