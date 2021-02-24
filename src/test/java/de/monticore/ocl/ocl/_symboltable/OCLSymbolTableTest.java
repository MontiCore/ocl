// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import de.monticore.ocl.ocl.AbstractTest;

import java.io.IOException;

public class OCLSymbolTableTest extends AbstractTest {

  // TODO: Wait for CD4A symbol table and type check fixes
  //@ParameterizedTest
  //@MethodSource("getValidModels")
  public void shouldCreateSymTabForValidModels(String filename) throws IOException {
    /*
    // given
    final Optional<ASTOCLCompilationUnit> ast = parse(prefixValidModelsPath(filename), false);
    initSymbolTable("/testinput/CDs/AuctionCD.cd", RELATIVE_MODEL_PATH + "/testinput/CDs");

    // when
    OCLScopesGenitorDelegator genitor = new OCLScopesGenitorDelegator();
    genitor.createFromAST(ast.get());


    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.addCoCo(new ValidTypes(new DeriveSymTypeOfOCLCombineExpressions()));
    checker.checkAll(ast.get());
     */
  }

  //@Test
  public void shouldCreateSymTabForValidModels() throws IOException {
    /*
    // given
    final Optional<ASTOCLCompilationUnit> ast = parse(RELATIVE_MODEL_PATH + "/docs/bookshop.ocl",
      false);
    initSymbolTable("/docs/Bookshop.cd", RELATIVE_MODEL_PATH + "/docs");

    // when
    OCLScopesGenitorDelegator genitor = new OCLScopesGenitorDelegator();
    genitor.createFromAST(ast.get());

    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.addCoCo(new ValidTypes(new DeriveSymTypeOfOCLCombineExpressions()));
    checker.checkAll(ast.get());

     */
  }
}
