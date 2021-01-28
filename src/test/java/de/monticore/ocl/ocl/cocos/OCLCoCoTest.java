package de.monticore.ocl.ocl.cocos;

import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.ExpressionHasNoSideEffect;
import de.monticore.ocl.ocl._cocos.OCLCoCoChecker;
import de.monticore.ocl.ocl._cocos.OCLCoCos;
import de.monticore.ocl.ocl._cocos.ValidTypes;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl._symboltable.OCLSymbolTableCreator;
import de.monticore.ocl.ocl._symboltable.OCLSymbolTableCreatorDelegator;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.ocl.types.check.FullSynthesizeSymTypeFromMCSimpleGenericTypes;
import de.monticore.types.check.TypeCheck;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

public class OCLCoCoTest extends AbstractTest {
  
  /*@ParameterizedTest
  @MethodSource("getValidModels")*/
  public void checkExpressionHasNoSideEffectCoCo(String fileName) throws IOException {
    Optional<ASTOCLCompilationUnit> ast = new OCLParser().parse(Paths.get(RELATIVE_MODEL_PATH + "/testinput/validGrammarModels/" + fileName).toString());
    initSymbolTable("/testinput/CDs/AuctionCD.cd", RELATIVE_MODEL_PATH + "/testinput/CDs");
    OCLSymbolTableCreatorDelegator symbolTableCreator = new OCLSymbolTableCreatorDelegator(globalScope);
    ((OCLSymbolTableCreator)symbolTableCreator.getOCLVisitor().get()).setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());
    TypeCheck tc = new TypeCheck(new FullSynthesizeSymTypeFromMCSimpleGenericTypes(), new DeriveSymTypeOfOCLCombineExpressions());
    symbolTableCreator.createFromAST(ast.get());
    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.addCoCo(new ValidTypes(new DeriveSymTypeOfOCLCombineExpressions()));
    checker.checkAll(ast.get());
  }

  //@Test
  public void testBanking() throws IOException {
    Optional<ASTOCLCompilationUnit> ast = new OCLParser().parse(Paths.get(RELATIVE_MODEL_PATH + "/docs/bankingChecks.ocl").toString());
    initSymbolTable("/docs/Banking.cd", RELATIVE_MODEL_PATH + "/docs");
    OCLSymbolTableCreatorDelegator symbolTableCreator = new OCLSymbolTableCreatorDelegator(globalScope);
    ((OCLSymbolTableCreator)symbolTableCreator.getOCLVisitor().get()).setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());
    TypeCheck tc = new TypeCheck(new FullSynthesizeSymTypeFromMCSimpleGenericTypes(), new DeriveSymTypeOfOCLCombineExpressions());
    symbolTableCreator.createFromAST(ast.get());
    OCLCoCoChecker checker = OCLCoCos.createChecker(new DeriveSymTypeOfOCLCombineExpressions());
    checker.checkAll(ast.get());
  }
}
