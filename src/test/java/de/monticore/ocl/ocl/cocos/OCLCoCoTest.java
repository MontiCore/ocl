package de.monticore.ocl.ocl.cocos;

import com.google.common.collect.Sets;
import de.monticore.cd4analysis.CD4AnalysisMill;
import de.monticore.cd4analysis._parser.CD4AnalysisParser;
import de.monticore.cd4analysis._symboltable.CD4AnalysisGlobalScope;
import de.monticore.cd4analysis._symboltable.CD4AnalysisSymbolTableCreatorDelegator;
import de.monticore.cd4analysis.trafo.CD4AnalysisTrafo4DefaultsDelegator;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.io.paths.ModelPath;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.*;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl._symboltable.*;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.types.check.SynthesizeSymTypeFromMCSimpleGenericTypes;
import de.monticore.types.check.TypeCheck;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

public class OCLCoCoTest {

  protected static final String RELATIVE_MODEL_PATH = "src/test/resources";

  protected IOCLGlobalScope globalScope;
  
  /*@ParameterizedTest
  @MethodSource("getValidModels")*/
  public void checkExpressionHasNoSideEffectCoCo(String fileName) throws IOException {
    Optional<ASTOCLCompilationUnit> ast = new OCLParser().parse(Paths.get(RELATIVE_MODEL_PATH + "/example/validGrammarModels/" + fileName).toString());
    initSymbolTable(RELATIVE_MODEL_PATH + "/example/CDs");
    OCLSymbolTableCreatorDelegator symbolTableCreator = new OCLSymbolTableCreatorDelegator(globalScope);
    ((OCLSymbolTableCreator)symbolTableCreator.getOCLVisitor().get()).setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());
    TypeCheck tc = new TypeCheck(new SynthesizeSymTypeFromMCSimpleGenericTypes(), new DeriveSymTypeOfOCLCombineExpressions());
    symbolTableCreator.createFromAST(ast.get());
    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.addCoCo(new ValidTypes(new DeriveSymTypeOfOCLCombineExpressions()));
    checker.checkAll(ast.get());
  }

  private static String[] getValidModels(){
    File f = new File(RELATIVE_MODEL_PATH + "/example/validGrammarModels");
    String[] filenames = f.list();
    return filenames;
  }

  private void setupGlobalScope() {
    this.globalScope = new OCLGlobalScopeBuilder()
            .setModelPath(new ModelPath(Paths.get(RELATIVE_MODEL_PATH)))
            .setModelFileExtension("ocl")
            .build();

  }

  public void initSymbolTable(String modelPath) throws IOException {
    initSymbolTable(Paths.get(modelPath).toFile());
  }


  public void initSymbolTable(File... modelPaths) throws IOException {
    Set<Path> p = Sets.newHashSet();
    for (File mP : modelPaths) {
      p.add(Paths.get(mP.getAbsolutePath()));
    }

    final ModelPath mp = new ModelPath(p);

    CD4AnalysisGlobalScope cd4AGlobalScope = (CD4AnalysisGlobalScope) CD4AnalysisMill.cD4AnalysisGlobalScopeBuilder()
            .setModelPath(mp)
            .setModelFileExtension("cd")
            .build();

    CD4AnalysisSymbolTableCreatorDelegator symbolTableCreatorDelegator = new CD4AnalysisSymbolTableCreatorDelegator(cd4AGlobalScope);
    Optional<ASTCDCompilationUnit> ast = new CD4AnalysisParser().parse(Paths.get(RELATIVE_MODEL_PATH + "/example/CDs/AuctionCD.cd").toString());
    CD4AnalysisTrafo4DefaultsDelegator a = new CD4AnalysisTrafo4DefaultsDelegator();
    a.transform(ast.get());
    symbolTableCreatorDelegator.createFromAST(ast.get());

    CDTypeSymbolDelegate cdTypeSymbolDelegate = new CDTypeSymbolDelegate(cd4AGlobalScope);

    setupGlobalScope();
    globalScope.addAdaptedTypeSymbolResolver(cdTypeSymbolDelegate);
  }

  @Test
  public void testSymbolTable() throws IOException {
    Optional<ASTOCLCompilationUnit> ast = new OCLParser().parse(Paths.get(RELATIVE_MODEL_PATH + "/example/validGrammarModels/container1.ocl").toString());
    initSymbolTable(RELATIVE_MODEL_PATH + "/example/CDs");
    OCLSymbolTableCreatorDelegator symbolTableCreator = new OCLSymbolTableCreatorDelegator(globalScope);
    ((OCLSymbolTableCreator)symbolTableCreator.getOCLVisitor().get()).setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());
    TypeCheck tc = new TypeCheck(new SynthesizeSymTypeFromMCSimpleGenericTypes(), new DeriveSymTypeOfOCLCombineExpressions());
    symbolTableCreator.createFromAST(ast.get());
    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.addCoCo(new ValidTypes(new DeriveSymTypeOfOCLCombineExpressions()));
    checker.checkAll(ast.get());
  }
}
