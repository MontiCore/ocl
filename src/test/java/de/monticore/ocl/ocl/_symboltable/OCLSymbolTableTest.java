// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.util.SymbolTableUtil;
import de.se_rwth.commons.logging.Log;
import java.util.Optional;
import org.junit.jupiter.api.Assumptions;
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
  public void shouldCreateSymTabForValidModels(String filename) {
    // todo https://git.rwth-aachen.de/monticore/monticore/-/issues/4509
    Assumptions.assumeFalse(
        filename.endsWith("Test06.ocl")
            || filename.endsWith("Test08.ocl")
            || filename.endsWith("Test01.ocl")
            || filename.endsWith("Test02.ocl")
            || filename.endsWith("Test03.ocl")
            || filename.endsWith("Test04.ocl")
            || filename.endsWith("Test07.ocl")
            || filename.endsWith("Test09.ocl")
            || filename.endsWith("comprehension10.ocl")
            || filename.endsWith("comprehension12.ocl")
            || filename.endsWith("comprehension13.ocl")
            || filename.endsWith("comprehension3.ocl")
            || filename.endsWith("comprehension5.ocl")
            || filename.endsWith("comprehension7.ocl")
            || filename.endsWith("comprehension9.ocl")
            || filename.endsWith("invalidVariableDeclaration.ocl")
            || filename.endsWith("listsAndSetsAsArguments.ocl")
            || filename.endsWith("prepost12.ocl")
            || filename.endsWith("prepost13.ocl")
            || filename.endsWith("quantifiers12.ocl")
            || filename.endsWith("quantifiers13.ocl")
            || filename.endsWith("setoperations11.ocl")
            || filename.endsWith("setoperations13.ocl")
            || filename.endsWith("setoperations15.ocl")
            || filename.endsWith("setoperations16.ocl")
            || filename.endsWith("setoperations6.ocl")
            || filename.endsWith("setoperations8.ocl")
            || filename.endsWith("sizeAndLength.ocl")
            || filename.endsWith("special2.ocl")
            || filename.endsWith("special3.ocl")
            || filename.endsWith("special4.ocl")
            || filename.endsWith("special5.ocl")
            || filename.endsWith("typeIfString.ocl")
            || filename.endsWith("typeif1.ocl")
            || filename.endsWith("ParameterDeclarationName.ocl")
            || filename.endsWith("cases1.ocl")
            || filename.endsWith("cases2.ocl")
            || filename.endsWith("association2.ocl")
            || filename.endsWith("commonOperatorsBoolean.ocl")
            || filename.endsWith("commonOperatorsInteger.ocl")
            || filename.endsWith("comparisons.ocl"));

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
  public void shouldNotCreateSymTabForValidModels(String filename) {

    // todo https://git.rwth-aachen.de/monticore/monticore/-/issues/4509
    Assumptions.assumeFalse(filename.endsWith("setoperations8.ocl"));
    final Optional<ASTOCLCompilationUnit> ast = parse(filename, false);
    assertThat(ast).isPresent();

    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/DefaultTypes.sym");

    // when / then
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());

    assertFalse(Log.getFindings().isEmpty(), "calls to Log.error expected");
  }
}
