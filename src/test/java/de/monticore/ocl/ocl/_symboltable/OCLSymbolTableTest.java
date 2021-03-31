// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OCLSymbolTableTest extends AbstractTest {

  @ParameterizedTest
  @MethodSource("getModelsWithValidSymTab")
  public void shouldCreateSymTabForValidModels(String filename) throws IOException {
    // given
    final Optional<ASTOCLCompilationUnit> ast = parse(prefixValidModelsPath(filename), false);
    assertThat(ast).isPresent();

    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/DefaultTypes.sym");

    // when / then
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());
  }

  //TODO: remove ignore when fixed
  @Ignore
  @Test
  public void testAddedMethods(){
    //add resolver and methods
    SymbolTableUtil.prepareMill();

    //resolve for List
    Optional<TypeSymbol> optList = OCLMill.globalScope().resolveType("java.util.List");
    assertTrue(optList.isPresent());
    TypeSymbol list = optList.get();

    //resolve for prepend, test for correct parameters and return type
    Optional<FunctionSymbol> optPrepend = list.getSpannedScope().resolveFunction("prepend");
    assertTrue(optPrepend.isPresent());
    FunctionSymbol prepend = optPrepend.get();
    assertEquals("List<E>", prepend.getReturnType().print());
    assertEquals(1,prepend.getParameterList().size());
    VariableSymbol o = prepend.getParameterList().get(0);
    assertEquals("o", o.getName());
    assertEquals("E", o.getType().print());

    //resolve for asSet, test for correct parameters and return type
    Optional<FunctionSymbol> optAsSet = list.getSpannedScope().resolveFunction("asSet");
    assertTrue(optAsSet.isPresent());
    FunctionSymbol asSet = optAsSet.get();
    assertEquals("Set<E>", asSet.getReturnType().print());
    assertEquals(0, asSet.getParameterList().size());
  }
}
