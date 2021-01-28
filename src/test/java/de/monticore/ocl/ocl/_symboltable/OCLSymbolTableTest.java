// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.ExpressionHasNoSideEffect;
import de.monticore.ocl.ocl._cocos.OCLCoCoChecker;
import de.monticore.ocl.ocl._cocos.ValidTypes;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;

import java.io.IOException;
import java.util.Optional;

public class OCLSymbolTableTest extends AbstractTest {

  // TODO: Wait for CD4A symbol table and type check fixes
  //@ParameterizedTest
  //@MethodSource("getValidModels")
  public void shouldCreateSymTabForValidModels(String filename) throws IOException {
    // given
    final Optional<ASTOCLCompilationUnit> ast = parse(prefixValidModelsPath(filename), false);
    initSymbolTable("/testinput/CDs/AuctionCD.cd", RELATIVE_MODEL_PATH + "/testinput/CDs");

    // when
    OCLSymbolTableCreatorDelegator symbolTableCreator = new OCLSymbolTableCreatorDelegator(globalScope);
    ((OCLSymbolTableCreator)symbolTableCreator.getOCLVisitor().get()).setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());
    symbolTableCreator.createFromAST(ast.get());


    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.addCoCo(new ValidTypes(new DeriveSymTypeOfOCLCombineExpressions()));
    checker.checkAll(ast.get());
  }
}
