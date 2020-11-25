package de.monticore.ocl.ocl.cocos;

import de.monticore.io.paths.ModelPath;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._cocos.ExpressionHasNoSideEffect;
import de.monticore.ocl.ocl._cocos.IterateExpressionVariableUsageIsCorrect;
import de.monticore.ocl.ocl._cocos.OCLCoCoChecker;
import de.monticore.ocl.ocl._cocos.OCLCoCos;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl._symboltable.*;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.types.check.SynthesizeSymTypeFromMCSimpleGenericTypes;
import de.monticore.types.check.TypeCheck;
import de.se_rwth.commons.logging.Log;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.compiler.TypeChecker;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

public class OCLCoCoTest {

  protected static final String RELATIVE_MODEL_PATH = "src/test/resources";

  protected IOCLGlobalScope globalScope;
  
  /*@ParameterizedTest
  @MethodSource("getValidModels")*/
  public void checkExpressionHasNoSideEffectCoCo(String fileName) throws IOException {
    Optional<ASTOCLCompilationUnit> ast = new OCLParser().parse(Paths.get(RELATIVE_MODEL_PATH + "/example/validGrammarModels/" + fileName).toString());
    setupGlobalScope();
    OCLSymbolTableCreator symbolTableCreator = new OCLSymbolTableCreator(globalScope);
    TypeCheck tc = new TypeCheck(new SynthesizeSymTypeFromMCSimpleGenericTypes(), new DeriveSymTypeOfOCLCombineExpressions());
    symbolTableCreator.setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());
    symbolTableCreator.createFromAST(ast.get());
    /*if(ast.get().getOCLArtifact().getOCLConstraint(0) instanceof ASTOCLInvariant && !((ASTOCLInvariant) ast.get().getOCLArtifact().getOCLConstraint(0)).getParamsList().isEmpty()){
      Log.debug(String.valueOf(tc.symTypeFromAST(((ASTOCLInvariant) ast.get().getOCLArtifact().getOCLConstraint(0)).getParams(0).getMCType())));
    }*/
    //OCLCoCoChecker checker = OCLCoCos.createChecker(new TypeChecker(, ClassPool.getDefault()));
    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ExpressionHasNoSideEffect());
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

}
