package de.monticore.ocl.ocl.cocos;

import de.monticore.ocl.ocl.AbstractTest;

import java.io.IOException;

public class OCLCoCoTest extends AbstractTest {

  // TODO: Wait for CD4A symbol table and type check fixes
  //@ParameterizedTest
  //@MethodSource("getValidModels")
  public void checkExpressionHasNoSideEffectCoCo(String fileName) throws IOException {
    /*
    Optional<ASTOCLCompilationUnit> ast = new OCLParser().parse(Paths.get(RELATIVE_MODEL_PATH + "/testinput/validGrammarModels/" + fileName).toString());
    initSymbolTable("/testinput/CDs/AuctionCD.cd", RELATIVE_MODEL_PATH + "/testinput/CDs");
    OCLScopesGenitorDelegator genitor = new OCLScopesGenitorDelegator();
    TypeCheck tc = new TypeCheck(new FullSynthesizeSymTypeFromMCSimpleGenericTypes(), new DeriveSymTypeOfOCLCombineExpressions());
    genitor.createFromAST(ast.get());
    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.addCoCo(new ValidTypes(new DeriveSymTypeOfOCLCombineExpressions()));
    checker.checkAll(ast.get());
     */
  }

  //@Test
  public void testBanking() throws IOException {
    /*
    Optional<ASTOCLCompilationUnit> ast = new OCLParser().parse(Paths.get(RELATIVE_MODEL_PATH + "/docs/bankingChecks.ocl").toString());
    initSymbolTable("/docs/Banking.cd", RELATIVE_MODEL_PATH + "/docs");
    OCLScopesGenitorDelegator genitor = new OCLScopesGenitorDelegator();
    TypeCheck tc = new TypeCheck(new FullSynthesizeSymTypeFromMCSimpleGenericTypes(), new DeriveSymTypeOfOCLCombineExpressions());
    genitor.createFromAST(ast.get());
    OCLCoCoChecker checker = OCLCoCos.createChecker(new DeriveSymTypeOfOCLCombineExpressions());
    checker.checkAll(ast.get());

     */
  }
}
