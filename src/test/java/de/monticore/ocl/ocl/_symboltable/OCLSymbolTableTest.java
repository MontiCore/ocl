// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import de.monticore.class2mc.Java2MCResolver;
import de.monticore.io.paths.ModelPath;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.ExpressionHasNoSideEffect;
import de.monticore.ocl.ocl._cocos.OCLCoCoChecker;
import de.monticore.ocl.ocl._cocos.ValidTypes;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.oclexpressions._symboltable.OCLExpressionsSymbolTableCompleter;
import de.monticore.ocl.setexpressions._symboltable.SetExpressionsSymbolTableCompleter;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.oosymbols.OOSymbolsMill;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

public class OCLSymbolTableTest extends AbstractTest {

  @ParameterizedTest
  @MethodSource("getValidModels")
  public void shouldCreateSymTabForValidModels(String filename) throws IOException {
    // given
    final Optional<ASTOCLCompilationUnit> ast = parse(prefixValidModelsPath(filename), false);
    final String cdSymbols = "src/test/resources/testinput/CDs/AuctionCD.cdsym";
    OCLMill.reset();
    OCLMill.init();
    OCLMill.globalScope().clear();
    BasicSymbolsMill.initializePrimitives();

    OOSymbolsMill.globalScope().setModelPath(new ModelPath(Paths.get(prefixValidModelsPath(""))));
    Java2MCResolver resolver = new Java2MCResolver(OOSymbolsMill.globalScope());
    OCLMill.globalScope().addAdaptedTypeSymbolResolver(resolver);
    OOSymbolsMill.globalScope().addAdaptedTypeSymbolResolver(resolver);

    OCLSymbols2Json deSer = new OCLSymbols2Json();
    OCLMill.globalScope().addSubScope(deSer.load(cdSymbols));
    OCLMill.globalScope()
      .addSubScope(deSer.load("src/test/resources/testinput/CDs/DefaultTypes.cdsym"));

    // when
    OCLScopesGenitorDelegator genitor = OCLMill.scopesGenitorDelegator();
    genitor.createFromAST(ast.get());

    OCLSymbolTableCompleter stCompleter = new OCLSymbolTableCompleter(
      ast.get().getMCImportStatementList(), ast.get().getPackage()
    );
    stCompleter.setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());
    OCLExpressionsSymbolTableCompleter stCompleter2 = new OCLExpressionsSymbolTableCompleter(
      ast.get().getMCImportStatementList(), ast.get().getPackage()
    );
    stCompleter2.setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());
    SetExpressionsSymbolTableCompleter stCompleter3 = new SetExpressionsSymbolTableCompleter(
      ast.get().getMCImportStatementList(), ast.get().getPackage()
    );
    stCompleter3.setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());

    OCLTraverser t = OCLMill.traverser();
    t.add4BasicSymbols(stCompleter);
    t.add4OCL(stCompleter);
    t.setOCLHandler(stCompleter);
    t.setOCLExpressionsHandler(stCompleter2);
    t.setSetExpressionsHandler(stCompleter3);
    t.add4BasicSymbols(stCompleter2);
    t.add4OCLExpressions(stCompleter2);
    t.add4BasicSymbols(stCompleter3);
    t.add4SetExpressions(stCompleter3);
    ast.get().accept(t);

    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.addCoCo(new ValidTypes(new DeriveSymTypeOfOCLCombineExpressions()));
    checker.checkAll(ast.get());
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
