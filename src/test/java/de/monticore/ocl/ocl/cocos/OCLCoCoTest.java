package de.monticore.ocl.ocl.cocos;

import com.google.common.collect.Sets;
import de.monticore.cd4analysis.CD4AnalysisMill;
import de.monticore.cd4analysis._parser.CD4AnalysisParser;
import de.monticore.cd4analysis._symboltable.CD4AnalysisGlobalScope;
import de.monticore.cd4analysis._symboltable.CD4AnalysisSymbolTableCreatorDelegator;
import de.monticore.cd4analysis.trafo.CD4AnalysisTrafo4DefaultsDelegator;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.io.paths.ModelPath;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.ExpressionHasNoSideEffect;
import de.monticore.ocl.ocl._cocos.OCLCoCoChecker;
import de.monticore.ocl.ocl._cocos.OCLCoCos;
import de.monticore.ocl.ocl._cocos.ValidTypes;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl._symboltable.CDTypeSymbolDelegate;
import de.monticore.ocl.ocl._symboltable.IOCLGlobalScope;
import de.monticore.ocl.ocl._symboltable.OCLSymbolTableCreator;
import de.monticore.ocl.ocl._symboltable.OCLSymbolTableCreatorDelegator;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.ocl.types.check.FullSynthesizeSymTypeFromMCSimpleGenericTypes;
import de.monticore.types.check.TypeCheck;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

public class OCLCoCoTest extends AbstractTest {

  protected IOCLGlobalScope globalScope;
  
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

  private void setupGlobalScope() {
    this.globalScope = OCLMill.globalScope();
    this.globalScope.setModelPath(new ModelPath(Paths.get(RELATIVE_MODEL_PATH)));
    this.globalScope.setFileExt("ocl");
  }

  public void initSymbolTable(String model, String modelPath) throws IOException {
    initSymbolTable(model, Paths.get(modelPath).toFile());
  }


  public void initSymbolTable(String model, File... modelPaths) throws IOException {
    Set<Path> p = Sets.newHashSet();
    for (File mP : modelPaths) {
      p.add(Paths.get(mP.getAbsolutePath()));
    }

    final ModelPath mp = new ModelPath(p);

    CD4AnalysisGlobalScope cd4AGlobalScope = (CD4AnalysisGlobalScope) CD4AnalysisMill.globalScope();
    cd4AGlobalScope.setModelPath(mp);
    cd4AGlobalScope.setFileExt("cd");

    CD4AnalysisSymbolTableCreatorDelegator symbolTableCreatorDelegator = new CD4AnalysisSymbolTableCreatorDelegator(cd4AGlobalScope);
    Optional<ASTCDCompilationUnit> ast = new CD4AnalysisParser().parse(Paths.get(RELATIVE_MODEL_PATH + model).toString());
    CD4AnalysisTrafo4DefaultsDelegator a = new CD4AnalysisTrafo4DefaultsDelegator();
    a.transform(ast.get());
    symbolTableCreatorDelegator.createFromAST(ast.get());

    CDTypeSymbolDelegate cdTypeSymbolDelegate = new CDTypeSymbolDelegate(cd4AGlobalScope);

    setupGlobalScope();
    globalScope.addAdaptedTypeSymbolResolver(cdTypeSymbolDelegate);
  }

  @Test
  public void testSymbolTable() throws IOException {
    Optional<ASTOCLCompilationUnit> ast = new OCLParser().parse(Paths.get(RELATIVE_MODEL_PATH + "/testinput/validGrammarModels/container1.ocl").toString());
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
