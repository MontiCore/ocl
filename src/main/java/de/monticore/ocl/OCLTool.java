// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl;

import com.google.common.base.Preconditions;
import de.monticore.cd4analysis.CD4AnalysisMill;
import de.monticore.cd4analysis._cocos.CD4AnalysisCoCoChecker;
import de.monticore.cd4analysis._parser.CD4AnalysisParser;
import de.monticore.cd4analysis._symboltable.CD4AnalysisSymbolTableCreatorDelegator;
import de.monticore.cd4analysis._symboltable.ICD4AnalysisArtifactScope;
import de.monticore.cd4analysis._symboltable.ICD4AnalysisGlobalScope;
import de.monticore.cd4analysis._symboltable.ICD4AnalysisScope;
import de.monticore.cd4analysis.cocos.CD4AnalysisCoCos;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDPackage;
import de.monticore.io.paths.ModelPath;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.OCLCoCoChecker;
import de.monticore.ocl.ocl._cocos.OCLCoCos;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl._symboltable.*;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.ocl.util.ParserUtil;
import de.se_rwth.commons.logging.Log;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public class OCLTool {
  protected OCLCoCoChecker oclChecker;

  protected CD4AnalysisCoCoChecker cdChecker;

  protected boolean isSymTabInitialized;

  public static final String OCL_FILE_EXTENSION = "ocl";

  public static final String CD_FILE_EXTENSION = "cd";

  protected static final String TOOL_NAME = "OCLTool";

  public OCLTool() {
    this(OCLCoCos.createChecker(), new CD4AnalysisCoCos().createNewChecker());
  }

  public OCLTool(OCLCoCoChecker oclChecker,
    CD4AnalysisCoCoChecker cdChecker) {
    Preconditions.checkArgument(oclChecker != null);
    Preconditions.checkArgument(cdChecker != null);
    this.oclChecker = oclChecker;
    this.cdChecker = cdChecker;
    this.isSymTabInitialized = false;
  }

  protected OCLCoCoChecker getOCLChecker() {
    return this.oclChecker;
  }

  protected CD4AnalysisCoCoChecker getCdChecker() {
    return this.cdChecker;
  }

  public IOCLGlobalScope processModels(Path... modelPaths) {
    Preconditions.checkArgument(modelPaths != null);
    Preconditions.checkArgument(!Arrays.asList(modelPaths).contains(null));
    ModelPath mp = new ModelPath(Arrays.asList(modelPaths));
    ICD4AnalysisGlobalScope cdGlobalScope = CD4AnalysisMill.globalScope();
    cdGlobalScope.setModelPath(mp);
    cdGlobalScope.setFileExt(CD_FILE_EXTENSION);

    IOCLGlobalScope oclGlobalScope = OCLMill.globalScope();
    oclGlobalScope.setModelPath(mp);
    oclGlobalScope.setFileExt(OCL_FILE_EXTENSION);

    linkResolvingDelegates(oclGlobalScope, cdGlobalScope);

    this.processModels(cdGlobalScope);
    this.processModels(oclGlobalScope);
    return oclGlobalScope;
  }

  protected void linkResolvingDelegates(IOCLGlobalScope oclGlobalScope,
    ICD4AnalysisGlobalScope cdGlobalScope) {
    CDTypeSymbolDelegate cdTypeSymbolDelegate = new CDTypeSymbolDelegate(cdGlobalScope);
    oclGlobalScope.addAdaptedTypeSymbolResolver(cdTypeSymbolDelegate);
  }

  public void processModels(IOCLGlobalScope scope) {
    Preconditions.checkArgument(scope != null);
    for (IOCLArtifactScope as : this.createSymbolTable(scope)) {
      ASTOCLCompilationUnit a = (ASTOCLCompilationUnit) as.getAstNode();
      a.accept(this.getOCLChecker());
    }
  }

  public void processModels(ICD4AnalysisGlobalScope scope) {
    Preconditions.checkArgument(scope != null);
    for (ICD4AnalysisArtifactScope a : this.createSymbolTable(scope)) {
      for (ICD4AnalysisScope as : a.getSubScopes()) {
        ASTCDPackage astNode = (ASTCDPackage) as.getSpanningSymbol().getAstNode();
        astNode.accept(this.getCdChecker());
      }
    }
  }

  public Collection<IOCLArtifactScope> createSymbolTable(IOCLGlobalScope scope) {
    Preconditions.checkArgument(scope != null);
    Collection<IOCLArtifactScope> result = new HashSet<>();

    for (ASTOCLCompilationUnit ast : parseModels(scope)) {
      OCLSymbolTableCreatorDelegator symTab = new OCLSymbolTableCreatorDelegator(
        scope);

      if (!symTab.getOCLVisitor().isPresent()) {
        Log.error("0xOCL31 Symbol table is not linked to OCL visitor");
      } else {
        OCLSymbolTableCreator symTabCreator = (OCLSymbolTableCreator) symTab.getOCLVisitor().get();
        symTabCreator.setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());
      }

      result.add(symTab.createFromAST(ast));
    }
    return result;
  }

  public Collection<ICD4AnalysisArtifactScope> createSymbolTable(ICD4AnalysisGlobalScope scope) {
    Preconditions.checkArgument(scope != null);
    Collection<ICD4AnalysisArtifactScope> result = new HashSet<>();
    for (ASTCDCompilationUnit ast : parseModels(scope)) {
      CD4AnalysisSymbolTableCreatorDelegator symTab = new CD4AnalysisSymbolTableCreatorDelegator(
        scope);
      result.add(symTab.createFromAST(ast));
    }
    return result;
  }

  public Collection<ASTOCLCompilationUnit> parseModels(IOCLGlobalScope scope) {
    return (Collection<ASTOCLCompilationUnit>) ParserUtil
      .parseModels(scope, OCL_FILE_EXTENSION, new OCLParser());
  }

  public Collection<ASTCDCompilationUnit> parseModels(ICD4AnalysisGlobalScope scope) {
    return (Collection<ASTCDCompilationUnit>) ParserUtil
      .parseModels(scope, CD_FILE_EXTENSION, new CD4AnalysisParser());
  }

  public Optional<ASTOCLCompilationUnit> parseOCL(String filename) {
    return (Optional<ASTOCLCompilationUnit>) ParserUtil.parse(filename, new OCLParser());
  }

  public Optional<ASTCDCompilationUnit> parseCD(String filename) {
    return (Optional<ASTCDCompilationUnit>) ParserUtil.parse(filename, new CD4AnalysisParser());
  }

  public Collection<ASTOCLCompilationUnit> parseOCL(Path path) {
    return (Collection<ASTOCLCompilationUnit>) ParserUtil
      .parse(path, OCL_FILE_EXTENSION, new OCLParser());
  }

  public Collection<ASTCDCompilationUnit> parseCD(Path path) {
    return (Collection<ASTCDCompilationUnit>) ParserUtil
      .parse(path, CD_FILE_EXTENSION, new CD4AnalysisParser());
  }
}
